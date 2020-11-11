package ch.treasurekeep.service.interactivebrokers;

import ch.treasurekeep.config.IbConfiguration;
import ch.treasurekeep.data.CostRepository;
import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.*;
import ch.treasurekeep.service.CurrencyConversionService;
import ch.treasurekeep.service.NotificationService;
import ch.treasurekeep.service.SettingsService;
import ch.treasurekeep.service.interactivebrokers.callbacks.CostMonitoringCallback;
import ch.treasurekeep.service.interactivebrokers.callbacks.NetLiquidationValueMonitoringCallback;
import ch.treasurekeep.service.interactivebrokers.callbacks.PortfolioRiskStatusCallback;
import ch.treasurekeep.service.interactivebrokers.callbacks.PositionCoverageObservingCallback;
import ch.treasurekeep.service.interactivebrokers.ewrappers.*;
import com.ib.client.*;
import kotlin.NotImplementedError;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * Central-Point for communicating with IB
 * Takes care of establishing and reestablishing connection
 * Every request will be triggered here, every response will be captured here
 *
 */
@Service
public class InteractiveBrokersService {
    private final List<String> accountsManaged = new ArrayList<>();
    private volatile EClientSocket socket;

    private final IbConfiguration ibConfiguration;
    private final NotificationService notificationService;
    private final SettingsService settingsService;
    private final LogRepository logRepository;
    private final CostRepository costRepository;
    private final CurrencyConversionService currencyDownloader;

    private final EWrapperComposite compositeWrapper;
    private final AccountUpdateEWrapper accountUpdateEWrapper;
    private final ExecutionsEWrapper executionsEWrapper;
    private final OpenOrderEWrapper openOrderEWrapper;
    private final OpenPositionEWrapper openPositionEWrapper;

    private final IBConnectionManagingThread ibConnectionManagingThread = new IBConnectionManagingThread();

    public InteractiveBrokersService(CostRepository costRepository, NotificationService notificationService, IbConfiguration connfiguration, SettingsService settingsService, LogRepository logRepository, NotificationService emailService, CurrencyConversionService currencyDownloader) {
        this.costRepository = costRepository;
        this.notificationService = notificationService;
        this.ibConfiguration = connfiguration;
        this.settingsService  = settingsService;
        this.logRepository = logRepository;
        this.currencyDownloader = currencyDownloader;

        this.accountUpdateEWrapper = new AccountUpdateEWrapper(this.logRepository);
        this.executionsEWrapper = new ExecutionsEWrapper(this.logRepository);
        this.openOrderEWrapper = new OpenOrderEWrapper(this.logRepository);
        this.openPositionEWrapper = new OpenPositionEWrapper(this.logRepository);

        this.compositeWrapper = new EWrapperComposite();
        this.compositeWrapper.addListener(this.accountUpdateEWrapper);
        this.compositeWrapper.addListener(this.executionsEWrapper);
        this.compositeWrapper.addListener(this.openOrderEWrapper);
        this.compositeWrapper.addListener(this.openPositionEWrapper);

        this.accountUpdateEWrapper.appendCallback(new NetLiquidationValueMonitoringCallback(this.settingsService, this.notificationService, this.logRepository, this));
    }

    @PostConstruct
    public void initialize() {
        this.ibConnectionManagingThread.start();
    }

    @PreDestroy
    public void finito() {
        this.accountUpdateEWrapper.continnousUnfire(this.socket);
        //placeholder, subscriptions must be closed here in case you have them
        this.ibConnectionManagingThread.kill();
        this.socket.eDisconnect();
    }

    /**
     * We switch every 3 minutes to the next account and subscribes again
     * Consequence: It might take several minutes until all the AccountUpdates are in the system.
     * The service handle this by simply not showing the related results
     * This is suboptimal but its a limitation of the API
     * (IB is not offering to subscribe to all Accounts at once)
     */
    @Scheduled(fixedRate = 1000*60*3+1)
    public void manageAccountUpdates() {//TODO Check if it is possible to resubscribe within less than 3 minutes
        if(this.accountsManaged.size() == 0) {
            this.accountsManaged.addAll(this.settingsService.getSettings().getManagedAccounts());
        }
        if(this.accountsManaged.size() == 0) {
            return;
        }
        this.accountUpdateEWrapper.continuousFire(this.accountsManaged.remove(0), this.socket);
    }

