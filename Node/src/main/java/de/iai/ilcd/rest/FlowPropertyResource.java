package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.FlowPropertyDao;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;

/**
 * REST Web Service for FlowProperty
 */
@Component
@Path("flowproperties")
public class FlowPropertyResource extends AbstractDataSetResource<FlowProperty> {

    public FlowPropertyResource() {
        super(DataSetType.FLOWPROPERTY, ILCDTypes.FLOWPROPERTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSetDao<FlowProperty, ?, ?> getFreshDaoInstance() {
        return new FlowPropertyDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXMLTemplatePath() {
        return "/xml/flowproperty.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataSetTypeName() {
        return "flow property";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<FlowProperty> getDataSetType() {
        return FlowProperty.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHTMLDatasetDetailTemplatePath() {
        return "/html/flowproperty.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean userRequiresDatasetDetailRights() {
        return false;
    }
}
