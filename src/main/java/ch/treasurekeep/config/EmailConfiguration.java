package ch.treasurekeep.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration
 * Properties to install notification via e-mail
 */
@Component
@ConfigurationProperties("treasurekeep.mail")
@Validated
public class EmailConfiguration {
    private String password;
    @NotNull
    private String address;
    @NotNull
    private String hostname;
    @NotNull
    private String port;
    @NotNull
    private String authMethods;
    @NotNull
    private String tlsEnabled;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }

    public String getAuthMethods() { return authMethods; }
    public void setAuthMethods(String authMethods) { this.authMethods = authMethods; }

    public String getTlsEnabled() { return tlsEnabled; }
    public void setTlsEnabled(@NotNull String tlsEnabled) { this.tlsEnabled = tlsEnabled; }

}
