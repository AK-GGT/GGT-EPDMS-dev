package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.LCIAMethodDao;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;

/**
 * REST web service for LCIAMethods
 */
@Component
@Path("lciamethods")
public class LCIAMethodResource extends AbstractDataSetResource<LCIAMethod> {

    public LCIAMethodResource() {
        super(DataSetType.LCIAMETHOD, ILCDTypes.LCIAMETHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSetDao<LCIAMethod, ?, ?> getFreshDaoInstance() {
        return new LCIAMethodDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXMLTemplatePath() {
        return "/xml/lciamethod.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataSetTypeName() {
        return "LCIA method";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<LCIAMethod> getDataSetType() {
        return LCIAMethod.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHTMLDatasetDetailTemplatePath() {
        return "/html/lciamethod.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean userRequiresDatasetDetailRights() {
        return true;
    }

}
