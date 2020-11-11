package ch.treasurekeep.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration of the connection to exchangerate-api.com
 */
@Component
@ConfigurationProperties("treasurekeep.currencyconversion")
@Validated
public class CurrencyConversionConfiguration {

    @NotNull
    private String apiKey;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}
