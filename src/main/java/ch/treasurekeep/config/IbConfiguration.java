package ch.treasurekeep.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration
 * Properties to connect to interactive brokers (TWS or Gateway)
 */
@Component
@ConfigurationProperties("treasurekeep.ib")
@Validated
public class IbConfiguration {

    @NotNull
    private String host;
    @NotNull
    private Integer port;
    @NotNull
    private Integer clientId;

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

}
