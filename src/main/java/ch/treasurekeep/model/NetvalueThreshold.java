package ch.treasurekeep.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * Represents a Threshold that is breached whenever the Netto-Liquidation-Value is getting below it
 * A hard breach will trigger a emergency-exit
 * A soft breach will only trigger a message
 * After a Threshold was reached, it will be deactivated
 * It is possible to exclude certain entries from a hard exit
 * Only one breach is allowed to be active at any time
 */
public class NetvalueThreshold {
    public enum Priority {HARD, SOFT}

    @Id
    private String id;
    @NotNull
    private String account;
    @NotNull
    private double threshold;
    @NotNull
    private Priority priority;

    private List<String> ignoredSymbols;

    private boolean active = true;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public Priority getPriority() { return priority;}
    public void setPriority(Priority priority) { this.priority = priority; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<String> getIgnoredSymbols() { return this.ignoredSymbols; }
    public void setIgnoredSymbols(List<String> ignoredSymbols) { this.ignoredSymbols = ignoredSymbols; }
}