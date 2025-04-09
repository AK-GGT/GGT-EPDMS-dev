package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.UnitGroupDao;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;

/**
 * REST Web Service for UnitGroup
 */
@Component
@Path("unitgroups")
public class UnitGroupResource extends AbstractDataSetResource<UnitGroup> {

    public UnitGroupResource() {
        super(DataSetType.UNITGROUP, ILCDTypes.UNITGROUP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSetDao<UnitGroup, ?, ?> getFreshDaoInstance() {
        return new UnitGroupDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXMLTemplatePath() {
        return "/xml/unitgroup.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataSetTypeName() {
        return "unit group";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<UnitGroup> getDataSetType() {
        return UnitGroup.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHTMLDatasetDetailTemplatePath() {
        return "/html/unitgroup.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean userRequiresDatasetDetailRights() {
        return false;
    }

}
