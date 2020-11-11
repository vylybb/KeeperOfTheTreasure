package ch.treasurekeep.model;

import org.springframework.data.annotation.Id;

/**
 * Threshold how much costs are accepted to occur in a specific period.
 * The period is in number of days from NOW
 * The amount is in base-currency
 */
public class CostThreshold {

    @Id
    private String id;
    private int days;
    private  double amount;

    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Object getId() { return id; }
    public void setId(String id) { this.id = id; }
}