package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.service.client.ILCDServiceClientException;
import de.iai.ilcd.model.common.PushConfig;
import de.iai.ilcd.model.common.PushTarget;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.rest.util.NotAuthorizedException;
import de.iai.ilcd.service.exception.JobNotScheduledException;
import de.iai.ilcd.service.exception.UserNotExistingException;
import de.iai.ilcd.service.task.push.PushDataStockTask;
import de.iai.ilcd.service.task.push.PushDataStockTaskConfig;
import de.iai.ilcd.util.DependenciesOptions;
import de.iai.ilcd.webgui.controller.url.URLGeneratorBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler for push configuration entries.
 *
 * @author sarai
 */
@ManagedBean
@ViewScoped
public class PushConfigHandler extends AbstractAdminEntryHandler<PushConfig> {

    /**
     *
     */
    private static final long serialVersionUID = 1991027201516698087L;

    /*
     * The logger for logging data
     */
    protected final Logger log = LogManager.getLogger(this.getClass());
    /*
     * The dao for finding push configs
     */
    private final PushConfigDao pushConfigDao = new PushConfigDao();
    /*
     * The decidable dependency options
     */
    private final DependenciesOptions depOptions = new DependenciesOptions();
    /*
     * The list of decidable push target entries
     */
    private List<PushTarget> pushTargets = new ArrayList<>();
    /*
     * The password for connection to target node
     */
    private String password;
    /*
     * The handler for managing source stocks
     */
    @ManagedProperty(value = "#{stockListHandler}")
    private StockListHandler stockListHandler = new StockListHandler();

    /*
     * Flag for checking whether current view is editable
     */
    private boolean edit = false;

    /*
     * Flag for checking whether current config is already pushed
     */
    private boolean pushed = false;

    public PushConfigHandler() {
        super();
    }

    /**
     * Checks if current view of push config entry is editable
     */
    public boolean isEdit() {
        return edit;
    }

    /**
     * sets editable flag of current view to given flag
     */
    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    /**
     * Gets the dependencies option.
     *
     * @return dependencies option for push configuration
     */
    public DependenciesOptions getDependenciesOptions() {
        return this.depOptions;
    }

    /**
     * Gets list of all selectable push targets.
     *
     * @return list of all saved push targets
     */
    public List<PushTarget> getPushTargets() {
        return this.pushTargets;
    }

    /**
     * Sets the list of all push targets.
     *
     * @param pushTargets the list of all push targets
     */
    public void setPushTargets(List<PushTarget> pushTargets) {
        this.pushTargets = pushTargets;
    }

    /**
     * Gets the current stock handler for managing available source stocks.
     *
     * @return A StockHandler for managing available source stocks
     */
    public StockListHandler getStockListHandler() {
        return stockListHandler;
    }

    /**
     * Sets the StockHandler for managing source stocks.
     *
     * @param stockListHandler The stock handler for managing source stocks
     */
    public void setStockListHandler(StockListHandler stockListHandler) {
        this.stockListHandler = stockListHandler;
    }

    /**
     * Gets entered password of target node.
     *
     * @return The entered password of target node
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the entered password of target node.
     *
     * @param password The entered password for target node
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the current push configuration entry.
     *
     * @return An entry of the current push configuration
     */
    public PushConfig getPushConfig() {
        return this.getEntry();
    }

    /**
     * Sets the current push configuration entry.
     *
     * @param entry The entry of current push configuration
     */
    public void setPushConfig(PushConfig entry) {
        this.setEntry(entry);
    }

