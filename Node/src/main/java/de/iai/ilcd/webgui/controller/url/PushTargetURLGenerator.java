package de.iai.ilcd.webgui.controller.url;

/**
 * URL Generator for PushTarget links
 *
 * @author sarai
 */
public class PushTargetURLGenerator extends AbstractURLGenerator {

    /**
     * Initializes the PushTarget URL Generator.
     *
     * @param urlBean The URL generator bean
     */
    public PushTargetURLGenerator(URLGeneratorBean urlBean) {
        super(urlBean);
    }

    /**
     * Gets the URL to create a new PushTarget (on current stock name).
     *
     * @return generated URL
     */
    public String getNew() {
        return getNew(this.getCurrentStockName());
    }

    /**
     * Gets the URL to create a new PushTarget on given stock name.
     *
     * @param stockName The stock name
     * @return The generated URL
     */
    public String getNew(String stockName) {
        return "/admin/newPushTarget.xhtml" + super.getStockURLParam(stockName, true);
    }

    /**
     * Gets the URL to edit PushTarget.
     *
     * @return The generated URL to edit PushTarget
     */
    public String getEdit() {
        return this.getEdit(null, this.getCurrentStockName());
    }

    /**
     * Gets the URL to edit PushTarget with given ID.
     *
     * @param id The Id of PushTarget
     * @return The generated URL to edit PushTarget with given ID
     */
    public String getEdit(Long id) {
        return getEdit(id, this.getCurrentStockName());
    }

    /**
     * Gets the URL to edit PushTarget on given stock name.
     *
     * @param stockName The stock name
     * @return A generated URL to edit PushTarget
     */
    public String getEdit(String stockName) {
        return getEdit(null, stockName);
    }

    /**
     * Gets the URL to edit PushTarget with given ID on given stock.
     *
     * @param id        The ID of PushTarget
     * @param stockName The stock name
     * @return A generated URL to edit PushTarget with given ID on given stock.
     */
    public String getEdit(Long id, String stockName) {
        String pushTargetIdParam = (id != null ? "pushTargetId=" + id.toString() : "");
        String stockParam = super.getStockURLParam(stockName, false);
        return buildPathWithParams("/admin/showPushTarget.xhtml", pushTargetIdParam, stockParam);
    }

    /**
     * Gets the URL to show the list to manage a PushTarget (on current stock).
     *
     * @return A generated URL to manage PushTarget list
     */
    public String getShowList() {
        return getShowList(this.getCurrentStockName());
    }

    /**
     * Gets the URL to show the list to manage a PushTarget on given stock.
     *
     * @param stockName The stock name
     * @return A generated URL to manage PushTarget list
     */
    public String getShowList(String stockName) {
        return "/admin/managePushTargetList.xhtml" + super.getStockURLParam(stockName, true);
    }

}
