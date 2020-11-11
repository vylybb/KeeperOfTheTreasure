package ch.treasurekeep.service.interactivebrokers.ewrappers;

import com.ib.client.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Composite-Pattern for EWrappers
 * Was introduced to split the functionality that belongs here into smaller sub-components
 */
public class EWrapperComposite implements EWrapper {
    private List<EWrapper> listeners = new ArrayList<>();

    public void addListener(EWrapper wrapper) {
        if(!(listeners.contains(wrapper))) { listeners.add(wrapper); }
    }
    public List<EWrapper> getListeners() { return listeners; }
    public void removeListener(EWrapper wrapper) {
        this.listeners.remove(wrapper);
    }

    @Override
    public void tickPrice(int i, int i1, double v, TickAttrib tickAttrib) {
        for (EWrapper listener : listeners) {
            listener.tickPrice(i, i1, v, tickAttrib);
        }
    }

    @Override
    public void tickSize(int i, int i1, int i2) {
        for (EWrapper listener : listeners) {
            listener.tickSize(i, i1, i2);
        }
    }

    @Override
    public void tickOptionComputation(int i, int i1, double v, double v1, double v2, double v3, double v4, double v5, double v6, double v7) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickOptionComputation(i, i1, v, v1, v2, v3, v4, v5, v6, v7);
        }
    }

    @Override
    public void tickGeneric(int i, int i1, double v) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickGeneric(i, i1, v);
        }
    }

    @Override
    public void tickString(int i, int i1, String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickString(i, i1, s);
        }
    }

    @Override
    public void tickEFP(int i, int i1, double v, String s, double v1, int i2, String s1, double v2, double v3) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickEFP(i, i1, v, s,v1, i2, s1,v2, v3);
        }
    }

    @Override
    public void orderStatus(int orderId, String status, double filled,
                            double remaining, double avgFillPrice, int permId, int parentId,
                            double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.orderStatus(orderId, status, filled,
                    remaining, avgFillPrice, permId, parentId,
                    lastFillPrice, clientId, whyHeld, mktCapPrice);
        }
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.openOrder(orderId, contract, order, orderState);
        }
    }

    @Override
    public void openOrderEnd() {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.openOrderEnd();
        }
    }

    @Override
    public void updateAccountValue(String s, String s1, String s2, String s3) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.updateAccountValue(s, s1, s2, s3);
        }
    }

    @Override
    public void updatePortfolio(Contract contract, double v, double v1, double v2, double v3, double v4, double v5, String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.updatePortfolio(contract, v, v1, v2, v3, v4, v5, s);
        }
    }

    @Override
    public void updateAccountTime(String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.updateAccountTime(s);
        }
    }

    @Override
    public void accountDownloadEnd(String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.accountDownloadEnd(s);
        }
    }

    @Override
    public void nextValidId(int i) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.nextValidId(i);
        }
    }

    @Override
    public void contractDetails(int i, ContractDetails contractDetails) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.contractDetails(i, contractDetails);
        }
    }

    @Override
    public void bondContractDetails(int i, ContractDetails contractDetails) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.bondContractDetails(i, contractDetails);
        }
    }

    @Override
    public void contractDetailsEnd(int i) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.contractDetailsEnd(i);
        }
    }

    @Override
    public void execDetails(int i, Contract contract, Execution execution) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.execDetails(i, contract, execution);
        }
    }

    @Override
    public void execDetailsEnd(int i) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.execDetailsEnd(i);
        }
    }

    @Override
    public void updateMktDepth(int i, int i1, int i2, int i3, double v, int i4) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.updateMktDepth(i, i1, i2, i3, v, i4);
        }
    }

    @Override
    public void updateMktDepthL2(int i, int i1, String s, int i2, int i3, double v, int i4, boolean b) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.updateMktDepthL2(i, i1, s, i2, i3, v, i4, b);
        }
    }

    @Override
    public void updateNewsBulletin(int i, int i1, String s, String s1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.updateNewsBulletin(i, i1, s, s1);
        }
    }

    @Override
    public void managedAccounts(String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.managedAccounts(s);
        }
    }

    @Override
    public void receiveFA(int i, String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.receiveFA(i, s);
        }
    }

    @Override
    public void historicalData(int i, Bar bar) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.historicalData(i, bar);
        }
    }

    @Override
    public void scannerParameters(String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.scannerParameters(s);
        }
    }

    @Override
    public void scannerData(int i, int i1, ContractDetails contractDetails, String s, String
            s1, String s2, String s3) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.scannerData(i, i1, contractDetails, s, s1, s2, s3);
        }
    }

    @Override
    public void scannerDataEnd(int i) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.scannerDataEnd(i);
        }
    }

    @Override
    public void realtimeBar(int i, long l, double v, double v1, double v2, double v3, long l1,
                            double v4, int i1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.realtimeBar(i, l, v, v1, v2, v3, l1, v4, i1);

        }
    }

    @Override
    public void currentTime(long l) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.currentTime(l);

        }
    }

    @Override
    public void fundamentalData(int i, String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.fundamentalData(i, s);
        }
    }

    @Override
    public void deltaNeutralValidation(int i, DeltaNeutralContract
            deltaNeutralContract) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.deltaNeutralValidation(i, deltaNeutralContract);

        }
    }

    @Override
    public void tickSnapshotEnd(int i) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickSnapshotEnd(i);
        }
    }

    @Override
    public void marketDataType(int i, int i1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.marketDataType(i, i1);
        }
    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.commissionReport(commissionReport);
        }
    }

    @Override
    public void position(String s, Contract contract, double v,
                         double v1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.position(s, contract, v, v1);
        }
    }

    @Override
    public void positionEnd() {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.positionEnd();
        }
    }

    @Override
    public void accountSummary(int i, String s, String
            s1, String s2, String s3) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.accountSummary(i, s, s1, s2, s3);
        }
    }

    @Override
    public void accountSummaryEnd(int i) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.accountSummaryEnd(i);
        }
    }

    @Override
    public void verifyMessageAPI(String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.verifyMessageAPI(s);
        }
    }

    @Override
    public void verifyCompleted(
            boolean b, String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.verifyCompleted(b, s);
        }
    }

    @Override
    public void verifyAndAuthMessageAPI
            (String s, String s1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.verifyAndAuthMessageAPI(s, s1);
        }
    }

    @Override
    public void verifyAndAuthCompleted(
            boolean b, String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.verifyAndAuthCompleted(b, s);
        }
    }

    @Override
    public void displayGroupList(
            int i, String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.displayGroupList(i, s);
        }
    }

    @Override
    public void displayGroupUpdated
            (int i, String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.displayGroupUpdated(i, s);

        }
    }

    @Override
    public void error(Exception e) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.error(e);

        }
    }

    @Override
    public void error
            (String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.error(s);
        }
    }

    @Override
    public void error
            (int i,
             int i1, String s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.error(i, i1, s);
        }
    }

    @Override
    public void connectionClosed
            () {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.connectionClosed();
        }
    }

    @Override
    public void connectAck
            () {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.connectAck();
        }
    }

    @Override
    public void positionMulti
            (int i, String s, String s1, Contract contract,
             double v,
             double v1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.positionMulti(i, s, s1, contract, v, v1);
        }
    }

    @Override
    public void positionMultiEnd
            (
                    int i) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.positionMultiEnd(i);
        }
    }

    @Override
    public void accountUpdateMulti
            (
                    int i, String
                    s, String
                            s1, String
                            s2, String
                            s3, String
                            s4) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.accountUpdateMulti(i, s, s1, s2, s3, s4);
        }
    }

    @Override
    public void accountUpdateMultiEnd
            (
                    int i) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.accountUpdateMultiEnd(i);
        }
    }

    @Override
    public void securityDefinitionOptionalParameter
            (
                    int i, String
                    s,
                    int i1, String
                            s1, String
                            s2, Set<String> set, Set<Double> set1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.securityDefinitionOptionalParameter(i, s, i1, s1, s2, set, set1);
        }
    }

    @Override
    public void securityDefinitionOptionalParameterEnd
            (
                    int i) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.securityDefinitionOptionalParameterEnd(i);

        }
    }

    @Override
    public void softDollarTiers
            (
                    int i, SoftDollarTier[]
                    softDollarTiers) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.softDollarTiers(i, softDollarTiers);
        }
    }

    @Override
    public void familyCodes
            (FamilyCode[]
                     familyCodes) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.familyCodes(familyCodes);

        }
    }

    @Override
    public void symbolSamples
            (
                    int i, ContractDescription[]
                    contractDescriptions) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.symbolSamples(i, contractDescriptions);
        }
    }

    @Override
    public void historicalDataEnd
            (
                    int i, String
                    s, String
                            s1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.historicalDataEnd(i, s, s1);
        }
    }

    @Override
    public void mktDepthExchanges
            (DepthMktDataDescription[]
                     depthMktDataDescriptions) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.mktDepthExchanges(depthMktDataDescriptions);

        }
    }

    @Override
    public void tickNews
            (
                    int i,
                    long l, String
                            s, String
                            s1, String
                            s2, String
                            s3) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickNews(i, l, s, s1, s2, s3);
        }
    }

    @Override
    public void smartComponents
            (
                    int i, Map<
                    Integer, Map.Entry<String, Character>> map) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.smartComponents(i, map);

        }
    }

    @Override
    public void tickReqParams
            (
                    int i,
                    double v, String
                            s,
                    int i1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickReqParams(i, v, s, i1);
        }
    }

    @Override
    public void newsProviders
            (NewsProvider[]
                     newsProviders) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.newsProviders(newsProviders);

        }
    }

    @Override
    public void newsArticle
            (
                    int i,
                    int i1, String
                            s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.newsArticle(i, i1, s);

        }
    }

    @Override
    public void historicalNews
            (
                    int i, String
                    s, String
                            s1, String
                            s2, String
                            s3) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.historicalNews(i, s, s1, s2, s3);

        }
    }

    @Override
    public void historicalNewsEnd
            (
                    int i,
                    boolean b) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.historicalNewsEnd(i, b);

        }
    }

    @Override
    public void headTimestamp
            (
                    int i, String
                    s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.headTimestamp(i, s);

        }
    }

    @Override
    public void histogramData
            (
                    int i, List<
                    HistogramEntry> list) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.histogramData(i, list);

        }
    }

    @Override
    public void historicalDataUpdate
            (
                    int i, Bar
                    bar) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.historicalDataUpdate(i, bar);

        }
    }

    @Override
    public void rerouteMktDataReq
            (
                    int i,
                    int i1, String
                            s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.rerouteMktDataReq(i, i1, s);

        }
    }

    @Override
    public void rerouteMktDepthReq
            (
                    int i,
                    int i1, String
                            s) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.rerouteMktDepthReq(i, i1, s);

        }
    }

    @Override
    public void marketRule
            (
                    int i, PriceIncrement[]
                    priceIncrements) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.marketRule(i, priceIncrements);

        }

    }

    @Override
    public void pnl
            (
                    int i,
                    double v,
                    double v1,
                    double v2) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.pnl(i, v, v1, v2);

        }
    }

    @Override
    public void pnlSingle
            (
                    int i,
                    int i1,
                    double v,
                    double v1,
                    double v2,
                    double v3) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.pnlSingle(i, i1, v, v1, v2, v3);

        }
    }

    @Override
    public void historicalTicks
            (
                    int i, List<
                    HistoricalTick> list,
                    boolean b) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.historicalTicks(i, list, b);

        }
    }

    @Override
    public void historicalTicksBidAsk
            (
                    int i, List<
                    HistoricalTickBidAsk> list,
                    boolean b) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.historicalTicksBidAsk(i, list, b);

        }
    }

    @Override
    public void historicalTicksLast
            (
                    int i, List<
                    HistoricalTickLast> list,
                    boolean b) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.historicalTicksLast(i, list, b);

        }
    }

    @Override
    public void tickByTickAllLast
            (
                    int i,
                    int i1,
                    long l,
                    double v,
                    int i2, TickAttribLast
                            tickAttribLast, String
                            s, String
                            s1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickByTickAllLast(i, i1, l, v, i2, tickAttribLast, s, s1);

        }
    }

    @Override
    public void tickByTickBidAsk
            (
                    int i,
                    long l,
                    double v,
                    double v1,
                    int i1,
                    int i2, TickAttribBidAsk
                            tickAttribBidAsk) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickByTickBidAsk(i, l, v, v1, i1, i2, tickAttribBidAsk);

        }
    }

    @Override
    public void tickByTickMidPoint
            (
                    int i,
                    long l,
                    double v) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.tickByTickMidPoint(i, l, v);

        }
    }

    @Override
    public void orderBound
            (
                    long l,
                    int i,
                    int i1) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.orderBound(l, i, i1);

        }
    }

    @Override
    public void completedOrder
            (Contract
                     contract, Order
                     order, OrderState
                     orderState) {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.completedOrder(contract, order, orderState);
        }
    }

    @Override
    public void completedOrdersEnd() {
        for (EWrapper listener : new ArrayList<>(this.listeners)) {
            listener.completedOrdersEnd();
        }
    }

    public void clear() {
        this.listeners.clear();
    }

}
