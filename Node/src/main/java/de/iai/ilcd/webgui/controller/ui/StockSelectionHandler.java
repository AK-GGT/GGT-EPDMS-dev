/**
 *
 */
package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.AuthorizationException;

import javax.annotation.PostConstruct;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Managed bean for data stock
 */
@ViewScoped
@ManagedBean(name = "stockSelection")
public class StockSelectionHandler implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -2678136415735063421L;

    private static Logger logger = LogManager.getLogger(StockSelectionHandler.class);

    /**
     * Query parameter map to manipulate for other stock
     */
    private Map<String, String[]> paramMap;

    /**
     * Stock selection bean
     */
    @ManagedProperty(value = "#{availableStocks}")
    private AvailableStockHandler availableStocksHandler;

    /**
     * Configuration bean
     */
    @ManagedProperty(value = "#{conf}")
    private ConfigurationBean conf;

    /**
     * Flag to indicate if default root data stock was selected automatically (no stock parameter provided)
     */
    private boolean autoDefault;

    /**
     * Current stock meta data
     */
    private IDataStockMetaData currentStock;

    /**
     * Initialize data stock bean
     */
    public StockSelectionHandler() {
        // do nothing here that requires the injected beans, use init (because of the managed property)!!

        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest servletRequest = (HttpServletRequest) ctx.getExternalContext().getRequest();
        this.paramMap = new HashMap<String, String[]>(servletRequest.getParameterMap());

    }

    @PostConstruct
    public void init() {
        try {
            // stock can be set either by name or UUID. UUID takes precedence

            String stockId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("stockId");
            if (stockId != null) {
                if (logger.isDebugEnabled())
                    logger.debug("setting current stock by id to " + stockId);
                this.setCurrentStockByUUID(stockId);
                return;
            }

            String stock = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("stock");
            if (stock != null) {
                if (logger.isDebugEnabled())
                    logger.debug("setting current stock to " + stock);
                this.setCurrentStock(stock);
            }

            if (stock == null && stockId == null)
                this.setDefaultStock();
        } catch (Exception e) {
            logger.debug("error setting stock: ", e);
            if (e instanceof AuthorizationException) {
                throw (AuthorizationException) e;
            }
            this.setDefaultStock();
        }
    }

    /**
     * Get the current stock meta data
     *
     * @return current stock meta data
     */
    public IDataStockMetaData getCurrentStock() {
        return this.currentStock;
    }

    /**
     * Set the current stock via DB id
     *
     * @param id database id
     */
    public void setCurrentStock(long id) {
        if (logger.isDebugEnabled())
            logger.debug("setting current stock to stock with id " + id);
        for (IDataStockMetaData m : this.availableStocksHandler.getAllStocksMeta()) {
            if (m.getId() == id) {
                this.currentStock = m;
                this.autoDefault = false;
                if (logger.isDebugEnabled())
                    logger.debug("which happens to be " + m.getName());
                return;
            }
        }
        this.setDefaultStock();
    }

    /**
     * Set the current stock via name
     *
     * @param name stock name
     */
    public void setCurrentStock(String name) {
        if (logger.isDebugEnabled())
            logger.debug("setting current stock to  " + name);

        if (StringUtils.isBlank(name)) {
            this.setDefaultStock();
            return;
        }
        for (IDataStockMetaData m : this.availableStocksHandler.getAllStocksMeta()) {
            if (name.equals(m.getName()) && !m.getDisplayProperties().isHidden()) {
                this.currentStock = m;
                this.autoDefault = false;
                return;
            }
        }
        this.setDefaultStock();
    }

    /**
     * Get the current stock meta data as array with one entry
     *
     * @return current stock meta data as array with one entry
     */
    public IDataStockMetaData[] getCurrentStockAsArray() {
        return new IDataStockMetaData[]{this.currentStock};
    }

    /**
     * Set the current stock via name
     *
     * @param uuid stock name
     */
    public void setCurrentStockByUUID(String uuid) {
        if (logger.isDebugEnabled())
            logger.debug("setting current stock to  " + uuid);

        if (StringUtils.isBlank(uuid)) {
            this.setDefaultStock();
            return;
        }
        for (IDataStockMetaData m : this.availableStocksHandler.getAllStocksMeta()) {
            if (uuid.equals(m.getUuidAsString()) && !m.getDisplayProperties().isHidden()) {
                this.currentStock = m;
                this.autoDefault = false;
                return;
            }
        }
        this.setDefaultStock();
    }

    /**
     * Get the name of the current stock
     *
     * @return name of the current stock
     */
    public String getCurrentStockName() {
        return this.currentStock != null ? this.currentStock.getName() : "";
    }

    /**
     * Set the name of the current stock
     *
     * @param name name to set
     */
    public void setCurrentStockName(String name) {
        this.setCurrentStock(name);
    }

    /**
     * Indicate whether is is allowed to export (download) the currently
     * selected stock
     *
     * @return true if export is allowed, false otherwise
     */
    public boolean isExportAllowed() {
        if (currentStock == null)
            return false;
        try {
            SecurityUtil.assertCanExport(currentStock);
        } catch (AuthorizationException e) {
            return false;
        }
        return true;
    }

    /**
     * Get the handler for the available stocks
     *
     * @return handler for the available stocks
     */
    public AvailableStockHandler getAvailableStocksHandler() {
        return this.availableStocksHandler;
    }

    /**
     * Set the handler for the available stocks
     *
     * @param availableStocksHandler the handler for the available stocks to set
     */
    public void setAvailableStocksHandler(AvailableStockHandler availableStocksHandler) {
        this.availableStocksHandler = availableStocksHandler;
    }

    /**
     * Called by selectOneMenu after setting new stock value.
     * Redirect is done by {@link NavigationHandler}.
     */
    public void navigate() {
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer sb = new StringBuffer(context.getViewRoot().getViewId());
        sb.append("?");
        for (String s : this.paramMap.keySet()) {
            sb.append(s);
            sb.append("=");
            sb.append(this.paramMap.get(s)[0]);
        }
        sb.append("&faces-redirect=true");

        NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
        navigationHandler.handleNavigation(context, null, sb.toString());
    }

    /**
     * Method to wait for new values from stock selection selectOneMenu
     *
     * @param event event with old and new values (note: stock name is passed)
     */
    public void stockChangeEventHandler(ValueChangeEvent event) {
        this.paramMap.put("stock", new String[]{(String) event.getNewValue()});
    }

    /**
     * Set the default stock.
     */
    private void setDefaultStock() {
        if (logger.isDebugEnabled())
            logger.debug("setting stock to default stock");

        this.autoDefault = true;
        final long defId = this.conf.getDefaultDataStockId();

        if (logger.isDebugEnabled())
            logger.debug("default stock id is " + defId);

        for (IDataStockMetaData m : this.availableStocksHandler.getAllStocksMeta()) {
            if (defId == m.getId() && !m.getDisplayProperties().isHidden()) {
                this.currentStock = m;
                if (logger.isDebugEnabled())
                    logger.debug("setting default stock " + m.getName());
                return;
            }
        }
        if (!this.availableStocksHandler.getAllStocksMeta().isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("setting stock to first available item");

            this.currentStock = this.availableStocksHandler.getAllStocksMeta().get(0);
            return;
        }
        if (logger.isDebugEnabled())
            logger.debug("doing nothing");
    }

    /**
     * Get the configuration bean
     *
     * @return the configuration bean
     */
    public ConfigurationBean getConf() {
        return this.conf;
    }

    /**
     * Set the configuration bean
     *
     * @param conf the configuration bean to set
     */
    public void setConf(ConfigurationBean conf) {
        this.conf = conf;
    }

    /**
     * Determine if default root data stock was selected automatically (no stock parameter provided)
     *
     * @return <code>true</code> if default root data stock was selected automatically (no stock parameter provided),
     * <code>false</code> otherwise
     */
    public boolean isAutoDefault() {
        return this.autoDefault;
    }

}
