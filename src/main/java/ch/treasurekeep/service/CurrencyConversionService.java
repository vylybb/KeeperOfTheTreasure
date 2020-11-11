package ch.treasurekeep.service;

import ch.treasurekeep.config.CurrencyConversionConfiguration;
import ch.treasurekeep.service.interactivebrokers.CurrencyConversionCallback;
import ch.treasurekeep.service.interactivebrokers.CurrencyConversionKt;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Responsible for converting one currency to another
 * Updated twice a day
 * (Shall be replaced with calls to TWS eventually) //TODO
 */
@Component
@Service
public class CurrencyConversionService {

    private final CurrencyConversionConfiguration configuration;
    private final Map<CurrencyPair, Double> cache = new HashMap<>();

    public CurrencyConversionService(CurrencyConversionConfiguration configuration) {
        this.configuration = configuration;
    }

    @Scheduled(fixedRate = 1000*60*60*12)
    public void dropCache() {
        this.cache.clear();
    }

    public Double convert(CurrencyPair currencyPair) {
        Double cached = this.cache.get(currencyPair);
        if(cached == null) {
            this.cache.put(currencyPair, this.convert(Set.of(currencyPair)).get(currencyPair));
        }
        return this.cache.get(currencyPair);
    }

    /**
     * Call the Kotlin-Procedure
     * Transform the Asynchron nature to Synchron
     * @param currencies
     * @return
     */
    private Map<CurrencyPair, Double> convert(final Set<CurrencyPair> currencies) {
        Map<CurrencyPair, Double> result = new HashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(currencies.size());
        for(CurrencyPair pair : currencies) {
            CurrencyConversionKt.convertCurrency(pair.fromCurrency, pair.toCurrency, 1, configuration.getApiKey(),
                    new CurrencyConversionCallback()
                    {
                        @Override
                        public void calculated(double value) {
                            result.put(pair, value);
                            countDownLatch.countDown();
                        }

                        @Override
                        public void error() {
                            countDownLatch.countDown();
                        }
            });
        }
        try{ countDownLatch.await(); }
        catch(InterruptedException e) { Thread.currentThread().interrupt(); }
        return result;

    }

    public static class CurrencyPair {
        public final String fromCurrency;
        public final String toCurrency;
        public CurrencyPair(String fromCurrency, String toCurrency) {
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CurrencyPair that = (CurrencyPair) o;
            return Objects.equals(fromCurrency, that.fromCurrency) &&
                    Objects.equals(toCurrency, that.toCurrency);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fromCurrency, toCurrency);
        }
    }

}
