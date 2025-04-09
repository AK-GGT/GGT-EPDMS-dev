package de.iai.ilcd.util;

import de.iai.ilcd.model.common.ConfigurationItem;
import de.iai.ilcd.model.dao.ConfigurationItemDao;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.PersistException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for access the configuration to the
 * application wide configuration settings
 */
public class DBConfigurationUtil {

    private static Logger logger = LogManager.getLogger(DBConfigurationUtil.class);

    private static ConfigurationItemDao dao = new ConfigurationItemDao();

    /**
     * No instances, just static methods
     */
    private DBConfigurationUtil() {
    }

    /**
     * Update configuration for default data stock
     *
     * @param id     ID of the default data stock
     * @param isRoot root data stock flag for default data stock
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    public static boolean setDefaultDataStock(long id, boolean isRoot) {
        try {
            ConfigurationItem defaultStockId = dao.getConfigurationItem("datastock.default");
            Long stockId = defaultStockId.getIntvalue();

            if (!stockId.equals(id)) {
                defaultStockId.setIntvalue(id);
                dao.merge(defaultStockId);
            }

            ConfigurationItem defaultStockRoot = dao.getConfigurationItem("datastock.default_isroot");
            boolean root = defaultStockRoot.getIntvalue().equals(Long.valueOf(1));

            if (logger.isDebugEnabled()) {
                logger.debug("root: " + root);
                logger.debug("isRoot: " + isRoot);
            }
            if (root != isRoot) {
                if (logger.isDebugEnabled()) {
                    logger.debug("new isRoot value: " + (isRoot ? 1 : 0));
                }
                defaultStockRoot.setIntvalue(Long.valueOf(isRoot ? 1 : 0));
                dao.merge(defaultStockRoot);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean setDataprovider(String provider) {
        ConfigurationItem dataprovider = dao.getConfigurationItem("glad.dataprovider");
        if (dataprovider != null) {
            dataprovider.setStringvalue(provider);
            try {
                dao.merge(dataprovider);
            } catch (MergeException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            dataprovider = new ConfigurationItem();
            dataprovider.setPoperty("glad.dataprovider");
            dataprovider.setStringvalue(provider);
            try {
                dao.persist(dataprovider);
            } catch (PersistException pe) {
                pe.printStackTrace();
                return false;
            }
        }
        return true;
    }

}
