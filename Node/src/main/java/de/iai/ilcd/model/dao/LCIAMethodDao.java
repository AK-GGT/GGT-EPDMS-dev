package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.service.model.ILCIAMethodListVO;
import de.fzk.iai.ilcd.service.model.ILCIAMethodVO;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.common.Uuid;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lciamethod.LCIAMethodCharacterisationFactor;
import org.apache.velocity.tools.generic.ValueParser;

import javax.persistence.NoResultException;
import java.util.*;

/**
 * Data access object for {@link LCIAMethod LCIA methods}
 */
public class LCIAMethodDao extends DataSetDao<LCIAMethod, ILCIAMethodListVO, ILCIAMethodVO> {

    /**
     * Create the data access object for {@link LCIAMethod LCIA methods}
     */
    public LCIAMethodDao() {
        super("LCIAMethod", LCIAMethod.class, ILCIAMethodListVO.class, ILCIAMethodVO.class, DataSetType.LCIAMETHOD);
    }

    @Override
    protected String getDataStockField() {
        return "lciaMethods";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params, List<String> whereClauses,
                                                                    Map<String, Object> whereParamValues) {
        // nothing to do beside the defaults
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getQueryStringOrderJpql(String typeAlias, String sortString, boolean sortOrder) {
        if ("type.value".equals(sortString)) {
            return typeAlias + ".type";
        } else if ("timeInformation.referenceYear.value".equals(sortString)) {
            return buildOrderBy(typeAlias, "timeInformation.referenceYear.defaultValue", typeAlias, "nameCache", sortOrder);
        } else if ("timeInformation.duration.value".equals(sortString)) {
            return buildOrderBy(typeAlias, "timeInformation.duration.defaultValue", typeAlias, "nameCache", sortOrder);
        } else {
            return super.getQueryStringOrderJpql(typeAlias, sortString, sortOrder);
        }
    }

    /**
     * Link the flow instances in the database with the flow global references of this LCIAMethod
     *
     * @param method {@link LCIAMethod}
     */
    @Override
    protected void preCheckAndPersist(LCIAMethod method) {
        for (LCIAMethodCharacterisationFactor cf : method.getCharactarisationFactors()) {

            if (cf.getReferencedFlowInstance() != null) {
                continue; // flow already associated, nothing to do
            }

            GlobalReference flowGlobalReference = cf.getFlowGlobalReference();
            if (flowGlobalReference == null) {
                continue; // no global reference available, so no mapping with UUID in this characterisation factor can
                // be tried --> nothing to do
            }

            String flowUuidStr = null;
            Uuid flowUuid = flowGlobalReference.getUuid();
            if (flowGlobalReference.getUuid() == null) {
                // sadly no UUID attribute in the global reference
                // but we can try our luck with the URI
                String uriStr = flowGlobalReference.getUri();
                if (uriStr == null) {
                    continue; // bad luck: no URL :-(
                }
                String[] splittedUri = uriStr.split("_");
                if (splittedUri.length <= 2) {
                    continue; // URI has not enough parts in order to contain an UUID: bad luck again :-(
                }
                flowUuidStr = splittedUri[splittedUri.length - 2];
            } else {
                flowUuidStr = flowUuid.getUuid();
            }
            // all right ... we found ourselves an UUID string, let's try to find the flow for it in the DB
            ElementaryFlow flowInstance = null;
            try {
                ElementaryFlowDao flowDao = new ElementaryFlowDao();
                flowInstance = flowDao.getByUuid(flowUuidStr);

                // if no exception was thrown: congratulations, we found the flow!
                cf.setReferencedFlowInstance(flowInstance);
            } catch (NoResultException ex) {
                // close, but not close enough. UUID string was found, but no flow
                // with this UUID in the database: bad luck after all :-(
            }
        }
    }

    /* (non-Javadoc)
     * @see de.iai.ilcd.model.dao.DataSetDao#getDependencies(de.iai.ilcd.model.common.DataSet, de.iai.ilcd.model.dao.DependenciesMode)
     */
    @Override
    public Set<DataSet> getDependencies(DataSet dataset, DependenciesMode mode) {
        return new HashSet<>();
    }

    @Override
    public Set<DataSet> getDependencies(DataSet dataSet, DependenciesMode depMode, Collection<String> ignoreList) {
        return new HashSet<>();
    }

    @Override
    public LCIAMethod getSupersedingDataSetVersion(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

}
