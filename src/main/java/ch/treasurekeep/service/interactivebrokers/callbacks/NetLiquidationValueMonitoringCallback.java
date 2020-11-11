package ch.treasurekeep.service.interactivebrokers.callbacks;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.Log;
import ch.treasurekeep.model.NetvalueThreshold;
import ch.treasurekeep.model.Settings;
import ch.treasurekeep.service.NotificationService;
import ch.treasurekeep.service.SettingsService;
import ch.treasurekeep.service.interactivebrokers.InteractiveBrokersService;
import ch.treasurekeep.service.interactivebrokers.ewrappers.AccountUpdateEWrapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Monitors the development of the Netto-Liquidation-Value.
 * In case of a Soft NetValueThreshold being breached a notification will be triggered
 * In case of a Hard NetValueThreshold being breached a notification will be triggered and the liquidation of the account will be initiated
 * Please be aware that a NetLiquidation value can not only be result of dropping position, also regular transactions lead to it
 */
public class NetLiquidationValueMonitoringCallback implements AccountUpdateEWrapper.AccountUpdateCallback{

    private final SettingsService settingsService;
    private final NotificationService notificationService;
    private final LogRepository logRepository;
    private final InteractiveBrokersService interactiveBrokersService;

    public NetLiquidationValueMonitoringCallback(SettingsService settingsService, NotificationService notificationService, LogRepository logRepository, InteractiveBrokersService interactiveBrokersService) {
        this.settingsService = settingsService;
        this.notificationService = notificationService;
        this.logRepository = logRepository;
        this.interactiveBrokersService = interactiveBrokersService;
    }

    @Override
    public void callback(List<AccountUpdateEWrapper.AccountUpdate> fetchedAccountUpdates, List<AccountUpdateEWrapper.PortfolioUpdate> fetchedPortfolioUpdates) {
        try {
            Settings settings = settingsService.getSettings();
            for (AccountUpdateEWrapper.AccountUpdate update : fetchedAccountUpdates) {
                if ("BASE".equals(update.currency) && "NetLiquidationByCurrency".equals(update.key)) {
                    List<NetvalueThreshold> relevantNetvalueThresholds = settings.getNetvalueThresholds().stream()
                            .filter(o -> o.getAccount().equals(update.accountName))
                            .filter(o -> o.isActive())
                            .filter(o -> Double.parseDouble(update.value) < o.getThreshold())
                            .collect(Collectors.toList());

                    for (NetvalueThreshold threshold : relevantNetvalueThresholds) {
                        deactivateThreshold(threshold);
                        if (threshold.getPriority() == NetvalueThreshold.Priority.SOFT) {
                            notificationService.sendNotification("NettoLiquidationValue got to low", "This is a soft breach, your netto liquidation value dropped below " + threshold.getThreshold());
                        }
                        if (threshold.getPriority() == NetvalueThreshold.Priority.HARD) {
                            notificationService.sendNotification("NettoLiquidationValue got to low", "This is a HARD breach, your netto liquidation value dropped below " + threshold.getThreshold());
                            new Thread(() -> { this.interactiveBrokersService.emergencyExit(threshold); }).start();
                        }
                    }
                }
            }
        } catch (Exception e) {
            this.logRepository.insert(new Log(NetLiquidationValueMonitoringCallback.class.getName(), e.getMessage()));
        }
    }

    private void deactivateThreshold(NetvalueThreshold a) {
        Settings clone = this.settingsService.getSettings();
        List<NetvalueThreshold> targets = clone.getNetvalueThresholds().stream().filter(o -> o.getId().equals(a.getId())).collect(Collectors.toList());
        if(targets.size() != 1) {
            throw new IllegalStateException();
        }
        targets.get(0).setActive(false);
        settingsService.setSettings(clone);
    }

}
