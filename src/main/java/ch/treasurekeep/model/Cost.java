package ch.treasurekeep.model;

import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

/**
 * Represents expenses produced at a specific point in time
 */
public class Cost {
    @Id
    private String identifier;
    private String currency;
    private String symbol;
    private LocalDateTime localDateTime;
    private  Double amount;
    private  Double quantity;
    private long timestamp;

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public LocalDateTime getLocalDateTime() { return localDateTime; }
    public void setLocalDateTime(LocalDateTime localDateTime) { this.localDateTime = localDateTime; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

}
