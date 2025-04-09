package de.iai.ilcd.util;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * UTF-8 enabled resource bundle, based on:
 * <code>http://jdevelopment.nl/internationalization-jsf-utf8-encoded-properties-files/</code>
 */
public class SodaResourceBundle extends ResourceBundle implements Serializable {

    /**
     * Name of the bundle
     */
    protected static final String BUNDLE_NAME = "resources.lang";
    /**
     * Bundle files extension
     */
    protected static final String BUNDLE_EXTENSION = "properties";
    /**
     * UTF-8 controls
     */
    protected static final Control UTF8_CONTROL = new UTF8Control();
    /**
     *
     */
    private static final long serialVersionUID = 4429407288499738336L;

    /**
     * Create the bundle
     */
    public SodaResourceBundle() {
        this.setParent(ResourceBundle.getBundle(BUNDLE_NAME, FacesContext.getCurrentInstance().getViewRoot().getLocale(), SodaResourceBundle.UTF8_CONTROL));
    }

    public SodaResourceBundle(String lang) {
        this.setParent(ResourceBundle.getBundle(BUNDLE_NAME, new Locale(lang), SodaResourceBundle.UTF8_CONTROL));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object handleGetObject(String key) {
        try {
            return this.parent.getObject(key);
        } catch (MissingResourceException e) {
            return "???".concat(key).concat("???");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getKeys() {
        return this.parent.getKeys();
    }

    /**
     * Control for UTF-8
     */
    protected static class UTF8Control extends Control {

        /**
         * {@inheritDoc}
         */
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            // The below code is copied from default Control#newBundle() implementation.
            // Only the PropertyResourceBundle line is changed to read the file as UTF-8.
            String bundleName = this.toBundleName(baseName, locale);
            String resourceName = this.toResourceName(bundleName, BUNDLE_EXTENSION);
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }
}
