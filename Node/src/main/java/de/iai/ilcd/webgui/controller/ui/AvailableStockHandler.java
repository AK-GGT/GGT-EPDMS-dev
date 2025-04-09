/**
 *
 */
package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.security.IUser;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.webgui.controller.DirtyFlagBean;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Managed bean for data stock
 */
@SessionScoped
@ManagedBean(name = "availableStocks")
public class AvailableStockHandler implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -2678136344534345321L;

    /**
     * The last known time stamp for a stock modification
     */
    private long lastKnownStockModifiation;

    /**
     * Dirty flag bean
     */
    @ManagedProperty(value = "#{dirty}")
    private DirtyFlagBean dirty;

    /**
     * List of all stock meta data
     */
    private List<IDataStockMetaData> allStocksMeta = new ArrayList<IDataStockMetaData>();

    /**
     * List of all root stock meta data
     */
    private List<IDataStockMetaData> rootStocksMeta = new ArrayList<IDataStockMetaData>();


    /**
     * List of all visible stock meta data
     */
    private List<IDataStockMetaData> visibleStocksMeta = new ArrayList<IDataStockMetaData>();

    /**
     * Initialize data stock bean
     */
    public AvailableStockHandler() {
        this.reloadAllDataStocks();
    }

    /**
     * Get meta data of all data stocks
     *
     * @return meta data of all data stocks
     */
    public List<IDataStockMetaData> getAllStocksMeta() {
        if (this.dirty.isStockModificationDirty(this.lastKnownStockModifiation)) {
            this.reloadAllDataStocks();
        }
        return this.allStocksMeta;
    }

    /**
     * Get meta data of all root data stocks
     *
     * @return meta data of all data stocks
     */
    public List<IDataStockMetaData> getRootStocksMeta() {
        if (this.dirty != null && this.dirty.isStockModificationDirty(this.lastKnownStockModifiation)) {
            this.reloadAllDataStocks();
        }
        return this.rootStocksMeta;
    }

    /**
     * Get meta data of all visible data stocks
     *
     * @return meta data of all visible data stocks
     */
    public List<IDataStockMetaData> getVisibleStocksMeta() {
        if (this.dirty != null && this.dirty.isStockModificationDirty(this.lastKnownStockModifiation)) {
            this.reloadAllDataStocks();
        }
        return this.visibleStocksMeta;
    }

    /**
     * Load all data stocks.
     */
    public void reloadAllDataStocks() {
        // new list created and assigned at the end in order to
        // prevent ConcurrentModificationExceptions from being
        // thrown if list is cleared / entries added while
        // view is iterating through the list
        List<IDataStockMetaData> meta = new ArrayList<IDataStockMetaData>();
        List<IDataStockMetaData> root = new ArrayList<IDataStockMetaData>();
        List<IDataStockMetaData> visible = new ArrayList<IDataStockMetaData>();

        CommonDataStockDao dao = new CommonDataStockDao();
        List<AbstractDataStock> lstDs;
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.getPrincipals() == null) {
            lstDs = dao.getAllReadable(0, null);
        } else {
            // String userName = SecurityUtil.getPrincipalName();
            IUser user = SecurityUtil.getCurrentUser();
            if (user == null)
                // no user name set => assume guest
                // no user (aka guest), all readable for guests
                lstDs = dao.getAllReadable(0, null);
            else {
                // super admin ==> all
                if (user.isSuperAdminPermission()) {
                    lstDs = dao.getAll();
                }
                // no super admin ==> all readable for user
                else {
                    lstDs = user.getReadableStocks();
                }
            }

        }

        for (AbstractDataStock stock : lstDs) {
            meta.add(new DataStockMetaData(stock));
            if (stock.isRoot())
                root.add(new DataStockMetaData(stock));
            if (!stock.getDisplayProperties().isHidden())
                visible.add(stock);
        }

        this.allStocksMeta = meta;
        this.rootStocksMeta = root;
        this.visibleStocksMeta = visible;
        this.lastKnownStockModifiation = DirtyFlagBean.getNow();
    }

    /**
     * Get the dirty flag bean
     *
     * @return dirty flag bean
     */
    public DirtyFlagBean getDirty() {
        return this.dirty;
    }

    /**
     * Set the dirty flag bean
     *
     * @param dirty the dirty flag bean
     */
    public void setDirty(DirtyFlagBean dirty) {
        this.dirty = dirty;
    }

    /**
     * Get meta data of stock by its identifier (name or uuid)
     *
     * @param name name of the stock
     * @return meta data of stock
     */
    public IDataStockMetaData getStockByIdentifier(String stockIdentifier) {
        if (StringUtils.isNotBlank(stockIdentifier)) {
            boolean isUuid = stockIdentifier.indexOf('-') >= 0;
            if (isUuid) {
                return this.getStockByUuid(stockIdentifier);
            } else {
                return this.getStock(stockIdentifier);
            }
        }
        return null;
    }

    /**
     * Get meta data of stock by its UUID
     *
     * @param name name of the stock
     * @return meta data of stock
     */
    public IDataStockMetaData getStockByUuid(String uuid) {
        if (uuid == null) {
            return null;
        }
        for (IDataStockMetaData meta : this.allStocksMeta) {
            if (uuid.equals(meta.getUuidAsString())) {
                return meta;
            }
        }
        return null;
    }

    /**
     * Get meta data of stock by its name
     *
     * @param name name of the stock
     * @return meta data of stock
     */
    public IDataStockMetaData getStock(String name) {
        if (name == null) {
            return null;
        }
        for (IDataStockMetaData meta : this.allStocksMeta) {
            if (name.equals(meta.getName())) {
                return meta;
            }
        }
        return null;
    }

}
