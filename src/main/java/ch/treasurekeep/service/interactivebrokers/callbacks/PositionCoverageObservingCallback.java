package ch.treasurekeep.service.interactivebrokers.callbacks;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.Log;
import ch.treasurekeep.service.NotificationService;
import ch.treasurekeep.service.interactivebrokers.ewrappers.OpenOrderEWrapper;
import com.ib.client.*;

import java.util.*;

/**
 * Monitors if there is a position in the portfolio that lost its cover (its stop)
 * It will be reported via notification.
 * Depends on receiving OpenOrders
 */
public class PositionCoverageObservingCallback implements OpenOrderEWrapper.OpenOrderCallback {

    private final Map<String, Double> lastRun = new HashMap<>();
    private final Map<String, Double> thisRun = new HashMap<>();
    private final NotificationService notificationService;
    private final LogRepository logRepository;

    public PositionCoverageObservingCallback(NotificationService notificationService, LogRepository logRepository) {
        this.notificationService = notificationService;
        this.logRepository = logRepository;
    }

    @Override
    public void callback(Set<OpenOrderEWrapper.OpenOrder> fetchedOpenOrders, Set<OpenOrderEWrapper.OrderStatus> fetchedOrderStatus) {
        try{
            this.lastRun.clear();
            this.lastRun.putAll(this.thisRun);
            this.thisRun.clear();
            this.thisRun.putAll(createThisRunData(fetchedOpenOrders));

            for(String message : detectLostCoverages() ) {
                PositionCoverageObservingCallback.this.notificationService.sendNotification("Position lost coverage", message);
            }
        }
        catch (Exception e) {
            PositionCoverageObservingCallback.this.logRepository.insert(new Log(PositionCoverageObservingCallback.class.getName(), e.getMessage()));
        }
    }

    private String[] detectLostCoverages() {
            List<String> result = new ArrayList<String>();
            for (String lastKEy : lastRun.keySet()) {
                if (!thisRun.containsKey(lastKEy)) {
                    result.add(lastKEy + " does not have protecting orders anymore");
                }
                if (thisRun.get(lastKEy) < lastRun.get(lastKEy)) {
                    result.add(lastKEy + " has reduced amount of protecting orders. Dropped from " + lastRun.get(lastKEy) + " to " + thisRun.get(lastKEy));
               }
            }
            return result.toArray(new String[result.size()]);
        }

    private Map<? extends String,? extends Double> createThisRunData(Set<OpenOrderEWrapper.OpenOrder> fetchedOpenOrders) { Map<String, Double> result = new HashMap<>();
        for(OpenOrderEWrapper.OpenOrder oo : fetchedOpenOrders) {
            Order order = oo.order;
            Contract contract = oo.contract;
            String key = order.account() + "." + contract.localSymbol();
            result.put(key, order.totalQuantity());
        }
        return result;
    }
}

