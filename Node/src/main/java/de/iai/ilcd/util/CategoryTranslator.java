package de.iai.ilcd.util;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.ClassificationDao;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

public class CategoryTranslator {

    private final static Logger logger = LogManager.getLogger(CategoryTranslator.class);
    ClassificationDao cDao = new ClassificationDao();
    private ClassLoader loader = null;
    private String bundleBaseName = null;
    private final DataSetType datasetType;

    public CategoryTranslator(DataSetType datasetType, String catSystem) {
        if (logger.isDebugEnabled()) {
            logger.debug("instantiating CategoryTranslator for dataset type " + datasetType + " and " + catSystem);
            logger.debug("reading translations from " + ConfigurationService.INSTANCE.getTranslateClassifications().get(catSystem.toUpperCase()));
        }
        this.datasetType = datasetType;
        try {
            this.bundleBaseName = StringUtils.replaceEach(catSystem, new String[]{".", "-"}, new String[]{"", ""});
            this.bundleBaseName = this.bundleBaseName.toUpperCase();
            File file = new File(ConfigurationService.INSTANCE.getTranslateClassifications().get(catSystem.toUpperCase()));
            URL[] urls = {file.toURI().toURL()};
            this.loader = new URLClassLoader(urls);
        } catch (Exception e) {
            logger.debug("error resolving resource bundle", e);
        }
        if (logger.isDebugEnabled())
            logger.debug("CategoryTranslator successfully initialized");
    }

    public String translateTo(String key, String toLanguage) {
        ResourceBundle bundle;
        String result;
        if (logger.isDebugEnabled())
            logger.debug("translating key '" + key + "' to " + toLanguage);
        try {
            bundle = ResourceBundle.getBundle(bundleBaseName, new Locale(toLanguage), loader);
        } catch (Exception e) {
            logger.error("resource bundle '" + bundleBaseName + "' cannot be found");
            return key;
        }
        try {
            result = bundle.getString(key);
            if (logger.isDebugEnabled())
                logger.debug("result is '" + result + "'");
        } catch (Exception e) {
            logger.warn("translation cannot be resolved for key '" + key + "' ");
            if (key == null)
                return null;
            return key.concat(" (missing translation)");
        }
        return result;
    }

    public String translateFrom(String value, String fromLanguage) {
        ResourceBundle bundle;
        if (logger.isDebugEnabled())
            logger.debug("translating value '" + value + "' to " + fromLanguage);
        try {
            bundle = ResourceBundle.getBundle(bundleBaseName, new Locale(fromLanguage), loader);
            String result;
            for (String key : bundle.keySet()) {
                String keyValue = bundle.getString(key);
                if (value.equals(keyValue)) {
                    result = cDao.getNameByClId(datasetType, key);
                    if (logger.isDebugEnabled())
                        logger.debug("result is '" + result + "'");
                    return result;
                }
            }
        } catch (Exception e) {
            logger.error("resource bundle '" + bundleBaseName + "' cannot be found");
            return value;
        }

        return value;
    }
}
