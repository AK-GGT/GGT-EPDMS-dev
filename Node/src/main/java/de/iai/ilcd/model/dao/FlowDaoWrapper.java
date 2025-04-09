package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.service.model.IFlowListVO;
import de.fzk.iai.ilcd.service.model.IFlowVO;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.flow.Flow;
import org.apache.commons.lang.NotImplementedException;
import org.apache.velocity.tools.generic.ValueParser;

import java.util.*;

/**
 * @author oli
 */
public class FlowDaoWrapper extends DataSetDao<Flow, IFlowListVO, IFlowVO> {

    public FlowDaoWrapper() {
        super("Flow", Flow.class, IFlowListVO.class, IFlowVO.class, DataSetType.FLOW);
    }

    @Override
    protected String getDataStockField() {
        return null;
    }

    @Override
    protected void preCheckAndPersist(Flow dataSet) {
        throw new NotImplementedException();
    }

    @Override
    protected String getQueryStringOrderJpql(String typeAlias, String sortString, boolean sortOrder) {
        return FlowDao.getQueryStringOrderJpqlStatic(typeAlias, sortString, sortOrder);
    }

    @Override
    protected void addWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params, List<String> whereClauses, Map<String, Object> whereParamValues) {
        FlowDao.addWhereClausesAndNamedParamesForQueryStringJpqlStatic(typeAlias, params, whereClauses, whereParamValues);
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
    public Flow getSupersedingDataSetVersion(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

}
