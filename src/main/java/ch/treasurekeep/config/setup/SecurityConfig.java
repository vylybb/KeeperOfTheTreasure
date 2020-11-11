package ch.treasurekeep.config.setup;

import ch.treasurekeep.config.AuthConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;

/**
 * Spring-Security
 * Simple Username/Password notification
 * note: if this makes sense or not depends on the individual case
 * (Where is the server hosted, who can access the network it is running on etc.)
 * In some setup this might be insufficient
 */
@Configuration
@Component
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    private AuthConfiguration ibConfiguration;
    public SecurityConfig(AuthConfiguration ibConfiguration) {
        this.ibConfiguration = ibConfiguration;
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(this.ibConfiguration.getUsername())
                .password("{noop}" + this.ibConfiguration.getPassword())
                .roles("USER");
    }
}
