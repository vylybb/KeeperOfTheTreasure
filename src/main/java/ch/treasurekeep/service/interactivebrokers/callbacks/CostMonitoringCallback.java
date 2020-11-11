package ch.treasurekeep.service.interactivebrokers.callbacks;

import ch.treasurekeep.data.CostRepository;
import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.Cost;
import ch.treasurekeep.model.CostThreshold;
import ch.treasurekeep.model.Log;
import ch.treasurekeep.model.Settings;
import ch.treasurekeep.service.CurrencyConversionService;
import ch.treasurekeep.service.NotificationService;
import ch.treasurekeep.service.SettingsService;
import ch.treasurekeep.service.interactivebrokers.ewrappers.ExecutionsEWrapper;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Takes care of storing costs in database as well as checking if any CostThresholds were breached.
 * In case the server goes down, it should be able to recover as long as the down was not longer than a day, or it did not miss any costs while being down
 * (this is provided by the IB-adapter)
 * Costs will be stored only for one year and then removed.
 *
 * It depends on correctly receiving ExecutionReports
 */
public class CostMonitoringCallback implements ExecutionsEWrapper.ExecutionsCallback{
    
    private final SettingsService settingsService;
    private final CostRepository costRepository;
    private final NotificationService notificationService;
    private final CurrencyConversionService currencyDownloader;
    private final LogRepository logRepository;

    public CostMonitoringCallback(SettingsService settingsService, CostRepository costRepository, NotificationService notificationService, LogRepository logRepository, CurrencyConversionService currencyDownloader) {
        this.settingsService = settingsService;
        this.costRepository = costRepository;
        this.notificationService = notificationService;
        this.logRepository = logRepository;
        this.currencyDownloader = currencyDownloader;
    }

    @Override
    public void callback(Collection<ExecutionsEWrapper.Execution> executions) {
        new Thread(() -> {
            try{
                Settings settings = this.settingsService.getSettings();
                List<Cost> costs = createCosts(executions);
                for(Cost c : costs) {
                    costRepository.save(c); //save not insert
                }
                List<Cost> allCosts = costRepository.findAll();
                cleanupOneYear(allCosts);
                ZonedDateTime now = ZonedDateTime.now();
                for(CostThreshold setting : settings.getCostThresholds())
                {
                    Period settingsPeriod = Period.ofDays(setting.getDays());
                    List<Cost> relevantCosts = allCosts.stream()
                            .filter(
                                    cost -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(cost.getTimestamp()), ZoneId.systemDefault()).isAfter(now.minus(settingsPeriod)))
                            .collect(Collectors.toList());
                    Double costSum = relevantCosts.stream().mapToDouble(o -> o.getAmount() * currencyDownloader.convert(new CurrencyConversionService.CurrencyPair(settings.getBaseCurrency(), o.getCurrency()))).sum();
                    if (costSum > setting.getAmount()) {
                        this.notificationService.sendNotification("Cost Threshold breach hit", "Your total costs exceeded " + setting.getAmount() + " during " + settingsPeriod);
                    }
                }
            }
            catch (Exception e) {
                this.logRepository.insert(new Log(CostMonitoringCallback.class.getName(), e.getMessage()));
            }
        }).start();
    }

    private void cleanupUntil(List<Cost> costs) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime oldestDate = now.minus(this.settingsService.getSettings().getCostThresholds().stream().mapToInt(o -> o.getDays()).max().getAsInt(), ChronoUnit.DAYS);
        cleanup(oldestDate, costs);
    }
    private void cleanupOneYear(List<Cost> costs) {
        cleanup(ZonedDateTime.now().minus(1, ChronoUnit.YEARS), costs);
    }
    private void cleanup(ZonedDateTime untilDate, List<Cost> costs) {
        for(Cost rm : costs.stream().filter(o -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(o.getTimestamp()), ZoneId.systemDefault()).isBefore(untilDate)).collect(Collectors.toList())) {
            this.costRepository.deleteById(rm.getIdentifier());
        }
    }

    private List<Cost> createCosts(Collection<ExecutionsEWrapper.Execution> executions) {
        List<Cost> costs = new ArrayList<>();
        for(ExecutionsEWrapper.Execution execution: executions) {
            Instant now = Instant.now();
            Cost cost = new Cost();
            cost.setIdentifier(execution.execution.execId());
            cost.setQuantity(execution.execution.shares());
            cost.setTimestamp(now.toEpochMilli());
            cost.setLocalDateTime(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
            cost.setSymbol(execution.contract.symbol());
            cost.setCurrency(execution.contract.currency());
            cost.setAmount(execution.commissionReport.commission());
            costs.add(cost);
        }
        return costs;
    }

}
