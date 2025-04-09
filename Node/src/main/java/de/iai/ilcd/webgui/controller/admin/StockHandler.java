package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.security.ISecurityEntity;
import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.model.security.UserGroup;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.tag.Tag;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.StockAccessRight.AccessRightType;
import de.iai.ilcd.security.StockAccessRightDao;
import de.iai.ilcd.security.sql.IlcdAuthorizationInfo;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.util.StockAccessRightWrapper;
import de.iai.ilcd.util.StockChildrenWrapper;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import de.iai.ilcd.webgui.controller.DirtyFlagBean;
import de.iai.ilcd.webgui.controller.SessionStorageBean;
import de.iai.ilcd.webgui.controller.ui.AvailableStockHandler;
import de.iai.ilcd.webgui.controller.url.URLGeneratorBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base for admin handlers for data stock entries. Use the models of the
 * several data types for the detachment of children as well. For details
 * see documentation of {@link StockChildrenWrapper}.
 */
@ViewScoped
@ManagedBean(name = "stockHandler")
public class StockHandler extends AbstractAdminEntryHandler<AbstractDataStock> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 8912522301456171429L;
    /**
     * List of all data stocks except current one for loading of candidates
     */
    private final List<IDataStockMetaData> candidateStocks = new ArrayList<IDataStockMetaData>();
    /**
     * Dirty flag bean
     */
    @ManagedProperty(value = "#{dirty}")
    private DirtyFlagBean dirty;
    /**
     * Configuration bean
     */
    @ManagedProperty(value = "#{conf}")
    private ConfigurationBean conf;
    /**
     * Available stocks bean
     */
    @ManagedProperty(value = "#{availableStocks}")
    private AvailableStockHandler availableStocks;
    @ManagedProperty(value = "#{sessionStore}")
    private SessionStorageBean store;
    /**
     * List of available organizations
     */
    private List<Organization> organizations;

    /**
     * Stock children wrapper for processes
     */
    private StockChildrenWrapper<Process> processWrapper;

    /**
     * Stock children wrapper for LCIA methods
     */
    private StockChildrenWrapper<LCIAMethod> lciaMethodWrapper;

    /**
     * Stock children wrapper for flows
     */
    private StockChildrenWrapper<ElementaryFlow> elementaryFlowWrapper;

    /**
     * Stock children wrapper for flows
     */
    private StockChildrenWrapper<ProductFlow> productFlowWrapper;

    /**
     * Stock children wrapper for flow properties
     */
    private StockChildrenWrapper<FlowProperty> flowPropertyWrapper;

    /**
     * Stock children wrapper for sources
     */
    private StockChildrenWrapper<Source> sourceWrapper;

    /**
     * Stock children wrapper for contacts
     */
    private StockChildrenWrapper<Contact> contactWrapper;


    /**
     * Stock children wrapper for unit groups
     */
    private StockChildrenWrapper<UnitGroup> unitGroupWrapper;

    /**
     * Stock children wrapper for Life-Cycle Models
     */
    private StockChildrenWrapper<LifeCycleModel> lifeCycleModelWrapper;

    /**
     * Flag to indicate if this is a root stock currently
     */
    private boolean rootStock = false;

    /**
     * List of group access rights for this data stock
     */
    private List<StockAccessRightWrapper> accessRightsGroup;

    /**
     * List of user access rights for this data stock
     */
    private List<StockAccessRightWrapper> accessRightsUser;

    /**
     * List of all available protection types to display as column heading in datatable
     */
    private List<String> protectTypeColumns;

    /**
     * Dual list model for group assignment
     */
    private DualListModel<UserGroup> dualGroupAssignmentList;

    /**
     * Dual list model for user assignment
     */
    private DualListModel<User> dualUserAssignmentList;

    /**
     * Dual list model for tags
     */
    private DualListModel<Tag> dualTagList;

    /**
     * Flag to indicate if confirm dialog shall be shown on group access right assignment
     */
    private boolean confirmGroupRightDelete;

    /**
     * Flag to indicate if confirm dialog shall be shown on user access right assignment
     */
    private boolean confirmUserRightDelete;

    /**
     * Map to get users by ID
     */
    private Map<Long, User> userMapById;

    /**
     * Map to get groups by ID
     */
    private Map<Long, UserGroup> groupMapById;

    /**
     * Flag for only retrieving the most recent version of each dataset
     */
    private boolean mostRecentVersionOnly = ConfigurationService.INSTANCE.getDisplayConfig().isDefaultMostRecentVersionOnly();

    public boolean isMostRecentVersionOnly() {
        return mostRecentVersionOnly;
    }

    public void setMostRecentVersionOnly(boolean mostRecentVersionOnly) {
        this.mostRecentVersionOnly = mostRecentVersionOnly;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changeAttachedEntry() {
        CommonDataStockDao dao = new CommonDataStockDao();
        final AbstractDataStock stock = this.getEntry();
        if (StringUtils.isBlank(stock.getName())) {
            this.addI18NFacesMessage("facesMsg.stock.noName", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        AbstractDataStock existingDs = dao.getDataStockByName(stock.getName());
        if (existingDs != null && !existingDs.getId().equals(stock.getId())) {
            this.addI18NFacesMessage(existingDs.isRoot() ? "facesMsg.rootstock.alreadyExists" : "facesMsg.stock.alreadyExists", FacesMessage.SEVERITY_ERROR,
                    existingDs.getName());
            return false;
        }

        // save stock access rights
        if (!this.mergeStockAccessRights(this.accessRightsGroup)) {
            return false;
        }
        if (!this.mergeStockAccessRights(this.accessRightsUser)) {
            return false;
        }

        boolean tmp = this.mergeStock(stock, true);

        this.setStockAndPermissionsDirty();

        return tmp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createEntry() {
        final AbstractDataStock ds = this.getEntry();
        CommonDataStockDao dao = new CommonDataStockDao();

        if (StringUtils.isBlank(ds.getName())) {
            this.addI18NFacesMessage("facesMsg.stock.noName", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        AbstractDataStock existingDs = dao.getDataStockByName(ds.getName());
        if (existingDs != null) {
            this.addI18NFacesMessage(existingDs.isRoot() ? "facesMsg.rootstock.alreadyExists" : "facesMsg.stock.alreadyExists", FacesMessage.SEVERITY_ERROR,
                    existingDs.getName());
            return false;
        }

        if (ds.getId() != null) {
            this.addI18NFacesMessage("facesMsg.stock.saveError", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        AbstractDataStock toSave;
        // selected is a root data stock but current instance is non-root => copy
        if (this.isRootStock() && !ds.isRoot()) {
            toSave = new RootDataStock();
            this.copyFirstTabMeta(ds, toSave);
        }
        // selected is a non-root data stock but current instance is root => copy
        else if (!this.isRootStock() && ds.isRoot()) {
            toSave = new DataStock();
            this.copyFirstTabMeta(ds, toSave);
        }
        // all ok, same type
        else {
            toSave = ds;
        }

        try {
            dao.persist(toSave);

            // guest access right
            StockAccessRight sarGuest = new StockAccessRight(toSave.getId(), AccessRightType.User, 0L, 0);
            StockAccessRightDao sarDao = new StockAccessRightDao();
            sarDao.persist(sarGuest);

            this.dirty.stockModified();
            this.addI18NFacesMessage(toSave.isRoot() ? "facesMsg.rootstock.saveSuccess" : "facesMsg.stock.saveSuccess", FacesMessage.SEVERITY_INFO);
            return true;
        } catch (PersistException e) {
            this.addI18NFacesMessage("facesMsg.stock.saveError", FacesMessage.SEVERITY_ERROR);
            return false;
        }
    }

    /**
     * Set dirty flag for stocks and permissions
     */
    private void setStockAndPermissionsDirty() {
        this.dirty.stockModified();
        IlcdAuthorizationInfo.permissionsChanged();
    }

    /**
     * Copy all meta data from first tab in view from one stock to another
     *
     * @param src source stock
     * @param dst destination stock
     */
    private void copyFirstTabMeta(AbstractDataStock src, AbstractDataStock dst) {
        dst.setName(src.getName());
        dst.setLongTitle(src.getLongTitle());
        dst.setOwnerOrganization(src.getOwnerOrganization());
        dst.setDescription(src.getDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractDataStock createEmptyEntryInstance() {
        return this.isRootStock() ? new RootDataStock() : new DataStock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractDataStock loadEntryInstance(long id) throws Exception {
        CommonDataStockDao dao = new CommonDataStockDao();
        return dao.getById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConstruct() {
        // locate request params
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();

        // extract index of requested tab for tabView
        String requestedTabIndex = request.getParameter("activeTabIndex");
        if (requestedTabIndex != null) {
            try {
                int i = Integer.parseInt(requestedTabIndex);
                this.setActiveTabIndex(i);
            } catch (Exception e) {
                this.setActiveTabIndex(0);
            }
        }
    }

    /**
     * Detach a process from provided stock - do not modify database/persistence unit,
     * just do the model reference detachment work!
     *
     * @param stock stock to remove process from
     * @param p     process to remove from stock
     * @return <code>true</code> to perform action, <code>false</code> to do nothing (no changes)
     */
    protected boolean detachProcessFromStock(AbstractDataStock stock, Process p) {
        if (stock instanceof DataStock) {
            ((DataStock) stock).removeProcess(p);
            return true;
        }
        return false;
    }

    /**
     * Detach a LCIA method from provided stock - do not modify database/persistence unit,
     * just do the model reference detachment work!
     *
     * @param stock      stock to remove LCIA method from
     * @param lciamethod LCIA method to remove from stock
     * @return <code>true</code> to perform action, <code>false</code> to do nothing (no changes)
     */
    protected boolean detachLCIAMethodFromStock(AbstractDataStock stock, LCIAMethod lciamethod) {
        if (stock instanceof DataStock) {
            ((DataStock) stock).removeLCIAMethod(lciamethod);
            return true;
        }
        return false;
    }

    /**
     * Detach a flow from provided stock - do not modify database/persistence unit,
     * just do the model reference detachment work!
     *
     * @param stock stock to remove flow from
     * @param f     flow to remove from stock
     * @return <code>true</code> to perform action, <code>false</code> to do nothing (no changes)
     */
    protected boolean detachFlowFromStock(AbstractDataStock stock, Flow f) {
        if (stock instanceof DataStock) {
            if (f instanceof ElementaryFlow) {
                ((DataStock) stock).removeElementaryFlow((ElementaryFlow) f);
            } else if (f instanceof ProductFlow) {
                ((DataStock) stock).removeProductFlow((ProductFlow) f);
            }
            return true;
        }
        return false;
    }

    /**
     * Detach a flow property from provided stock - do not modify database/persistence unit,
     * just do the model reference detachment work!
     *
     * @param stock stock to remove flow property from
     * @param fp    flow property to remove from stock
     * @return <code>true</code> to perform action, <code>false</code> to do nothing (no changes)
     */
    protected boolean detachFlowPropertyFromStock(AbstractDataStock stock, FlowProperty fp) {
        if (stock instanceof DataStock) {
            ((DataStock) stock).removeFlowProperty(fp);
            return true;
        }
        return false;
    }

    /**
     * Detach an unit group from provided stock - do not modify database/persistence unit,
     * just do the model reference detachment work!
     *
     * @param stock stock to remove unit group from
     * @param ug    unit group to remove from stock
     * @return <code>true</code> to perform action, <code>false</code> to do nothing (no changes)
     */
    protected boolean detachUnitGroupFromStock(AbstractDataStock stock, UnitGroup ug) {
        if (stock instanceof DataStock) {
            ((DataStock) stock).removeUnitGroup(ug);
            return true;
        }
        return false;
    }

    /**
     * Detach a source from provided stock - do not modify database/persistence unit,
     * just do the model reference detachment work!
     *
     * @param stock stock to remove source from
     * @param s     source to remove from stock
     * @return <code>true</code> to perform action, <code>false</code> to do nothing (no changes)
     */
    protected boolean detachSourceFromStock(AbstractDataStock stock, Source s) {
        if (stock instanceof DataStock) {
            ((DataStock) stock).removeSource(s);
            return true;
        }
        return false;
    }

    /**
     * Detach a contact from provided stock - do not modify database/persistence unit,
     * just do the model reference detachment work!
     *
     * @param stock stock to remove process from
     * @param c     contact to remove from stock
     * @return <code>true</code> to perform action, <code>false</code> to do nothing (no changes)
     */
    protected boolean detachContactFromStock(AbstractDataStock stock, Contact c) {
        if (stock instanceof DataStock) {
            ((DataStock) stock).removeContact(c);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postEntrySet() {

        // TODO only for super admin, set selected organization for new entry by user for org admins
        OrganizationDao oDao = new OrganizationDao();
        this.organizations = oDao.getAll();

        if (!this.isCreateView()) {
            // create all stock children wrappers
            this.createLazyDataModelsForStockDataSets();

            UserGroupDao ugDao = new UserGroupDao();
            UserDao uDao = new UserDao();
            TagDao tDao = new TagDao();
            List<UserGroup> allGroups = ugDao.getAll();
            List<User> allUsers = uDao.getAll();
            List<Tag> sourceTags = tDao.getTagsAsc();

            this.userMapById = new HashMap<Long, User>();
            this.groupMapById = new HashMap<Long, UserGroup>();

            this.fillIdMap(allGroups, this.groupMapById);
            this.fillIdMap(allUsers, this.userMapById);

            this.loadAccessRights();

            this.protectTypeColumns = new ArrayList<String>();
            for (ProtectionType protectionType : ProtectionType.values()) {
                this.protectTypeColumns.add(protectionType.toString());
            }

            List<UserGroup> assignedGroups = new ArrayList<UserGroup>();
            List<User> assignedUsers = new ArrayList<User>();
            List<UserGroup> availableGroups = new ArrayList<UserGroup>();
            List<User> availableUsers = new ArrayList<User>();
            List<Tag> targetTags = new ArrayList<Tag>();

            this.processAvailableAndAssigned(this.accessRightsGroup, availableGroups, assignedGroups, allGroups);
            this.processAvailableAndAssigned(this.accessRightsUser, availableUsers, assignedUsers, allUsers);

            this.dualGroupAssignmentList = new DualListModel<UserGroup>(availableGroups, assignedGroups);
            this.dualUserAssignmentList = new DualListModel<User>(availableUsers, assignedUsers);

            List<Tag> dsTags = this.getEntry().getTags();

            // sort the tags beforehand, to get ordered DualLists
            Collections.sort(dsTags,
                    (t1, t2) -> t1.getName().toLowerCase(Locale.ROOT)
                            .compareTo(t2.getName().toLowerCase(Locale.ROOT))); // Anonymous Comparator<Tag>

            if (!dsTags.isEmpty()) {
                for (Tag t : tDao.getTagsAsc()) {
                    if (dsTags.contains(t)) {
                        targetTags.add(t);
                        sourceTags.remove(t);
                    }
                }
            }

            this.dualTagList = new DualListModel<Tag>(sourceTags, targetTags);

            this.candidateStocks.clear();
            if (CollectionUtils.isNotEmpty(this.availableStocks.getAllStocksMeta())) {
                for (IDataStockMetaData m : this.availableStocks.getAllStocksMeta()) {
                    if (!StringUtils.equals(this.getEntry().getName(), m.getName())) {
                        this.candidateStocks.add(m);
                    }
                }
            }

        }
    }

    /**
     * Fill the ID map for list of users or groups. Map key will be the ID.
     *
     * @param <T>   type of map entry / list
     * @param all   list to add to map
     * @param idMap map to add list to
     */
    private <T extends ISecurityEntity> void fillIdMap(List<T> all, Map<Long, T> idMap) {
        for (T secEnt : all) {
            idMap.put(secEnt.getId(), secEnt);
        }
    }

    /**
     * Fill the available and assigned lists for user/group right picklist popup
     *
     * @param <T>         type of map lists
     * @param wrapperList list with all stock access rights
     * @param available   list to fill with available users/groups
     * @param assigned    list to fill with users/groups that have already been given access rights
     * @param all         fallback in case no rights have been defined yet &rArr; add all to available list
     */
    private <T extends ISecurityEntity> void processAvailableAndAssigned(List<StockAccessRightWrapper> wrapperList, List<T> available, List<T> assigned,
                                                                         List<T> all) {
        // sort all entries beforehand, to obtain sorted lists
        Collections.sort(all,
                (t1, t2) -> t1.getDisplayName().toLowerCase(Locale.ROOT)
                        .compareTo(t2.getDisplayName().toLowerCase(Locale.ROOT))); // Anonymous Comparator<ISecurityEntity>

        if (wrapperList != null && !wrapperList.isEmpty()) {
            List<Long> wrappedIds = new ArrayList<Long>();
            for (StockAccessRightWrapper wrapper : wrapperList) {
                wrappedIds.add(wrapper.getAccessRight().getUgId());
            }
            for (T secEnt : all) {
                if (wrappedIds.contains(secEnt.getId())) {
                    assigned.add(secEnt);
                } else {
                    available.add(secEnt);
                }
            }
        }
        // no access rights, show all potential candidates as choice
        else {
            available.addAll(all);
        }
    }

    /**
     * Load access rights from DB
     */
    private void loadAccessRights() {
        StockAccessRightDao sarDao = new StockAccessRightDao();
        AbstractDataStock entry = this.getEntry();

        // load the access rights
        final List<StockAccessRight> accessRightsGroup = sarDao.get(entry, AccessRightType.Group);
        final List<StockAccessRight> accessRightsUser = sarDao.get(entry, AccessRightType.User);

        // and fill the wrappers
        this.accessRightsGroup = new ArrayList<StockAccessRightWrapper>();
        this.accessRightsUser = new ArrayList<StockAccessRightWrapper>();
        if (accessRightsGroup != null && !accessRightsGroup.isEmpty()) {
            for (StockAccessRight sar : accessRightsGroup) {
                this.accessRightsGroup.add(new StockAccessRightWrapper(sar, this.getGroupById(sar.getUgId())));
            }
        }
        if (accessRightsUser != null && !accessRightsUser.isEmpty()) {
            for (StockAccessRight sar : accessRightsUser) {
                this.accessRightsUser.add(new StockAccessRightWrapper(sar, this.getUserById(sar.getUgId())));
            }
        }

    }

    /**
     * Get a user instance by its ID
     *
     * @param id ID of user
     * @return user instance
     */
    private User getUserById(Long id) {
        return this.userMapById.get(id);
    }

    /**
     * Get a group instance by its ID
     *
     * @param id ID of group
     * @return group instance
     */
    private UserGroup getGroupById(Long id) {
        return this.groupMapById.get(id);
    }

    /**
     * Get the dual list model for the pick list to select access right groups
     *
     * @return the dual list model for the pick list to select access right groups
     */
    public DualListModel<UserGroup> getDualGroupAssignmentList() {
        return this.dualGroupAssignmentList;
    }

    /**
     * Set the dual list model for the pick list to select access right groups
     *
     * @param dualGroupAssignmentList the dual list for the pick list to select access right groups to set
     */
    public void setDualGroupAssignmentList(DualListModel<UserGroup> dualGroupAssignmentList) {
        // JSF would set an empty list if PickList was not displayed prior to save action
        if (SodaUtil.isTotalElementCountEqual(this.dualGroupAssignmentList, dualGroupAssignmentList)) {
            this.dualGroupAssignmentList = dualGroupAssignmentList;
        }
    }

    /**
     * Get the dual list model for the pick list to select access right users
     *
     * @return the dual list model for the pick list to select access right users
     */
    public DualListModel<User> getDualUserAssignmentList() {
        return this.dualUserAssignmentList;
    }

    /**
     * Set the dual list model for the pick list to select access right users
     *
     * @param dualUserAssignmentList the dual list for the pick list to select access right users to set
     */
    public void setDualUserAssignmentList(DualListModel<User> dualUserAssignmentList) {
        // JSF would set an empty list if PickList was not displayed prior to save action
        if (SodaUtil.isTotalElementCountEqual(this.dualUserAssignmentList, dualUserAssignmentList)) {
            this.dualUserAssignmentList = dualUserAssignmentList;
        }
    }

    /**
     * Get the dual list model for the pick list to select tags
     *
     * @return the dualTagList
     */
    public DualListModel<Tag> getDualTagList() {
        return dualTagList;
    }

    /**
     * Set the dual list model for the pick list to select tags
     *
     * @param dualTagList the dualTagList to set
     */
    public void setDualTagList(DualListModel<Tag> dualTagList) {
        this.dualTagList = dualTagList;
    }

    /**
     * Merge a data stock
     *
     * @param entry entry to merge
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    public boolean mergeStock(AbstractDataStock entry, boolean showMessage) {
        CommonDataStockDao dao = new CommonDataStockDao();
        try {
            this.setEntry(dao.merge(entry));
            this.dirty.stockModified();
            if (showMessage)
                store.depositMessage("stockHandler",
                        buildI18NFacesMessage("facesMsg.stock.changeSuccess", FacesMessage.SEVERITY_INFO, null));
            return true;
        } catch (MergeException e) {
            store.depositMessage("stockHandler",
                    buildI18NFacesMessage("facesMsg.stock.changeSuccess", FacesMessage.SEVERITY_INFO, null));
            return false;
        }
    }

    /**
     * Merge the stock access rights
     *
     * @param wrapperList list of wrapped rights
     * @return <code>true</code> on success, else <code>false</code>
     */
    private boolean mergeStockAccessRights(List<StockAccessRightWrapper> wrapperList) {
        StockAccessRightDao sarDao = new StockAccessRightDao();
        final List<StockAccessRight> sarList = new ArrayList<StockAccessRight>();
        for (StockAccessRightWrapper wrapper : wrapperList) {
            sarList.add(wrapper.getAccessRight());
        }
        try {
            sarDao.merge(sarList);
            // TODO faces msg
            return true;
        } catch (MergeException e) {
            // TODO faces msg
            return false;
        }
    }

    /**
     * Update the group access rights of the stock according to the target of {@link #getDualGroupAssignmentList()}.
     * Only for groups of that target list
     * access rights can be defined.
     */
    public void assignGroup() {
        this.loadAccessRights();
        this.handleAccessRightUpdate(AccessRightType.Group, this.dualGroupAssignmentList.getTarget(), this.accessRightsGroup);

    }

    /**
     * Update the user access rights of the stock according to the target of {@link #getDualUserAssignmentList()}. Only
     * for users of that target list
     * (and guests) access rights can be defined.
     */
    public void assignUser() {
        this.loadAccessRights();
        this.handleAccessRightUpdate(AccessRightType.User, this.dualUserAssignmentList.getTarget(), this.accessRightsUser);
    }

    /**
     * Process the group pick list and set value for {@link #isConfirmGroupRightDelete()}. <br />
     * <br />
     * This method is triggered by the &quot;OK&quot; JSF command button that is shown in
     * the dialog with the picklist for the selection of the groups getting access to the stock.
     */
    public void processGroupPickList() {
        this.confirmGroupRightDelete = this.determineConfirmDialogFlag(this.dualGroupAssignmentList.getTarget(), this.accessRightsGroup);
    }

    /**
     * Process the user pick list and set value for {@link #isConfirmUserRightDelete()} <br />
     * <br />
     * This method is triggered by the &quot;OK&quot; JSF command button that is shown in
     * the dialog with the picklist for the selection of the users getting access to the stock.
     */
    public void processUserPickList() {
        this.confirmUserRightDelete = this.determineConfirmDialogFlag(this.dualUserAssignmentList.getTarget(), this.accessRightsUser);
    }

    /**
     * Get the flag for confirmation of group right delete
     *
     * @return flag for confirmation of group right delete
     */
    public boolean isConfirmGroupRightDelete() {
        return this.confirmGroupRightDelete;
    }

    /**
     * Set the flag for confirmation of group right delete
     *
     * @param confirmGroupRightDelete flag to set
     */
    public void setConfirmGroupRightDelete(boolean confirmGroupRightDelete) {
        this.confirmGroupRightDelete = confirmGroupRightDelete;
    }

    /**
     * Get the flag for confirmation of user right delete
     *
     * @return flag for confirmation of user right delete
     */
    public boolean isConfirmUserRightDelete() {
        return this.confirmUserRightDelete;
    }

    /**
     * Set the flag for confirmation of user right delete
     *
     * @param confirmUserRightDelete flag to set
     */
    public void setConfirmUserRightDelete(boolean confirmUserRightDelete) {
        this.confirmUserRightDelete = confirmUserRightDelete;
    }

    /**
     * Handle the update of access rights.
     *
     * @param dualListTarget target of the dual list (user/group)
     * @param wrapperList    list of wrapper (user/group)
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private boolean determineConfirmDialogFlag(List<? extends ISecurityEntity> dualListTarget, List<StockAccessRightWrapper> wrapperList) {
        List<Long> targetList = new ArrayList<Long>();
        for (ISecurityEntity secEnt : dualListTarget) {
            targetList.add(secEnt.getId());
        }
        // find all access rights that have to be deleted
        for (StockAccessRightWrapper wrapper : wrapperList) {
            if (!targetList.contains(wrapper.getAccessRight().getUgId()) && !wrapper.isGuest()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle the update of access rights.
     *
     * @param arType         type of access right
     * @param dualListTarget target of the dual list (user/group)
     * @param wrapperList    list of wrapper (user/group)
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private boolean handleAccessRightUpdate(AccessRightType arType, List<? extends ISecurityEntity> dualListTarget, List<StockAccessRightWrapper> wrapperList) {
        List<Long> targetList = new ArrayList<Long>();
        for (ISecurityEntity secEnt : dualListTarget) {
            targetList.add(secEnt.getId());
        }
        List<Long> currentList = new ArrayList<Long>();
        for (StockAccessRightWrapper wrapper : wrapperList) {
            currentList.add(wrapper.getAccessRight().getUgId());
        }

        // find all access rights that have to be deleted
        List<StockAccessRight> toDelete = new ArrayList<StockAccessRight>();
        for (StockAccessRightWrapper wrapper : wrapperList) {
            if (!targetList.contains(wrapper.getAccessRight().getUgId()) && !wrapper.isGuest()) {
                toDelete.add(wrapper.getAccessRight());
            }
        }
        // find all user/groups that require access rights
        List<StockAccessRight> toPersist = new ArrayList<StockAccessRight>();
        for (ISecurityEntity secEnt : dualListTarget) {
            if (!currentList.contains(secEnt.getId())) {
                toPersist.add(new StockAccessRight(this.getEntry().getId(), arType, secEnt.getId(), 0));
            }
        }

        StockAccessRightDao sarDao = new StockAccessRightDao();
        try {
            sarDao.remove(toDelete);
        } catch (Exception e) {
            // TODO faces msg
            return false;
        }

        try {
            sarDao.persist(toPersist);
        } catch (Exception e) {
            // TODO faces msg
            return false;
        }

        this.setStockAndPermissionsDirty();

        // reload the access rights => now dual list + access right wrappers are in synch
        this.loadAccessRights();

        return true;
    }

    @SuppressWarnings("unchecked")
    public void onTagTransfer(TransferEvent event) {
        List<Tag> transferTags = (List<Tag>) event.getItems();
        List<Tag> addedTags = this.getEntry().getTags();
        if (event.isAdd()) {
            for (Tag tT : transferTags) {
                if (!tT.containedInList(addedTags))
                    this.getEntry().addTag(tT);
            }
        }
        if (event.isRemove()) {
            for (Tag tT : transferTags) {
                if (tT.containedInList(addedTags)) {
                    this.getEntry().removeTagByName(tT);
                }
            }
        }
    }

    /**
     * Currently only prepared for use in facelet, will be implemented later :)
     */
    public void removeSelected() {
    }

    /**
     * Get list of organizations
     *
     * @return list of organizations
     */
    public List<Organization> getOrganizations() {
        return this.organizations;
    }

    /**
     * Get list of group access rights
     *
     * @return list of group access rights
     */
    public List<StockAccessRightWrapper> getAccessRightsGroup() {
        return this.accessRightsGroup;
    }

    /**
     * Get list of user access rights
     *
     * @return list of user access rights
     */
    public List<StockAccessRightWrapper> getAccessRightsUser() {
        return this.accessRightsUser;
    }

    /**
     * Get list of protection types
     *
     * @return the list of protection types
     */
    public List<String> getProtectTypeColumns() {
        return this.protectTypeColumns;
    }

    /**
     * Set the list of protection types
     *
     * @param protectTypeColumns list of protection types to set
     */
    public void setProtectTypeColumns(List<String> protectTypeColumns) {
        this.protectTypeColumns = protectTypeColumns;
    }

    /**
     * Convenience method to set checkbox value
     *
     * @param protectionTypeColumn name of protection type (enum element)
     * @param accessRightValue     numerical value of protection type
     * @return <code>true</code> if access right is set, else <code>false</code>
     */
    public boolean hasRightTo(String protectionTypeColumn, int accessRightValue) {
        return ProtectionType.valueOf(protectionTypeColumn).isContained(accessRightValue);
    }

    /**
     * Get the stock children wrapper for processes
     *
     * @return stock children wrapper for processes
     */
    public StockChildrenWrapper<Process> getProcessWrapper() {
        return this.processWrapper;
    }

    /**
     * Get the stock children wrapper for LCIA methods
     *
     * @return stock children wrapper for LCIA methods
     */
    public StockChildrenWrapper<LCIAMethod> getLciaMethodWrapper() {
        return this.lciaMethodWrapper;
    }

    /**
     * Get the stock children wrapper for elementary flows
     *
     * @return stock children wrapper for elementary flows
     */
    public StockChildrenWrapper<ElementaryFlow> getElementaryFlowWrapper() {
        return this.elementaryFlowWrapper;
    }

    /**
     * Get the stock children wrapper for product flows
     *
     * @return stock children wrapper for product flows
     */
    public StockChildrenWrapper<ProductFlow> getProductFlowWrapper() {
        return this.productFlowWrapper;
    }

    /**
     * Get the stock children wrapper for flow properties
     *
     * @return stock children wrapper for flow properties
     */
    public StockChildrenWrapper<FlowProperty> getFlowPropertyWrapper() {
        return this.flowPropertyWrapper;
    }

    /**
     * Get the stock children wrapper for sources
     *
     * @return stock children wrapper for sources
     */
    public StockChildrenWrapper<Source> getSourceWrapper() {
        return this.sourceWrapper;
    }

    /**
     * Get the stock children wrapper for contacts
     *
     * @return stock children wrapper for contacts
     */
    public StockChildrenWrapper<Contact> getContactWrapper() {
        return this.contactWrapper;
    }

    /**
     * Get the stock children wrapper for unit groups
     *
     * @return stock children wrapper for unit groups
     */
    public StockChildrenWrapper<UnitGroup> getUnitGroupWrapper() {
        return this.unitGroupWrapper;
    }

    /**
     * Get the stock children wrapper for Life-Cycle Models
     *
     * @return stock children wrapper for Life-Cycle Models
     */
    public StockChildrenWrapper<LifeCycleModel> getLifeCycleModelWrapper() {
        return this.lifeCycleModelWrapper;
    }

    /**
     * Get the stock type
     *
     * @return stock type
     */
    public String getStockType() {
        return this.isRootStock() ? "rds" : "ds";
    }

    /**
     * Set the stock type flag (<code>rds</code> will cause {@link #isRootStock()} to be <code>true</code>, everything
     * else <code>false</code>)
     *
     * @param type type to set
     */
    public void setStockType(String type) {
        this.rootStock = StringUtils.equalsIgnoreCase("rds", type);
    }

    /**
     * Get the root stock flag
     *
     * @return root stock flag
     */
    public boolean isRootStock() {
        return this.rootStock;
    }

    /**
     * Convenience method for state of the remove button
     *
     * @return <code>true</code> if remove button shall be disabled, <code>false</code> otherwise
     */
    public boolean isRemoveButtonDisabled() {
        return true;
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
     * Get configuration bean
     *
     * @return configuration bean
     */
    public ConfigurationBean getConf() {
        return this.conf;
    }

    /**
     * Set configuration bean
     *
     * @param conf configuration bean to set
     */
    public void setConf(ConfigurationBean conf) {
        this.conf = conf;
    }

    /**
     * Get the available stocks bean
     *
     * @return available stocks bean
     */
    public AvailableStockHandler getAvailableStocks() {
        return this.availableStocks;
    }

    /**
     * Set available stocks bean
     *
     * @param availableStocks available stocks bean to set
     */
    public void setAvailableStocks(AvailableStockHandler availableStocks) {
        this.availableStocks = availableStocks;
    }

    /**
     * Create all stock children wrappers for a stock
     */
    public void createLazyDataModelsForStockDataSets() {
        this.processWrapper = StockChildrenWrapper.getProcessWrapper(this);
        this.lciaMethodWrapper = StockChildrenWrapper.getLCIAMethodWrapper(this);
        this.productFlowWrapper = StockChildrenWrapper.getProductFlowWrapper(this);
        this.elementaryFlowWrapper = StockChildrenWrapper.getElementaryFlowWrapper(this);
        this.flowPropertyWrapper = StockChildrenWrapper.getFlowPropertyWrapper(this);
        this.sourceWrapper = StockChildrenWrapper.getSourceWrapper(this);
        this.contactWrapper = StockChildrenWrapper.getContactWrapper(this);
        this.unitGroupWrapper = StockChildrenWrapper.getUnitGroupWrapper(this);
        this.lifeCycleModelWrapper = StockChildrenWrapper.getLifeCycleModelWrapper(this);
    }

    public void attachToProcess(AbstractDataStock abstractDataStockList, DataSet[] mergedProcess) {
        this.processWrapper.attachToStock(abstractDataStockList, mergedProcess, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditEntryUrl(URLGeneratorBean url, Long id) {
        return url.getStock().getEdit(id, getActiveTabIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCloseEntryUrl(URLGeneratorBean url) {
        return url.getStock().getManageList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewEntryUrl(URLGeneratorBean url) {
        return url.getStock().getNew();
    }

    /**
     *
     */
    public String noUsersHint(UserGroup g) {
        return SodaUtil.noUsersHint(g, this.getI18n());
    }

    /**
     * Get the list of candidate stocks
     *
     * @return list of candidate stocks
     */
    public List<IDataStockMetaData> getCandidateStocks() {
        return this.candidateStocks;
    }

    public List<String> getRootDataStocks() {
        List<String> options = new ArrayList<String>();

        for (IDataStockMetaData m : this.availableStocks.getRootStocksMeta())
            options.add(m.getName());

        return options;
    }

    /**
     * Save Entry and get edit URL
     *
     * @return URL to edit entry (including faces redirect if currently in {@link #isCreateView() create view}
     * @see #getEditEntryUrl(URLGeneratorBean, Long)
     */
    @Override
    public String saveEntry() {
        if (super.saveEntryInternal()) {
            String blub = this.getEditEntryUrl(super.urlGenerator, this.getEntry().getId()) + "&" + URLGeneratorBean.FACES_REDIRECT_TRUE;
            return blub;
        } else {
            return null;
        }
    }

    public void setStore(SessionStorageBean store) {
        this.store = store;
    }

    public void displayCachedMessage() {
        FacesMessage cachedMsg = store.retrieveMessage("stockHandler");
        if (cachedMsg != null)
            FacesContext.getCurrentInstance().addMessage(null, cachedMsg);
    }

    public List<IDataStockMetaData> getVisibleLogicalDataStockMDs() {
        return getAvailableStocks().getVisibleStocksMeta().stream()
                .filter(stm -> !stm.isRoot())
                .sorted()
                .collect(Collectors.toList());
    }

}
