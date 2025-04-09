package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.ConfigurationItem;
import de.iai.ilcd.model.dao.ConfigurationItemDao;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.PersistException;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.service.glad.GLADDatabaseProperties;
import de.iai.ilcd.service.glad.GLADDatabasePropertiesDao;
import de.iai.ilcd.service.glad.model.*;
import de.iai.ilcd.util.DBConfigurationUtil;
import de.iai.ilcd.webgui.controller.AbstractHandler;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * The Handler object for global GLAD properties.
 * This manages all configurable global properties which can be used for GLAD registration.
 *
 * @author sarai
 */
@ViewScoped
@ManagedBean(name = "gladAdminHandler")
public class GLADPropertiesAdminHandler extends AbstractHandler {

    /**
     *
     */
    private static final long serialVersionUID = -5989875772147120836L;

    GLADDatabasePropertiesDao gladDao = new GLADDatabasePropertiesDao();
    GLADDatabaseProperties prop;

    private String dataprovider;

    private ConfigurationItemDao confDao = new ConfigurationItemDao();

    /**
     * {@inheritDoc}
     */
    @PostConstruct
    public void init() {
        SecurityUtil.assertSuperAdminPermission();
        prop = gladDao.getById(1);
        if (prop == null) {
            prop = new GLADDatabaseProperties();
        }
        ConfigurationItem confDataprovider = confDao.getConfigurationItem("glad.dataprovider");
        if (confDataprovider != null)
            this.dataprovider = confDataprovider.getStringvalue();
    }

    /**
     * Saves the current configuration and persists changes to database.
     */
    public void saveConfiguration() {
        GLADDatabaseProperties oldProp = gladDao.getById(1);
        if (oldProp != null) {
            try {
                gladDao.merge(prop);
                this.addI18NFacesMessage("facesMsg.config.saveSuccess", FacesMessage.SEVERITY_INFO);
            } catch (MergeException me) {
                this.addI18NFacesMessage("facesMsg.config.saveError", FacesMessage.SEVERITY_ERROR);
            }
        } else {
            try {
                gladDao.persist(prop);
                this.addI18NFacesMessage("facesMsg.config.saveSuccess", FacesMessage.SEVERITY_INFO);
            } catch (PersistException pe) {
                this.addI18NFacesMessage("facesMsg.config.saveError", FacesMessage.SEVERITY_ERROR);
            }
        }
        if (!DBConfigurationUtil.setDataprovider(dataprovider)) {
            this.addI18NFacesMessage("facesMsg.config.peristexception", FacesMessage.SEVERITY_ERROR);
        }

    }

    public RepresentativenessType[] getRepresentativenessTypes() {
        return RepresentativenessType.values();
    }

    public RepresentativenessType getRepresentativenessType() {
        return prop.getRepresentativenessType();
    }

    public void setRepresentativenessType(RepresentativenessType representativenessType) {
        prop.setRepresentativenessType(representativenessType);
    }

    public ReviewSystem[] getReviewSystems() {
        return ReviewSystem.values();
    }

    public ReviewSystem getReviewSystem() {
        return prop.getReviewSystem();
    }

    public void setReviewSystem(ReviewSystem reviewSystem) {
        prop.setReviewSystem(reviewSystem);
    }

    public BiogenicCarbonModeling[] getBiogenicCarbonModelings() {
        return BiogenicCarbonModeling.values();
    }

    public BiogenicCarbonModeling getBiogenicCarbonModeling() {
        return prop.getBiogenicCarbonModeling();
    }

    public void setBiogenicCarbonModeling(BiogenicCarbonModeling biogenicCarbonModeling) {
        prop.setBiogenicCarbonModeling(biogenicCarbonModeling);
    }

    public EndOfLifeModeling[] getEndOfLifeModelings() {
        return EndOfLifeModeling.values();

    }

    public EndOfLifeModeling getEndOfLifeModeling() {
        return this.prop.getEndOfLifeModeling();
    }

    public void setEndOfLifeModeling(EndOfLifeModeling endOfLifeModeling) {
        prop.setEndOfLifeModeling(endOfLifeModeling);
    }

    public WaterModeling[] getWaterModelings() {
        return WaterModeling.values();
    }

    public WaterModeling getWaterModeling() {
        return this.prop.getWaterModeling();
    }

    public void setWaterModeling(WaterModeling waterModeling) {
        prop.setWaterModeling(waterModeling);
    }

    public InfrastructureModeling[] getInfrastructureModelings() {
        return InfrastructureModeling.values();
    }

    public InfrastructureModeling getInfrastructureModeling() {
        return this.prop.getInfrastructureModeling();
    }

    public void setInfrastructureModeling(InfrastructureModeling infrastructureModeling) {
        prop.setInfrastructureModeling(infrastructureModeling);

    }

    public EmissionModeling[] getEmissionModelings() {
        return EmissionModeling.values();
    }

    public EmissionModeling getEmissionModeling() {
        return this.prop.getEmissionModeling();
    }

    public void setEmissionModeling(EmissionModeling emissionModeling) {
        prop.setEmissionModeling(emissionModeling);
    }

    public CarbonStorageModeling[] getCarbonStorageModelings() {
        return CarbonStorageModeling.values();
    }

    public CarbonStorageModeling getCarbonStorageModeling() {
        return this.prop.getCarbonStorageModeling();
    }

    public void setCarbonStorageModeling(CarbonStorageModeling carbonStorageModeling) {
        prop.setCarbonStorageModeling(carbonStorageModeling);
    }

    public String getDataprovider() {
        return this.dataprovider;
    }


    public void setDataprovider(String dataprovider) {
        this.dataprovider = dataprovider;
    }
}
