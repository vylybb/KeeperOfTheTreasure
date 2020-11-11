package ch.treasurekeep.service.interactivebrokers.ewrappers;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.Log;
import ch.treasurekeep.model.NetvalueThreshold;
import ch.treasurekeep.service.NotificationService;
import ch.treasurekeep.service.SettingsService;
import com.ib.client.*;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * EWrapper to handle OpenOrders from IB
 *
 * You can register a callbacks via load
 * The fire method will trigger the a request against IB
 * The next openOrderEnd-event will trigger the delivery of a summary to your callbacks
 * The callback is only notified once
 */
public class OpenOrderEWrapper extends DefaultEWrapper {

    private LogRepository logRepository;
    private final List<OpenOrderCallback> callbacks = new ArrayList<>();

    private Set<OpenOrder> fetchingOpenOrders = new HashSet<>();
    private Set<OpenOrder> fetchedOpenOrders = new HashSet<>();

    private Set<OrderStatus> fetchingOrderStatus = new HashSet<>();
    private Set<OrderStatus> fetchedOrderStatus = new HashSet<>();


    public OpenOrderEWrapper(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void load(OpenOrderCallback callback) {
        synchronized (this.callbacks) {
            this.callbacks.add(callback);
        }
    }
    public void fire(EClientSocket socket) {
        try{
            socket.reqAllOpenOrders();
        }
        catch (Exception e) {
            logRepository.insert(new Log(OpenOrderEWrapper.class.getName(), e.getMessage()));
        }
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        try{
            this.fetchingOpenOrders.add(new OpenOrder(orderId, order, contract));
        }
        catch (Exception e) {
            logRepository.insert(new Log(OpenOrderEWrapper.class.getName(), e.getMessage()));
        }
    }

    @Override
    public void orderStatus(int orderId, String status, double filled,
                            double remaining, double avgFillPrice, int permId, int parentId,
                            double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        try{
            OrderStatus orderStatus = new OrderStatus();
            orderStatus.orderId = orderId;
            orderStatus.status = status;
            orderStatus.filled = filled;
            orderStatus.remaining = remaining;
            orderStatus.avgFillPrice = avgFillPrice;
            orderStatus.permId = permId;
            orderStatus.parentId = parentId;
            orderStatus.lastFillPrice = lastFillPrice;
            orderStatus.clientId = clientId;
            orderStatus.whyHeld = whyHeld;
            orderStatus.mktCapPrice = mktCapPrice;
            this.fetchingOrderStatus.add(orderStatus);
        }
        catch (Exception e) {
            logRepository.insert(new Log(OpenOrderEWrapper.class.getName(), e.getMessage()));
        }
    }

    @Override
    public void openOrderEnd() {
        try{
            this.fetchedOpenOrders.clear();
            this.fetchedOrderStatus.clear();
            this.fetchedOpenOrders.addAll(this.fetchingOpenOrders);
            this.fetchedOrderStatus.addAll(this.fetchedOrderStatus);
            this.fetchingOpenOrders.clear();
            this.fetchingOrderStatus.clear();
            List<OpenOrderCallback> executed = new ArrayList<>();
            for (OpenOrderCallback callback : this.callbacks) {
                callback.callback(this.fetchedOpenOrders, this.fetchedOrderStatus);
                executed.add(callback);
            }
            this.callbacks.removeAll(executed);
        }
        catch (Exception e) {
            logRepository.insert(new Log(OpenOrderEWrapper.class.getName(), e.getMessage()));
        }
    }

    public static interface OpenOrderCallback {
        void callback(Set<OpenOrder> fetchedOpenOrders, Set<OrderStatus> fetchedOrderStatus);
    }

    public static class OrderStatus{
        public int orderId;
        public String status;
        public double filled;
        public double remaining;
        public double avgFillPrice;
        public int permId;
        public int parentId;
        public double lastFillPrice;
        public int clientId;
        public String whyHeld;
        public double mktCapPrice;
    }

    public static class OpenOrder {
        public final int orderId;
        public final Order order;
        public final Contract contract;

        public OpenOrder(int orderId, Order order, Contract contract) {
            this.orderId = orderId;
            this.order = order;
            this.contract = contract;
        }

        public boolean isAffected(NetvalueThreshold threshold) {
            if (
                    threshold.getPriority() == NetvalueThreshold.Priority.HARD
                            &&
                            threshold.isActive()
                            &&
                            threshold.getAccount().equals(this.order.account())
                            &&
                            (!threshold.getIgnoredSymbols().contains(this.contract.symbol()))
            ) {
                return true;
            }
            return false;
        }

        public double getQuantity(Set<OpenPositionEWrapper.Position> positions) {
            //i dont know if we just can use equals on contracts instead
            Set<OpenPositionEWrapper.Position> filteredPositions =  positions.stream()
                    .filter(po -> Objects.equals(po.account, this.order.account()))
                    .filter(po -> Objects.equals(po.contract.symbol(), this.contract.symbol()))
                    .filter(po -> Objects.equals(po.contract.exchange(), this.contract.exchange()))
                    .filter(po -> Objects.equals(po.contract.currency(), this.contract.currency()))
                    .collect(Collectors.toSet());
            if(filteredPositions.size() != 1) {
                throw new IllegalStateException("One hit was expected but received " + filteredPositions.size());
            }
            return filteredPositions.iterator().next().pos;
        }
    }
}
