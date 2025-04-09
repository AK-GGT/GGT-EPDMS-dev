package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.UnitGroupDao;
import de.iai.ilcd.model.unitgroup.UnitGroup;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * Backing bean for unit group list view
 */
@ManagedBean()
@ViewScoped
public class UnitgroupsHandler extends AbstractDataSetsHandler<UnitGroup, UnitGroupDao> implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 3028750324308603552L;

    /**
     * Initialize handler
     */
    public UnitgroupsHandler() {
        super(UnitGroup.class, new UnitGroupDao());
    }

}
