package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.source.Source;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple bean to hold the available compliance systems.
 */
@SessionScoped
@ManagedBean(name = "AvailableComplianceSystemsHandler")
public class AvailableComplianceSystemsHandler implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 7262479997721885748L;

    /**
     * Cached List of available Compliance Systems
     */
    private List<Source> availableCompSystems = new ArrayList<Source>();

    private SourceDao sourceDao = new SourceDao();

    public AvailableComplianceSystemsHandler() {
        loadAvailableComplianceSystems();
    }

    private void loadAvailableComplianceSystems() {
        this.availableCompSystems = sourceDao.getComplianceSystems();
    }

    public void reload() {
        this.loadAvailableComplianceSystems();
    }

    public Source getByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty())
            return null;

        for (Source cs : availableCompSystems)
            if (cs.getUuidAsString().equals(uuid))
                return cs;

        return null;
    }

    public List<Source> getAvailableCompSystems() {
        return availableCompSystems;
    }

}
