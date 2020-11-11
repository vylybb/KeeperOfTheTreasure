package ch.treasurekeep.service.interactivebrokers.callbacks;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.RiskColumn;
import ch.treasurekeep.service.CurrencyConversionService;
import ch.treasurekeep.service.SettingsService;
import ch.treasurekeep.service.interactivebrokers.ewrappers.AccountUpdateEWrapper;
import ch.treasurekeep.service.interactivebrokers.ewrappers.OpenOrderEWrapper;
import ch.treasurekeep.service.interactivebrokers.ewrappers.OpenPositionEWrapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Produces Risk-Reports
 * Needs to be called from outside
 * Depends on OpenOrder, OpenPosition as well as on AccountUpdates
 * AccountUpdates are received through consulting the last execution
 * Remember that this can result in not all accounts being respected at the beginning of the application
 * (Due to limitations on the IB-Interface)
 */
public class PortfolioRiskStatusCallback implements OpenOrderEWrapper.OpenOrderCallback, OpenPositionEWrapper.OpenPositionCallback {

    private volatile List<OpenOrderEWrapper.OpenOrder> openOrders = null;
    private volatile List<OpenPositionEWrapper.Position> positions = null;

    private final CurrencyConversionService currencyDownloader;
    private final SettingsService settingsService;
    private final LogRepository logRepository;
    private final AccountUpdateEWrapper accountUpdateEWrapper;

    private Callback callback;

    public PortfolioRiskStatusCallback(CurrencyConversionService currencyDownloader, SettingsService settingsService, LogRepository logRepository, AccountUpdateEWrapper accountUpdateEWrapper, Callback callback) {
        this.currencyDownloader = currencyDownloader;
        this.settingsService = settingsService;
        this.logRepository = logRepository;
        this.accountUpdateEWrapper = accountUpdateEWrapper;
        this.callback = callback;

    }

    @Override
    public void callback(Set<OpenOrderEWrapper.OpenOrder> fetchedOpenOrders, Set<OpenOrderEWrapper.OrderStatus> fetchedOrderStatus) {
        this.openOrders = new ArrayList<>();
        this.openOrders.addAll(fetchedOpenOrders);
        if(this.openOrders != null && this.positions != null) {
            try{
                callback.callback(produceResult());
            }
            catch (Exception e) {
                callback.error(e.getMessage());
            }
        }
    }

    @Override
    public void callback(Set<OpenPositionEWrapper.Position> position) {
        this.positions = new ArrayList<>();
        this.positions.addAll(position);
        if(this.openOrders != null && this.positions != null) {
            try{
                callback.callback(produceResult());
            }
            catch (Exception e) {
                callback.error(e.getMessage());
            }
        }
    }

    private List<RiskColumn> produceResult() {
        List<RiskColumn> result = new ArrayList<>();
        String baseCurrency = this.settingsService.getSettings().getBaseCurrency();
        List<String> accounts = this.settingsService.getSettings().getManagedAccounts();
        Map<String, List<AccountUpdateEWrapper.PortfolioUpdate>> portfolioPerAccount = this.accountUpdateEWrapper.getFetchedPortfolioUpdatesPerAccount();

        List<Instrument> instruments = createInstruments(this.positions).stream().filter(o -> accounts.contains(o.account)).collect(Collectors.toList());
        Map<String, List<OpenOrderEWrapper.OpenOrder>> stopOrders = getStopOrdersPerContractId(this.openOrders);
        Map<Instrument, AccountUpdateEWrapper.PortfolioUpdate> portfolioUpdates = createPortfolioUpdatesPerInstrument(instruments, portfolioPerAccount);

        for(Instrument instrument : instruments) {
            List<RiskColumn> riskColumns = new ArrayList<>();
            for(OpenOrderEWrapper.OpenOrder oo : (stopOrders.get(instrument.contractId) != null ? stopOrders.get(instrument.contractId) : new ArrayList<OpenOrderEWrapper.OpenOrder>())) { //create a column for every stop order
                RiskColumn col = new RiskColumn();
                col.setQuantity(oo.order.totalQuantity());
                col.setCurrency(oo.contract.currency());
                col.setAccount(oo.order.account());
                col.setSymbol(oo.contract.symbol());
                col.setStop(oo.order.trailStopPrice());
                col.setExchange(oo.contract.exchange());
                riskColumns.add(col);
            }
            double restQuantity = instrument.quantity - riskColumns.stream().mapToDouble(o -> o.getQuantity()).sum();

            if(restQuantity != 0) { //These columns are placeholders for everything that is not covered by stops. stop is 0
                RiskColumn col = new RiskColumn();
                col.setQuantity(restQuantity);        col.setStop(0.0);
                col.setSymbol(instrument.symbol);     col.setAccount(instrument.account);
                col.setExchange(instrument.exchange != null ? instrument.exchange : "?"); col.setCurrency(instrument.currency);
                riskColumns.add(col);
            }
            for(RiskColumn col : riskColumns) { //enhance the riskcolumns with data from porfrolioUpdates
                AccountUpdateEWrapper.PortfolioUpdate portfolioUpdate = portfolioUpdates.get(instrument);
                col.setAvgPrice(instrument.avgPrice);
                col.setCurrent(portfolioUpdate != null ? portfolioUpdate.marketPrice : null);
                col.setProfit(portfolioUpdate != null ? portfolioUpdate.unrealizedPNL : null);
                col.setMarketValue(portfolioUpdate != null ? portfolioUpdate.marketValue : null);
                col.setRiskInBasecurrency(portfolioUpdate != null ? (portfolioUpdate.marketPrice - col.getStop()) * col.getQuantity() * currencyDownloader.convert(new CurrencyConversionService.CurrencyPair(col.getCurrency(), baseCurrency)): null);
                col.setRiskInPercentage(col.getRiskInBasecurrency() != null ? 100 * col.getRiskInBasecurrency() / (col.getMarketValue() * currencyDownloader.convert(new CurrencyConversionService.CurrencyPair(col.getCurrency(), baseCurrency))) : null);
                result.add(col);
            }
        }
        result.addAll(createTotalColumn(result));
        return sort(result);
    }

