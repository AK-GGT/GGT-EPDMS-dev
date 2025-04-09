package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.source.Source;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * Backing bean for source list view
 */
@ManagedBean()
@ViewScoped
public class SourcesHandler extends AbstractDataSetsHandler<Source, SourceDao> implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -363205804984340433L;

    /**
     * Initialize handler
     */
    public SourcesHandler() {
        super(Source.class, new SourceDao());
    }

}
