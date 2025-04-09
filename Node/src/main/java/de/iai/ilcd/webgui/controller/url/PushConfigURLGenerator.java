package de.iai.ilcd.webgui.controller.url;

/**
 * URL Generator for PushConfig links
 *
 * @author sarai
 */
public class PushConfigURLGenerator extends AbstractURLGenerator {

    /**
     * Initializes the PushConfig URL Generator.
     *
     * @param urlBean The URL generator bean
     */
    public PushConfigURLGenerator(URLGeneratorBean urlBean) {
        super(urlBean);
    }

    /**
     * Gets the URL to create a new PushConfig (on current stock name).
     *
     * @return generated URL
     */
    public String getNew() {
        return getNew(this.getCurrentStockName());
    }

    /**
     * Gets the URL to create a new PushConfig on given stock name.
     *
     * @param stockName The stock name
     * @return The generated URL
     */
    public String getNew(String stockName) {
        return "/admin/newPushConfig.xhtml" + super.getStockURLParam(stockName, true);
    }

    /**
     * Gets the URL to edit PushConfig.
     *
     * @return The generated URL to edit PushConfig
     */
    public String getEdit() {
        return this.getEdit(null, this.getCurrentStockName());
    }

    /**
     * Gets the URL to edit PushConfig with given ID.
     *
     * @param id The Id of PushConfig
     * @return The generated URL to edit PushConfig with given ID
     */
    public String getEdit(Long id) {
        return getEdit(id, this.getCurrentStockName());
    }

    /**
     * Gets the URL to edit PushConfig on given stock name.
     *
     * @param stockName The stock name
     * @return A generated URL to edit PushConfig
     */
    public String getEdit(String stockName) {
        return getEdit(null, stockName);
    }

    /**
     * Gets the URL to edit PushConfig with given ID on given stock.
     *
     * @param id        The ID of PushConfig
     * @param stockName The stock name
     * @return A generated URL to edit PushCOnfig with given ID on given stock.
     */
    public String getEdit(Long id, String stockName) {
        return "/admin/showPushConfig.xhtml?" + (id != null ? "pushConfigId=" + id.toString() + "&" : "") + super.getStockURLParam(stockName);
    }

    /**
     * Gets the URL to show the list to manage a PushConfig (on current stock).
     *
     * @return A generated URL to manage PushConfig list
     */
    public String getShowList() {
        return getShowList(this.getCurrentStockName());
    }

    /**
     * Gets the URL to show the list to manage a PushConfig on given stock.
     *
     * @param stockName The stock name
     * @return A generated URL to manage PushConfig list
     */
    public String getShowList(String stockName) {
        return "/admin/managePushConfigList.xhtml" + super.getStockURLParam(stockName, true);
    }

}
