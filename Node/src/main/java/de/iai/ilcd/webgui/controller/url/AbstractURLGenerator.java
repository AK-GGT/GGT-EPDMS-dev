package de.iai.ilcd.webgui.controller.url;

import de.iai.ilcd.webgui.controller.ConfigurationBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base implementation for URL generators. Please note that all generated
 * URLs are absolute URLs within context path (start with <code>/</code>).
 * If your element requires context path to be part of the URL, use the
 * context path provided by {@link ConfigurationBean}.
 */
public abstract class AbstractURLGenerator {

    protected static final Logger logger = LogManager.getLogger(AbstractURLGenerator.class);

    /**
     * URL generator bean
     */
    private final URLGeneratorBean urlBean;

    /**
     * Create the generator
     *
     * @param urlBean underlying URL bean
     */
    public AbstractURLGenerator(URLGeneratorBean urlBean) {
        this.urlBean = urlBean;
    }

    /**
     * Get the current stock name
     *
     * @return current stock name
     */
    protected final String getCurrentStockName() {
        return this.urlBean.getCurrentStockName();
    }

    /**
     * Encode an URL parameter
     *
     * @param s string to encode
     * @return encoded string
     */
    protected final String encodeURL(String s) {
        return this.urlBean.encodeURL(s);
    }

    /**
     * Get the URL parameter for stock
     *
     * @param stockName name of stock
     * @return URL parameter for stock as String
     */
    protected final String getStockURLParam(String stockName) {
        return this.urlBean.getStockURLParam(stockName, false);
    }

    /**
     * Get the URL parameter for stock
     *
     * @param stockName           name of stock
     * @param includeQuestionMark include question mark (first parameter)
     * @return URL parameter for stock as String
     */
    protected final String getStockURLParam(String stockName, boolean includeQuestionMark) {
        return this.urlBean.getStockURLParam(stockName, includeQuestionMark);
    }

    protected final String buildPathWithParams(String path, String... params) {
        String parameters = String.join("&", params);
        StringBuilder result = new StringBuilder(path);
        if (StringUtils.isNotBlank(parameters)) {
            result.append("?");
            result.append(parameters);
        }
        return result.toString();
    }

}
