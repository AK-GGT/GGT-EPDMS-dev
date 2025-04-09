package de.iai.ilcd.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author MK
 * @since soda4LCA 6.7.3
 */
@Configuration
@EnableCaching
public class CachingConfig {

    public static final String DEFAULT_CACHEMANAGER = "default_cache";
    public static final String DEPENDENCIES_CACHEMANAGER = "dataset_dependencies_cache";
    public static final String LICENSE_CACHEMANAGER = "license_cache_manager";
    /**
     * Alias for ConcurrentMapCacheManager that holds:
     *
     * <ul>
     * <li>reference years</li>
     * <li>validuntil years</li>
     * <li>locations</li>
     * <li>languages</li>
     * <li>complianceSystems</li>
     * <li>databases</li>
     * </ul>
     *
     *
     * <p>
     * As defined in the deprecated ForeignNodesDataCache:
     * </p>
     * <code>Node/src/main/java/de/iai/ilcd/model/dao/ForeignNodesDataCache.java</code>
     **/
    public static final String FOREIGN_NODES_CACHEMANGER = "foreign_nodes_cache";
    /**
     * The time in milliseconds that a cache entry will be valid from the time
     * it is stored
     */
    public static final int FOREIGN_NODES_ENTRY_TTL = 86400000; // 24 hours
    static final Logger LOGGER = LoggerFactory.getLogger(CachingConfig.class);

    @Bean
    @Primary
    @Qualifier(DEFAULT_CACHEMANAGER)
    public CacheManager getDefaultCacheManager() {
        return new ConcurrentMapCacheManager(DEFAULT_CACHEMANAGER, DEPENDENCIES_CACHEMANAGER, FOREIGN_NODES_CACHEMANGER, LICENSE_CACHEMANAGER);
    }

    @Scheduled(fixedRate = FOREIGN_NODES_ENTRY_TTL)
    @CacheEvict(value = FOREIGN_NODES_CACHEMANGER, allEntries = true)
    public void cronClean() {
        LOGGER.info("FOREIGN_NODES_CACHEMANGER has been cleared");
    }

}