    /**
     * Creates a new push configuration entry if all entries of form were filled
     * in correctly.
     *
     * @return true if name of push configuration did not exist yet and if entry
     * is successfully stored in data table.
     */
    public boolean createPushConfig() {
        PushConfig pushConfig = this.getEntry();
        if (pushConfig.getDependenciesMode() == null) {
            this.addI18NFacesMessage("facesMsg.pushConfig.noDependency", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        if (pushConfig.getName() == null) {
            this.addI18NFacesMessage("facesMsg.pushConfig.noName", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        if (pushConfig.getSource() == null) {
            this.addI18NFacesMessage("facesMsg.pushConfig.noSource", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        if (pushConfig.getTarget() == null) {
            this.addI18NFacesMessage("facesMsg.pushConfig.noTarget", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        PushConfig existingPushConfig = this.pushConfigDao.getPushConfig(pushConfig.getName());
        if (existingPushConfig != null) {
            this.addI18NFacesMessage("facesMsg.pushConfig.alreadyExists", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        try {
            this.pushConfigDao.persist(pushConfig);
            this.addI18NFacesMessage("facesMsg.pushConfig.createSuccess", FacesMessage.SEVERITY_INFO);
            return true;
        } catch (PersistException pe) {
            log.warn("Could not persist PushConfig entry!");
            if (log.isDebugEnabled()) {
                pe.printStackTrace();
            }
            this.addI18NFacesMessage("facesMsg.pushConfig.createError", FacesMessage.SEVERITY_ERROR);
            return false;
        }
    }

    /**
     * Changes the current push configuration entry.
     *
     * @return true if push configuration entry does already exist and new data
     * are successfully merged into existing entry
     */
    public boolean changePushConfig() {
        final PushConfig pushConfig = this.getEntry();
        final Long pushConfigId = pushConfig.getId();
        if (pushConfig.getName().strip().isEmpty()) {
            this.addI18NFacesMessage("facesMsg.pushConfig.noName", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        if (pushConfig.getDependenciesMode() == null) {
            this.addI18NFacesMessage("facesMsg.pushConfig.noDependency", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        if (pushConfig.getTarget() == null) {
            this.addI18NFacesMessage("facesMsg.pushConfig.noTarget", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        if (pushConfig.getSource() == null) {
            this.addI18NFacesMessage("facesMsg.pushConfig.noSource", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        PushConfig existingPushConfig = this.pushConfigDao.getPushConfig(pushConfig.getName());
        if (existingPushConfig != null && !existingPushConfig.getId().equals(pushConfigId)) {
            this.addI18NFacesMessage("facesMsg.pushConfig.alreadyExists", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        try {
            this.setEntry(this.pushConfigDao.merge(pushConfig));
            this.addI18NFacesMessage("facesMsg.pushConfig.changeSuccess", FacesMessage.SEVERITY_INFO);
            return true;
        } catch (Exception e) {
            log.warn("Could not change PushConfigEntry!");
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            this.addI18NFacesMessage("facesMsg.saveDataError", FacesMessage.SEVERITY_ERROR);
            return false;
        }
    }

    /**
     * Pushes source stock with dependency mode to target stock of target node.
     */
    public final void pushDataStock() {
        try {
            log.debug("pushing data stock on handler");
            PushDataStockTaskConfig pushTaskConfig = new PushDataStockTaskConfig(this.getEntry(), password);
            PushDataStockTask pushTask = new PushDataStockTask(pushTaskConfig, this.getCurrentUserOrNull());
            pushTask.runIn(this.getGlobalQueue())
                    .message(this);

        } catch (SchedulerException se) {
            log.warn("Could not schedule push job!");
            if (log.isDebugEnabled()) {
                se.printStackTrace();
            }
            this.addI18NFacesMessage("facesMsg.push.networkError", FacesMessage.SEVERITY_ERROR);
        } catch (PersistException e) {
            this.addI18NFacesMessage("facesMsg.jobScheduling.persistError", FacesMessage.SEVERITY_ERROR);
            log.warn("Could not persist JobMetaData entry of PushConfig job!");
            if (log.isDebugEnabled())
                e.printStackTrace();
        } catch (MergeException e) {
            this.addI18NFacesMessage("facesMsg.jobScheduling.stateNotUpdated", FacesMessage.SEVERITY_ERROR);
            log.warn("Could not update job state of push job!");
            if (log.isDebugEnabled())
                e.printStackTrace();
        } catch (UserNotExistingException e) {
            this.addI18NFacesMessage("facesMsg.jobScheduling.userNotExisting", FacesMessage.SEVERITY_ERROR);
            log.warn("Current user does not exist!");
            if (log.isDebugEnabled())
                e.printStackTrace();
        } catch (JobNotScheduledException e) {
            this.addI18NFacesMessage("facesMsg.jobScheduling.jobNotScheduled", FacesMessage.SEVERITY_ERROR);
            log.warn("Push job could not be scheduled!");
            if (log.isDebugEnabled())
                e.printStackTrace();
        } catch (ILCDServiceClientException e) {
            this.addI18NFacesMessage("facesMsg.push.networkError", FacesMessage.SEVERITY_ERROR);
            log.warn("Could not initialize network client!");
            if (log.isDebugEnabled())
                e.printStackTrace();
        } catch (NotAuthorizedException e) {
            log.warn("User is not authorized!");
            if (log.isDebugEnabled())
                e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        pushed = true;
    }

    /**
     * Gets the number of process data sets contained in source data stock.
     *
     * @return The number of process data set contained in source data stock
     */
    public Long getDataSetCount() {
        if (this.getEntry() == null) {
            log.warn("Cannot load dataset count for source stock: Push config null");

            return null;

        } else if (this.getEntry().getSource() == null) {
            log.error("Cannot load dataset count for source stock: Data stock is null");

            return null;
        }
        final var sourceStock = this.getEntry().getSource();

        long totalCount = 0;
        for (final var daoType : DataSetDaoType.values()) {
            final var dao = daoType.getDao();
            final var typeSpecificCount = dao.getCount(sourceStock);
            totalCount += typeSpecificCount;
        }

        return totalCount;
    }

    public Long getImportedDataSetsAfterPush() {
        if (this.getEntry() == null) {
            log.warn("Cannot load imported-after-last-push count: Push config is null");

            return null;

        } else if (this.getEntry().getSource() == null) {
            log.error("Cannot load imported-after-last-push count: Data stock is null");

            return null;
        }

        final var lastPushDate = this.getEntry().getLastPushDate() != null ? this.getEntry().getLastPushDate().toInstant()
                : Instant.ofEpochMilli(0);

        long totalCount = 0;
        for (final var daoType : DataSetDaoType.values()) {
            final var dao = daoType.getDao();
            final var typeSpecificCount = dao.getCountImportedAfter(lastPushDate, getEntry().getSource());
            totalCount += typeSpecificCount;
        }

        log.debug("getImportedDataSetsAfterPush: {}", totalCount);
        return totalCount;
    }

    /**
     * Gets the number of process data sets contained in source data set as
     * String.
     *
     * @return The number of process data set contained in source data stock as
     * String
     */
    public String getDataSetCountAsString() {
        return getDataSetCount().toString();
    }

    public String getImportedDataSetsAfterPushAsString() {
        Long count = getImportedDataSetsAfterPush();

        return count != null ? count.toString() : "";
    }

    public boolean isFavourite() {
        return this.getEntry().isFavourite();
    }

    public void setFavourite(boolean favourite) {
        this.getEntry().setFavourite(favourite);
    }

    public boolean isPushed() {
        return pushed;
    }

    public void setPushed(boolean pushed) {
        this.pushed = pushed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postEntrySet() {
        PushTargetDao pTDao = new PushTargetDao();
        pushTargets = pTDao.getAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PushConfig createEmptyEntryInstance() {
        return new PushConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PushConfig loadEntryInstance(long id) throws Exception {
        return this.pushConfigDao.getById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConstruct() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createEntry() {
        return createPushConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changeAttachedEntry() {
        return changePushConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditEntryUrl(URLGeneratorBean url, Long id) {
        return url.getPushConfig().getEdit(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCloseEntryUrl(URLGeneratorBean url) {
        return url.getPushConfig().getShowList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewEntryUrl(URLGeneratorBean url) {
        return url.getPushConfig().getNew();
    }

}