    @Scheduled(fixedRate = 1000*60*5)
    public void submitIBRequests() { //TODO check if some of these can be solved via subscriptions
        synchronized (this.socket) {
            if (this.socket.isConnected()) {
                this.executionsEWrapper.load(new CostMonitoringCallback(settingsService, costRepository, notificationService, logRepository, currencyDownloader));
                this.openOrderEWrapper.load(new PositionCoverageObservingCallback(notificationService, logRepository));

                this.executionsEWrapper.fire(this.socket);
                this.openOrderEWrapper.fire(this.socket);
            }
        }
    }

    public void createRiskReport(PortfolioRiskStatusCallback.Callback callback) {
        PortfolioRiskStatusCallback riskCallback = new PortfolioRiskStatusCallback(currencyDownloader, settingsService, logRepository, this.accountUpdateEWrapper, callback);
        new Thread(() -> {
            InteractiveBrokersService.this.openOrderEWrapper.load(riskCallback);
            InteractiveBrokersService.this.openOrderEWrapper.fire(InteractiveBrokersService.this.socket);
            InteractiveBrokersService.this.openPositionEWrapper.load(riskCallback);
            InteractiveBrokersService.this.openPositionEWrapper.fire(InteractiveBrokersService.this.socket);
        }).start();
    }

    public void emergencyExit(NetvalueThreshold threshold) {
        throw new NotImplementedError();
        /*
        The code below is garbage
                this.openOrderEWrapper.execute((fetchedOpenOrders, fetchedOrderStatus) -> {
                    Set<OpenOrderEWrapper.OpenOrder> openOrders = fetchedOpenOrders;
                    this.openPositionEWrapper.execute(position -> {
                        StringBuilder protocol = new StringBuilder();
                            Set<OpenPositionEWrapper.Position> positions = position;
                            for(OpenOrderEWrapper.OpenOrder oo : openOrders) {
                                    if (oo.isAffected(threshold)) {
                                        //socket.cancelOrder(oo.orderId);
                                        this.compositeWrapper.addListener(new DefaultEWrapper() {
                                            @Override
                                            public void nextValidId(int id) {
                                                try {
                                                    Thread.sleep(1000 * 40); //we anyway can not be sure if something really was cancelled so we just give it 40 secounds
                                                    Order order = new Order();
                                                    order.action("SELL");
                                                    order.orderType("MKT");
                                                    order.totalQuantity(oo.getQuantity(positions));
                                                    socket.placeOrder(id, oo.contract, order); // so far we only care about submitting if its executed we could report as well
                                                    protocol.append("Order was submitted");
                                                    InteractiveBrokersService.this.compositeWrapper.removeListener(this);
                                                } catch (Exception e) {
                                                    protocol.append(e.getMessage() + "\n");
                                                }
                                            }
                                        });
                                        socket.reqIds(99);
                                    }
                                }
                        InteractiveBrokersService.this.notificationService.sendNotification("EMERGENCYEXIT WAS TAKING PLACE: ", "Executionreport:\n" + protocol.toString());
                    });
                });

         */
    }

    /**
     * Establishes the connection to TWS and tries to reestablish when it went down.
     */
    private class IBConnectionManagingThread extends Thread {
        private volatile boolean running = true;

        public void kill() {
            this.running = false;
        }
        @Override
        public void run() {
            this.running = true;
            do {
                startIBGateway(compositeWrapper);
                do {
                    try{ Thread.sleep(1000*60); }
                    catch (InterruptedException e){ Thread.currentThread().interrupt(); }
                }
                while(InteractiveBrokersService.this.socket.isConnected()); //remember connection is not connection to ib but to tws only

            }while(this.running && InteractiveBrokersService.this.settingsService.getSettings().isReconnecting());
        }

        private void startIBGateway(EWrapperComposite wrapper) {
            MyEreaderSignal signal = new MyEreaderSignal();

            socket = new EClientSocket(wrapper, signal);
            socket.eConnect(
                    InteractiveBrokersService.this.ibConfiguration.getHost(),
                    InteractiveBrokersService.this.ibConfiguration.getPort(),
                    InteractiveBrokersService.this.ibConfiguration.getClientId()
            );

            final EReader reader = new EReader(socket, signal);

            reader.start();
            new Thread(() -> {
                while (socket.isConnected()) {
                    signal.waitForSignal();
                    try {
                        reader.processMsgs();
                    } catch (Exception e) {
                        InteractiveBrokersService.this.logRepository.insert(
                                new Log(InteractiveBrokersService.class.getName(),
                                        "Original message " + e.getMessage()
                                )
                        );
                    }
                }
            }).start();
        }
    }
}
