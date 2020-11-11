package ch.treasurekeep.service.interactivebrokers.ewrappers;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.Log;
import com.ib.client.*;

import java.util.*;

/**
 * EWrapper to handle Executions from IB
 *
 * You can register a callbacks via load
 * The fire method will trigger the a request against IB
 * The next execDetailsEnd-event will trigger the delivery of a summary to your callbacks
 * The callback is only notified once
 */
public class ExecutionsEWrapper extends DefaultEWrapper {

    private LogRepository logRepository;
    private final List<ExecutionsCallback> callbacks = new ArrayList<>(); 

    public ExecutionsEWrapper(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    private Map<String, Execution> fetchingCosts = new HashMap<>();
    private Map<String, Execution> fetchedCosts = new HashMap<>();

    public void load(ExecutionsCallback callback) {
        synchronized (this.callbacks) {
            this.callbacks.add(callback);
        }
    }
    public void fire(EClientSocket socket) {
        try{
            socket.reqExecutions(10001, new ExecutionFilter());
        }
        catch (Exception e) {
            logRepository.insert(new Log(ExecutionsEWrapper.class.getName(), e.getMessage()));
        }
    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {
        try{
            if(this.fetchingCosts.get(commissionReport.execId()) == null) {
                this.fetchedCosts.put(commissionReport.execId(), new Execution());
            }
            this.fetchedCosts.get(commissionReport.execId()).commissionReport = commissionReport;
        }
        catch (Exception e) {
            logRepository.insert(new Log(ExecutionsEWrapper.class.getName(), e.getMessage()));
        }
    }

    @Override
    public void execDetails(int reqId, Contract contract, com.ib.client.Execution execution) {
        try{
            if(this.fetchingCosts.get(execution.execId()) == null) {
                this.fetchingCosts.put(execution.execId(), new Execution());
            }
            this.fetchedCosts.get(execution.execId()).reqId = reqId;
            this.fetchedCosts.get(execution.execId()).contract = contract;
            this.fetchedCosts.get(execution.execId()).execution= execution;
        }
        catch (Exception e) {
            logRepository.insert(new Log(ExecutionsEWrapper.class.getName(), e.getMessage()));
        }
    }

    @Override
    public void execDetailsEnd(int i) {
        try {
            synchronized (this.callbacks) {
                this.fetchedCosts.clear();
                this.fetchedCosts.putAll(this.fetchedCosts);
                this.fetchingCosts.clear();
                for (ExecutionsCallback callback : this.callbacks) {
                    callback.callback(new ArrayList(this.fetchedCosts.values()));
                }
                this.callbacks.clear();
            }
        }
        catch (Exception e) {
            logRepository.insert(new Log(ExecutionsEWrapper.class.getName(), e.getMessage()));
        }
    }

    public class Execution {
        public int reqId;
        public Contract contract;
        public com.ib.client.Execution execution;
        public CommissionReport commissionReport;
    }

    public static interface ExecutionsCallback {
        public void callback(Collection<Execution> executions);
    }

}
