package ch.treasurekeep.service.interactivebrokers.ewrappers;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.Log;
import com.ib.client.Contract;
import com.ib.client.DefaultEWrapper;
import com.ib.client.EClientSocket;

import java.util.*;

/**
 * EWrapper to handle AccountUpdates from IB
 * The fire method subscribe at IB for a specific account
 * 1. You can subscribe at this component via the append method -> At every accountDownloadEnd you will receive a summary
 * 2. You can pick up the latest updates at any time
 */
public class AccountUpdateEWrapper extends DefaultEWrapper {

    private final LogRepository logRepository;
    private final List<AccountUpdateCallback> callbacks = new LinkedList<>();

    private final List<AccountUpdate> fetchingAccountUpdates = new ArrayList<>();
    private final Map<String, List<AccountUpdate>> fetchedAccountUpdatesPerAccount = new HashMap();

    private final List<PortfolioUpdate> fetchingPortfolioUpdates = new ArrayList<>();
    private final Map<String, List<PortfolioUpdate>> fetchedPortfolioUpdatesPerAccount = new HashMap();

    private String lastAccount;

    public AccountUpdateEWrapper(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void continuousFire(String account, EClientSocket socket) {
        this.lastAccount = account;
        try{
            socket.reqAccountUpdates(true, account);
        }
        catch (Exception e) {
            logRepository.insert(new Log(AccountUpdateEWrapper.class.getName(), e.getMessage()));
        }
    }
    public void continnousUnfire(EClientSocket socket) {
        try{
            socket.reqAccountUpdates(false, lastAccount);
        }
        catch (Exception e) {
            logRepository.insert(new Log(AccountUpdateEWrapper.class.getName(), e.getMessage()));
        }
    }
    
    public void appendCallback(AccountUpdateCallback callback) {
        synchronized (this.callbacks) {
            this.callbacks.add(callback);
        }
    }

    public void removeCallback(AccountUpdateCallback callback) {
        synchronized (this.callbacks) {
            this.callbacks.remove(callback);
        }
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        try{
            AccountUpdate accountUpdate = new AccountUpdate();
            accountUpdate.accountName = accountName;
            accountUpdate.key = key;
            accountUpdate.currency = currency;
            accountUpdate.value = value;
            this.fetchingAccountUpdates.add(accountUpdate);
        }
        catch (Exception e) {
            logRepository.insert(new Log(AccountUpdateEWrapper.class.getName(), e.getMessage()));
        }
    }

    @Override
    public void updatePortfolio(Contract contract, double position,
                                double marketPrice, double marketValue, double averageCost,
                                double unrealizedPNL, double realizedPNL, String accountName) {
        PortfolioUpdate portfolioUpdate = new PortfolioUpdate();
        portfolioUpdate.contract = contract;
        portfolioUpdate.position = position;
        portfolioUpdate.marketPrice = marketPrice;
        portfolioUpdate.marketValue = marketValue;
        portfolioUpdate.averageCost = averageCost;
        portfolioUpdate.unrealizedPNL = unrealizedPNL;
        portfolioUpdate.realizedPNL = realizedPNL;
        portfolioUpdate.accountName = accountName;
        this.fetchingPortfolioUpdates.add(portfolioUpdate);
    }

    @Override
    public void accountDownloadEnd(String s) {
        try{
            String lastAccount = this.lastAccount;
            synchronized (this.callbacks) {
                synchronized (this.fetchedPortfolioUpdatesPerAccount) {
                    synchronized (this.fetchedPortfolioUpdatesPerAccount) {
                        synchronized (lastAccount) {
                            if (lastAccount == null) {
                                throw new IllegalStateException();
                            }
                            if (this.fetchedAccountUpdatesPerAccount.get(lastAccount) == null) {
                                this.fetchedAccountUpdatesPerAccount.put(lastAccount, new ArrayList<>());
                            }
                            if (this.fetchedPortfolioUpdatesPerAccount.get(lastAccount) == null) {
                                this.fetchedPortfolioUpdatesPerAccount.put(lastAccount, new ArrayList<>());
                            }
                            this.fetchedPortfolioUpdatesPerAccount.put(lastAccount, new ArrayList<>(this.fetchingPortfolioUpdates)); //overwrite existing with clones
                            this.fetchedAccountUpdatesPerAccount.put(lastAccount, new ArrayList<>(this.fetchingAccountUpdates)); //overwrite existing with clones
                            this.fetchingAccountUpdates.clear();
                            this.fetchingPortfolioUpdates.clear();
                            for (AccountUpdateCallback callback : this.callbacks) {
                                callback.callback(new ArrayList<>(
                                        this.fetchedAccountUpdatesPerAccount.get(lastAccount)), 
                                        new ArrayList<>(this.fetchedPortfolioUpdatesPerAccount.get(lastAccount))
                                );
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            logRepository.insert(new Log(AccountUpdateEWrapper.class.getName(), e.getMessage()));
        }
    }

    public List<AccountUpdate> getFetchedAccountUpdates(String accountName) {
        return this.fetchedAccountUpdatesPerAccount.get(accountName) != null ? this.fetchedAccountUpdatesPerAccount.get(accountName): new ArrayList<>();
    }

    public List<PortfolioUpdate> getFetchedPortfolioUpdates(String accountName) {
        return this.fetchedPortfolioUpdatesPerAccount.get(accountName) != null ? this.fetchedPortfolioUpdatesPerAccount.get(accountName): new ArrayList<>();
    }

    public Map<String, List<PortfolioUpdate>> getFetchedPortfolioUpdatesPerAccount() { return fetchedPortfolioUpdatesPerAccount; }

    public static class AccountUpdate {
        public String key;
        public String value;
        public String currency;
        public String accountName;
    }

    public static interface AccountUpdateCallback {
        public void callback(List<AccountUpdate> fetchedAccountUpdates, List<PortfolioUpdate> fetchedPortfolioUpdates);
    }

    public static class PortfolioUpdate {
        public Contract contract;
        public double position;
        public double marketPrice;
        public double marketValue;
        public double averageCost;
        public double unrealizedPNL;
        public double realizedPNL;
        public String accountName;
    }

}
