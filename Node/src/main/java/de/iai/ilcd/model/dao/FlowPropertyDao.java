package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.api.app.flowproperty.FlowPropertyDataSet;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.service.model.IFlowPropertyListVO;
import de.fzk.iai.ilcd.service.model.IFlowPropertyVO;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.util.UnmarshalHelper;
import org.apache.velocity.tools.generic.ValueParser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Data access object for {@link FlowProperty flow properties}
 */
public class FlowPropertyDao extends DataSetDao<FlowProperty, IFlowPropertyListVO, IFlowPropertyVO> {

    /**
     * Create the data access object for {@link FlowProperty flow properties}
     */
    public FlowPropertyDao() {
        super("FlowProperty", FlowProperty.class, IFlowPropertyListVO.class, IFlowPropertyVO.class, DataSetType.FLOWPROPERTY);
    }

    @Override
    protected String getDataStockField() {
        return "flowProperties";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preCheckAndPersist(FlowProperty dataSet) {
        this.attachUnitGroup(dataSet);
    }

    /**
     * Get flow property by UUID. Overrides {@link DataSetDao} implementation because
     * {@link #attachUnitGroup(FlowProperty)} is called after loading.
     */
    @Override
    public FlowProperty getByUuid(String uuid) {
        FlowProperty fp = super.getByUuid(uuid);
        if (fp != null) {
            this.attachUnitGroup(fp);
        }
        return fp;
    }

    private void attachUnitGroup(FlowProperty flowprop) {
        UnitGroupDao unitGroupDao = new UnitGroupDao();
        if (flowprop != null && flowprop.getReferenceToUnitGroup() != null) {
            String uuid = flowprop.getReferenceToUnitGroup().getUuid().getUuid();
            UnitGroup unitGroup = unitGroupDao.getByUuid(uuid);
            flowprop.setUnitGroup(unitGroup);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getQueryStringOrderJpql(String typeAlias, String sortString, boolean sortOrder) {
        if ("unitGroupName.value".equals(sortString)) {
            return typeAlias + ".unitGroup.name.value";
        } else if ("defaultUnit".equals(sortString)) {
            return typeAlias + ".unitGroup.referenceUnit.name";
        } else {
            return super.getQueryStringOrderJpql(typeAlias, sortString, sortOrder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params, List<String> whereClauses,
                                                                    Map<String, Object> whereParamValues) {
        // nothing to do beside the defaults
    }

    /* (non-Javadoc)
     * @see de.iai.ilcd.model.dao.DataSetDao#getDependencies(de.iai.ilcd.model.common.DataSet, de.iai.ilcd.model.dao.DependenciesMode)
     */
    @Override
    public Set<DataSet> getDependencies(DataSet dataset, DependenciesMode mode) {
        return getDependencies(dataset, mode, null);
    }

    @Override
    public Set<DataSet> getDependencies(DataSet dataSet, DependenciesMode depMode, Collection<String> ignoreList) {
        Set<DataSet> dependencies = new HashSet<>();
        FlowProperty fp = (FlowProperty) dataSet;
        RootDataStock stock = fp.getRootDataStock();
        final List<String> ignoreListFinal = ignoreList != null ? new ArrayList<>(ignoreList) : new ArrayList<>();

        // unit group
        UnitGroup ug = fp.getUnitGroup();
        GlobalReference ugRef = fp.getReferenceToUnitGroup();
        boolean ignore = false;
        if (ug != null)
            ignore = ignoreListFinal.contains(ug.getUuidAsString());
        else if (ugRef != null)
            ignore = ignoreListFinal.contains(ugRef.getRefObjectId());
        else
            ignore = true;

        if (!ignore)
            addDependency(ug, ugRef, stock, dependencies);

        FlowPropertyDataSet xmlDataset = (FlowPropertyDataSet) new UnmarshalHelper().unmarshal(fp);


        GlobalReferenceType tmp;
        List<GlobalReferenceType> tmpList;
        try {
            tmpList = xmlDataset.getAdministrativeInformation().getDataEntryBy().getReferenceToDataSetFormat().stream()
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .collect(Collectors.toList());

            if (!tmpList.isEmpty())
                addDependencies(tmpList, stock, dependencies);

        } catch (Exception e) {
            // None found, none added
        }

        try {
            tmp = xmlDataset.getAdministrativeInformation().getPublicationAndOwnership()
                    .getReferenceToOwnershipOfDataSet();
            if (!ignoreListFinal.contains(tmp.getRefObjectId()))
                addDependency(tmp, stock, dependencies);
        } catch (Exception e) {
            // None found, none added
        }

        try {
            tmp = xmlDataset.getFlowPropertiesInformation().getQuantitativeReference()
                    .getReferenceToReferenceUnitGroup();
            if (!ignoreListFinal.contains(tmp.getRefObjectId()))
                addDependency(tmp, stock, dependencies);

        } catch (Exception e) {
            // None found, none added
        }

        return dependencies;
    }

    @Override
    public FlowProperty getSupersedingDataSetVersion(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

}