    private Map<Instrument, AccountUpdateEWrapper.PortfolioUpdate>createPortfolioUpdatesPerInstrument(List<Instrument> instruments, Map<String, List<AccountUpdateEWrapper.PortfolioUpdate>> riskColumnsPerAccount) {
        Map<Instrument, AccountUpdateEWrapper.PortfolioUpdate> result = new HashMap<>();
        List<AccountUpdateEWrapper.PortfolioUpdate> allPortfolioUpdates = new ArrayList<>();
        for(String key : riskColumnsPerAccount.keySet() ) {
            allPortfolioUpdates.addAll(riskColumnsPerAccount.get(key));
        }
        for(Instrument instrument: instruments) {
            List<AccountUpdateEWrapper.PortfolioUpdate> updates = allPortfolioUpdates.stream()
                    .filter(o -> o.accountName.equals(Objects.requireNonNull(instrument.account)))
                    .filter(o -> o.contract.symbol().equals(Objects.requireNonNull(instrument.symbol)))
                    .filter(o -> o.contract.currency().equals(Objects.requireNonNull(instrument.currency)))
                    //nullpointers.filter(o -> o.contract.exchange().equals(Objects.requireNonNull(instrument.exchange)))
                    .collect(Collectors.toList());
            if(updates.size() != 1) {
                throw new IllegalStateException();
            }
            result.put(instrument, updates.get(0));
        }
        return result;
    }

    private List<RiskColumn> createTotalColumn(List<RiskColumn> riskColumns) {
        String baseCurrency = this.settingsService.getSettings().getBaseCurrency();
        List<RiskColumn> result = new ArrayList<>();
        for(String account : this.settingsService.getSettings().getManagedAccounts()) {
            double totalRiskInBasecurrency = riskColumns.stream().filter(o -> o.getAccount().equals(account)).mapToDouble(o -> o.getRiskInBasecurrency() != null ? o.getRiskInBasecurrency():0).sum();
            riskColumns.stream()
                    .filter(a -> a.getAccount().equals(account) && a.getRiskInBasecurrency() != null)
                    .forEach(o -> o.setPercentageOfTotalRisk(o.getRiskInBasecurrency() / totalRiskInBasecurrency * 100));
            RiskColumn totalRiskColumn = new RiskColumn();
            totalRiskColumn.setAccount(account);
            totalRiskColumn.setCurrency(baseCurrency);
            totalRiskColumn.setRiskInBasecurrency(totalRiskInBasecurrency);
            result.add(totalRiskColumn);
        }
        return result;
    }

    private List<RiskColumn> sort(List<RiskColumn> input) {
        List<RiskColumn> result = new ArrayList<>();
        for(String account : this.settingsService.getSettings().getManagedAccounts()) {
            for(String symbol : input.stream()
                    .filter(o -> o.getSymbol() != null)
                    .sorted(Comparator.comparingDouble(RiskColumn::getRiskInBasecurrency))
                    .map(o -> o.getSymbol())
                    .collect(Collectors.toSet())) {
                result.addAll(input.stream().filter(o -> o.getAccount().equals(account) && o.getSymbol() != null && o.getSymbol().equals(symbol)).collect(Collectors.toList()));
            }
            result.addAll(input.stream().filter(o -> o.getSymbol() == null && o.getAccount().equals(account)).collect(Collectors.toList()));
        }
        return result;
    }


    private Map<String, List<OpenOrderEWrapper.OpenOrder>> getStopOrdersPerContractId(List<OpenOrderEWrapper.OpenOrder> openorders) {
        Map<String, List<OpenOrderEWrapper.OpenOrder>> result = new HashMap<>();

        for(OpenOrderEWrapper.OpenOrder oo : openorders) {
            if(oo.order.action().name().toUpperCase().equals("SELL") && List.of("STP", "STP LMT", "STP PRT", "TRAIL", "TRAIL LIMIT").contains(oo.order.getOrderType())) {
                if(!result.containsKey(Integer.toString(oo.contract.conid()))) {
                    result.put(Integer.toString(oo.contract.conid()), new ArrayList<>());
                }
                result.get(Integer.toString(oo.contract.conid())).add(oo);
            }
        }
        return result;
    }

    private List<Instrument> createInstruments(List<OpenPositionEWrapper.Position> positions) {
        List<Instrument> result = new ArrayList<>();
        for(OpenPositionEWrapper.Position pos : positions) {
            Instrument instrument = new Instrument();
            instrument.account = pos.account;
            instrument.symbol = pos.contract.symbol();
            instrument.exchange = pos.contract.exchange();
            instrument.quantity = pos.pos;
            instrument.avgPrice = pos.avgPrice;
            instrument.currency = pos.contract.currency();
            instrument.contractId = Integer.toString(pos.contract.conid());
            result.add(instrument);
        }
        return result;
    }

    private static class Instrument {
        public String symbol;
        public String exchange;
        public String account;
        public Double quantity;
        public String currency;
        public Double avgPrice;
        public String contractId;
    }

    public static interface Callback {
        public void callback(List<RiskColumn> result);
        public void error(String message);
    }

}
