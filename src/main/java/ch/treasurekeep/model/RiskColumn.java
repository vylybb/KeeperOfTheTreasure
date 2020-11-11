package ch.treasurekeep.model;

/**
 * Represents a Column displayed in the web-ui
 * It is not persisted in MongoDB
 * Gives a overview about the height of the current risk of a portfolio
 * and its distribution over individual positions
 */
public class RiskColumn {

    public String symbol;
    public String account;
    public String exchange;
    public Double stop;
    public Double quantity;
    public String currency;
    public Double profit;
    public Double current;
    public Double marketValue;
    public Double riskInBasecurrency;
    public Double riskInPercentage;
    public Double percentageOfTotalRisk;
    public Double avgPrice;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public Double getStop() { return stop; }
    public void setStop(Double stop) { this.stop = stop; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Double getProfit() { return profit; }
    public void setProfit(Double profit) { this.profit = profit; }

    public Double getCurrent() { return current; }
    public void setCurrent(Double current) {this.current = current; }

    public Double getMarketValue() { return marketValue; }
    public void setMarketValue(Double marketValue) { this.marketValue = marketValue; }

    public Double getRiskInBasecurrency() { return riskInBasecurrency; }
    public void setRiskInBasecurrency(Double riskInBasecurrency) { this.riskInBasecurrency = riskInBasecurrency; }

    public Double getRiskInPercentage() { return riskInPercentage; }
    public void setPercentageOfTotalRisk(Double percentageOfTotalRisk) { this.percentageOfTotalRisk = percentageOfTotalRisk; }

    public Double getPercentageOfTotalRisk() { return percentageOfTotalRisk; }
    public void setRiskInPercentage(Double riskInPercentage) {this.riskInPercentage = riskInPercentage; }

    public Double getAvgPrice() { return avgPrice; }
    public void setAvgPrice(Double avgPrice) { this.avgPrice = avgPrice; }
}
