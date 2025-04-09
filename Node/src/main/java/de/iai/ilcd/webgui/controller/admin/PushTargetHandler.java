package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.service.client.ILCDServiceClientException;
import de.fzk.iai.ilcd.service.client.impl.ILCDNetworkClient;
import de.fzk.iai.ilcd.service.client.impl.vo.DataStockList;
import de.fzk.iai.ilcd.service.client.impl.vo.DataStockVO;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.PushTarget;
import de.iai.ilcd.model.common.TargetStock;
import de.iai.ilcd.model.dao.PersistException;
import de.iai.ilcd.model.dao.PushTargetDao;
import de.iai.ilcd.webgui.controller.url.URLGeneratorBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.SelectableDataModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The managing class for {@link PushTarget}.
 *
 * @author sarai
 */
@ManagedBean
@ViewScoped
public class PushTargetHandler extends AbstractAdminEntryHandler<PushTarget>
        implements SelectableDataModel<TargetStock> {
    /*
     * The generated id.
     */
    private static final long serialVersionUID = 3811308703366623533L;
    private static final String REST_SERVLET_PREFIX = "resource/";
    /*
     * Logger for logging data.
     */
    protected final Logger log = LogManager.getLogger(this.getClass());
    /*
     * The dao for finding Push Target entries.
     */
    private PushTargetDao pushTargetDao = new PushTargetDao();

    /*
     * The target stock to where data sets will be sent
     */
    private TargetStock selectedTargetStock = new TargetStock();

    /*
     * The list of target data stocks to choose from
     */
    private List<TargetStock> targetStockList = new ArrayList<TargetStock>();

    private String password;

    private boolean editable = false;

    /**
     * Gets password for connection to target node.
     *
     * @return password for connection to target noce user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets password for connection to target node.
     *
     * @param password Password for target node user
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets Push Target entry.
     *
     * @return Given pushTarget entry.
     */
    public PushTarget getPushTarget() {
        return this.getEntry();
    }

    /**
     * Sets Push Target entry.
     *
     * @param pushTarget
     */
    public void setPushTarget(PushTarget pushTarget) {
        this.setEntry(pushTarget);
    }


    /**
     * Gets selected target stock.
     *
     * @return The selected target stock
     */
    public TargetStock getSelectedTargetStock() {
        return this.selectedTargetStock;
    }

    /**
     * Sets the selected target stock.
     *
     * @param selectedTargetStock The selected target stock
     */
    public void setSelectedTargetStock(TargetStock selectedTargetStock) {
        this.selectedTargetStock = selectedTargetStock;
    }

    /**
     * Gets list of root data stocks of target node.
     *
     * @return a list of target stocks for choosing desired target stock.
     */
    public List<TargetStock> getTargetStockList() {
        return targetStockList;
    }

    /**
     * Sets list of root data stocks of target node.
     *
     * @param targetStockList
     */
    public void setTargetStockList(List<TargetStock> targetStockList) {
        this.targetStockList = targetStockList;
    }

    /**
     * Checks if current view is editable.
     *
     * @return true if current view iseditable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Changes the editable flag of current view.
     */
    public void changeEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Shows all possible root data stocks of given target node.
     */
    public void showStocks() {
        targetStockList = new ArrayList<TargetStock>();
        selectedTargetStock = null;
        try {

            String baseUrl = new String(this.getEntry().getTargetURL());
            if (!this.getEntry().getTargetURL().endsWith("/")) {
                baseUrl = baseUrl + "/";
            }
            if (!baseUrl.endsWith(REST_SERVLET_PREFIX))
                baseUrl += REST_SERVLET_PREFIX;
            ILCDNetworkClient client = new ILCDNetworkClient(baseUrl,
                    this.getEntry().getLogin(), password);
            client.setOrigin(ConfigurationService.INSTANCE.getNodeInfo().getBaseURLwithResource());
            this.getEntry().setTargetName(client.getNodeInfo().getName());
            this.getEntry().setTargetID(client.getNodeInfo().getNodeID());
            this.getEntry().setTargetURL(client.getNodeInfo().getBaseURL());
            if (!client.getAuthenticationStatus().isAuthenticated()) {
                this.addI18NFacesMessage("facesMsg.login.invalidCredentials", FacesMessage.SEVERITY_ERROR);
                return;
            }
            log.debug("Created new client.");
            DataStockList dsList = client.getDataStocks();
            List<DataStockVO> dStocks = dsList.getDataStocks();
            if (dStocks != null) {
                for (DataStockVO dsVo : dStocks) {
                    if (dsVo.isRoot()) {
                        TargetStock tmpStock = new TargetStock();
                        tmpStock.setDsName(dsVo.getShortName());
                        tmpStock.setDsDescription(dsVo.getDescription().getValue());
                        tmpStock.setDsUuid(dsVo.getUuid());
                        targetStockList.add(tmpStock);
                    }
                }
            }
        } catch (ILCDServiceClientException e) {
            this.addI18NFacesMessage("facesMsg.push.networkError", FacesMessage.SEVERITY_ERROR);
            log.warn("Given URL is not leading to another node!");
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            log.warn("Could not get data stocks!");
            if (log.isDebugEnabled())
                e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getRowKey(TargetStock object) {
        return object.getDsUuid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetStock getRowData(String rowKey) {
        for (TargetStock object : targetStockList) {
            if (object.getDsUuid().equals(rowKey)) {
                return object;
            }
        }
        return null;
    }

    /**
     * Creates a new PushTarget entry with given information.
     *
     * @return true, if creation of new PushTarget entry was successful.
     */
    public boolean createPushTarget() {


        PushTarget pushTarget = this.getEntry();

        if (pushTarget.getName() == null) {
            this.addI18NFacesMessage("facesMsg.pushTarget.noName", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        if (selectedTargetStock == null) {
            this.addI18NFacesMessage("facesMsg.pushTarget.noTargetStock", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        if (pushTarget.getTargetURL() == null) {
            this.addI18NFacesMessage("facesMsg.pushTarget.noTargetURL", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        PushTarget existingPushTarget = this.pushTargetDao.getPushTarget(pushTarget.getName());

        if (existingPushTarget != null) {
            this.addI18NFacesMessage("facesMsg.pushTarget.alreadyExists", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        try {

            this.getEntry().setTargetDsName(selectedTargetStock.getDsName());
            this.getEntry().setTargetDsUuid(selectedTargetStock.getDsUuid());
            log.debug("Trying to persist.");
            this.pushTargetDao.persist(pushTarget);
            this.addI18NFacesMessage("facesMsg.pushTarget.createSuccess", FacesMessage.SEVERITY_INFO);
            return true;
        } catch (PersistException e) {
            log.warn("Could not persist new PushTarget entry!");
            if (log.isDebugEnabled())
                e.printStackTrace();
            this.addI18NFacesMessage("facesMsg.pushTarget.createError", FacesMessage.SEVERITY_ERROR);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postEntrySet() {
    }

    /**
     * Changes Push Target Entry with given changes.
     *
     * @return true, if changing of PushTarget entry was successful
     */
    public boolean changePushTarget() {
        final PushTarget pushTarget = this.getEntry();
        if (selectedTargetStock != null) {
            pushTarget.setTargetDsName(selectedTargetStock.getDsName());
            pushTarget.setTargetDsUuid(selectedTargetStock.getDsUuid());
        } else {
            this.addI18NFacesMessage("facesMsg.pushTarget.noTargetStock", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        final Long pushTargetId = pushTarget.getId();
        PushTarget existingPushTarget = this.pushTargetDao.getPushTarget(pushTarget.getName());
        if (existingPushTarget != null && !(existingPushTarget.getId().equals(pushTargetId))) {
            this.addI18NFacesMessage("facesMsg.pushTarget.alreadyExists", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        try {
            this.setEntry(this.pushTargetDao.merge(pushTarget));
            this.addI18NFacesMessage("facesMsg.pushTarget.changeSuccess", FacesMessage.SEVERITY_INFO);
            return true;
        } catch (Exception e) {
            log.warn("Could not change PushTarget entry!");
            if (log.isDebugEnabled())
                e.printStackTrace();
            this.addI18NFacesMessage("facesMsg.saveDataError", FacesMessage.SEVERITY_ERROR);
            return false;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PushTarget createEmptyEntryInstance() {
        return new PushTarget();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PushTarget loadEntryInstance(long id) throws Exception {
        return this.pushTargetDao.getById(id);
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
        return createPushTarget();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changeAttachedEntry() {
        return changePushTarget();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditEntryUrl(URLGeneratorBean url, Long id) {
        return url.getPushTarget().getEdit(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCloseEntryUrl(URLGeneratorBean url) {
        return url.getPushTarget().getShowList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewEntryUrl(URLGeneratorBean url) {
        return url.getPushTarget().getNew();
    }
}
