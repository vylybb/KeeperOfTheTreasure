package ch.treasurekeep.config.setup;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is depricated and not in use
 */
@Configuration
public class ChaceConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("currencies");
    }
}