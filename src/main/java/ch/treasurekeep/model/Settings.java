package ch.treasurekeep.model;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents all Settings of the application that can be changed during runtime
 * (The others can be found in the application.properties-file)
 * Has always the id of 1 (Singleton)
 */
public class Settings {

    @Id
    private String id;
    private List<NetvalueThreshold> netvalueThresholds = new ArrayList<>();
    private List<CostThreshold> costThresholds = new ArrayList<>();
    private List<String> managedAccounts = new ArrayList<>();
    private String baseCurrency;
    private boolean reconnecting;

    public Settings() { this.id = "1"; }
    public Settings(Settings settings) {
        this.id = settings.id;
        this.netvalueThresholds.addAll(settings.netvalueThresholds);
        this.costThresholds.addAll(settings.costThresholds);
        this.managedAccounts.addAll(settings.managedAccounts);
        this.reconnecting = settings.reconnecting;
    }

    /**
     * Business rules
     * @throws IllegalStateException
     */
    public List<String> validate() {
        List<String> result = new ArrayList<>();
        if(this.getNetvalueThresholds().stream().map(o -> o.getId()).collect(Collectors.toSet()).size() != this.getNetvalueThresholds().stream().map(o -> o.getId()).collect(Collectors.toList()).size())
            result.add("Double identifier in NetValueThresholds");
        if(this.getCostThresholds().stream().map(o -> o.getId()).collect(Collectors.toSet()).size() != this.getCostThresholds().stream().map(o -> o.getId()).collect(Collectors.toList()).size())
            result.add("Double identifier in CostTresholds");
        if(this.getNetvalueThresholds().stream().filter(o -> o.getPriority() == NetvalueThreshold.Priority.HARD).count() > 1) {
            result.add("Only one hard threshold is allowed"); //this is up 2 discuss
        }
        return result;
    }

    public List<NetvalueThreshold> getNetvalueThresholds() { return netvalueThresholds; }
    public void setNetvalueThresholds(List<NetvalueThreshold> netvalueThresholds) { this.netvalueThresholds = netvalueThresholds; }

    public List<CostThreshold> getCostThresholds() { return costThresholds; }
    public void setCostThresholds(List<CostThreshold> costThresholds) { this.costThresholds = costThresholds; }

    /**
     * Name of the accounts that the service is taking care of
     * (may be empty)
     * @return
     */
    public List<String> getManagedAccounts() { return managedAccounts; }

    /**
     * Name of the accounts that the service is taking care of
     * (may be empty)
     *
     * @param managedAccounts
     */
    public void setManagedAccounts(List<String> managedAccounts) { this.managedAccounts = managedAccounts; }

    /**
     * True if the service is allowed to try to reconnect after disconnected from TWS
     * (the try to reconnect will go infinite)
     * Default is false
     * @return
     */
    public boolean isReconnecting() { return reconnecting; }

    /**
     * True if the service is allowed to try to reconnect after disconnected from TWS
     * (the try to reconnect will go infinite)
     * Default is false
     * @param reconnecting
     */
    public void setReconnecting(boolean reconnecting) { this.reconnecting = reconnecting; }

    /**
     * Currency in which the service will operate
     * It strongly should be the same currency as your account has.
     * Default is CHF
     * @return
     */
    public String getBaseCurrency() { return baseCurrency != null ? baseCurrency : "CHF"; }

    /**
     * Currency in which the service will operate
     * It strongly should be the same currency as your account has.
     * Default is CHF
     * @param baseCurrency
     */
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
}
