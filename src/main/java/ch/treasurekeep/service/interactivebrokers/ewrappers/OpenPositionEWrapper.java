package ch.treasurekeep.service.interactivebrokers.ewrappers;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.Log;
import com.ib.client.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * EWrapper to handle Positions from IB
 *
 * You can register a callbacks via load
 * The fire method will trigger the a request against IB
 * The next positionEnd-event will trigger the delivery of a summary to your callbacks
 * The callback is only notified once
 */
public class OpenPositionEWrapper extends DefaultEWrapper {

    private final LogRepository logRepository;
    private final List<OpenPositionCallback> callbacks = new ArrayList<>();

    private final Set<Position> fetchingPositions = new HashSet<>();
    private final Set<Position> fetchedPositions = new HashSet<>();

    public OpenPositionEWrapper(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void load(OpenPositionCallback callback) {
        synchronized (this.callbacks) {
            this.callbacks.add(callback);
        }
    }
    public void fire(EClientSocket socket) {
        try{
            socket.reqPositions();
        }
        catch (Exception e) {
            this.logRepository.insert(new Log(OpenPositionEWrapper.class.getName(), e.getMessage()));
        }
    }

    @Override
    public void position(String s, Contract contract, double pos, double avgPrice) {
        try{
            fetchingPositions.add(new Position(s, contract, pos, avgPrice));
        }
        catch (Exception e) {
            this.logRepository.insert(new Log(OpenPositionEWrapper.class.getName(), e.getMessage()));
        }
    }
    @Override
    public void positionEnd() {
        try {
            synchronized (this.callbacks) {
                fetchedPositions.clear();
                fetchedPositions.addAll(fetchingPositions);
                fetchingPositions.clear();
                for (OpenPositionCallback callback : this.callbacks) {
                    callback.callback(new HashSet<>(this.fetchedPositions));
                }
                this.callbacks.clear();
            }
        }
        catch (Exception e) {
            this.logRepository.insert(new Log(OpenPositionEWrapper.class.getName(), e.getMessage()));
        }
    }

    public static class Position {
        public final String account;
        public final Contract contract;
        public final double pos;
        public final double avgPrice;
        public Position(String account, Contract contract, double pos, double avgPrice) {
            this.account = account;
            this.contract = contract;
            this.pos = pos;
            this.avgPrice = avgPrice;
        }
    }

    public interface OpenPositionCallback {
        public void callback(Set<Position> position);
    }
}
