package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.service.model.IDataSetListVO;
import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facility for caching the results of distributed queries
 */
public enum ForeignDataSetsHelperCache {

    INSTANCE;

    private static final Logger logger = LogManager.getLogger(ForeignDataSetsHelperCache.class);
    private ConcurrentHashMap<MultivaluedMap<String, String>, CacheEntry> cache = new ConcurrentHashMap<MultivaluedMap<String, String>, CacheEntry>();

    public void put(MultivaluedMap<String, String> key, List<IDataSetListVO> data) {
        if (logger.isTraceEnabled()) {
            logger.trace("cache size is " + this.cache.size());
            logger.trace("putting item in cache: " + key + " (" + data.size() + " results)");
        }

        this.cache.put(key, new CacheEntry(data));

        if (logger.isTraceEnabled())
            logger.trace("new cache size is " + this.cache.size());
    }

    public List<IDataSetListVO> get(MultivaluedMap<String, String> key) {
        if (logger.isTraceEnabled())
            logger.trace("cache size is " + this.cache.size());

        cleanUp();

        CacheEntry item = this.cache.get(key);
        if (item == null) {
            logger.trace("get: cache miss");
            return null;
        } else {
            logger.debug("get: cache hit");
            if (item.isExpired()) {
                if (logger.isTraceEnabled())
                    logger.trace("but item is expired");
                this.cache.remove(key);
                return null;
            }
            return item.data;
        }
    }

    public boolean hasItem(MultivaluedMap<String, String> key) {
        if (logger.isTraceEnabled()) {
            logger.trace("cache size is " + this.cache.size());
            logger.trace("hasItem: cache " + (this.cache.containsKey(key) ? "hit" : "miss"));
            if (this.cache.containsKey(key))
                logger.trace("item is expired: " + this.cache.get(key).isExpired());
        }
        return (this.cache.containsKey(key) && !this.cache.get(key).isExpired());
    }

    public void clear() {
        this.cache.clear();
    }

    public void cleanUp() {
        if (logger.isTraceEnabled()) {
            logger.trace("cache cleanup");
            logger.trace("cache size before cleanup is " + this.cache.size());
        }

        for (MultivaluedMap<String, String> key : this.cache.keySet()) {
            CacheEntry item = this.cache.get(key);
            if (item.isExpired())
                this.cache.remove(key);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("cache size after cleanup is " + this.cache.size());
        }
    }

    public int getSize() {
        return this.cache.size();
    }

    private class CacheEntry {
        private List<IDataSetListVO> data;

        private long expiry;

        public CacheEntry(List<IDataSetListVO> data) {
            this.expiry = System.currentTimeMillis() + ConfigurationService.INSTANCE.getSearchDistCacheTTL();
            this.data = data;
        }

        public boolean isExpired() {
            return this.expiry < System.currentTimeMillis();
        }
    }

}