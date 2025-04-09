package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.LCIAMethodDao;
import de.iai.ilcd.model.lciamethod.LCIAMethod;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Backing bean for LCIAMethod List View
 */
@ManagedBean(name = "lciamethodsHandler")
@ViewScoped
public class LCIAMethodsHandler extends AbstractDataSetsHandler<LCIAMethod, LCIAMethodDao> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -5952220742029661416L;

    /**
     * Initialize backing bean
     */
    public LCIAMethodsHandler() {
        super(LCIAMethod.class, new LCIAMethodDao());
    }

}
