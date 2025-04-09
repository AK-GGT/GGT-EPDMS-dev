package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.LifeCycleModelDao;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

@ManagedBean()
@ViewScoped
public class LifeCycleModelsHandler extends AbstractDataSetsHandler<LifeCycleModel, LifeCycleModelDao>
        implements Serializable {

    private static final long serialVersionUID = 590796921862986133L;

    public LifeCycleModelsHandler() {
        super(LifeCycleModel.class, new LifeCycleModelDao());
    }
}
