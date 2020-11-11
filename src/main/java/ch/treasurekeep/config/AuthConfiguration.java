package ch.treasurekeep.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration
 * Security-Credentials to use the REST-API
 */
@Component
@ConfigurationProperties("treasurekeep.auth")
@Validated
public class AuthConfiguration {
    @NotNull
    private String username;
    @NotNull
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
