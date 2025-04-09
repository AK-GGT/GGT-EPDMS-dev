package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.api.app.unitgroup.UnitGroupDataSet;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.service.model.IUnitGroupListVO;
import de.fzk.iai.ilcd.service.model.IUnitGroupVO;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.util.UnmarshalHelper;
import org.apache.velocity.tools.generic.ValueParser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Data access object for {@link UnitGroup unit groups}
 */

/**
 * @author oli
 */
public class UnitGroupDao extends DataSetDao<UnitGroup, IUnitGroupListVO, IUnitGroupVO> {

    /**
     * Create the data access object for {@link UnitGroup unit groups}
     */
    public UnitGroupDao() {
        super("UnitGroup", UnitGroup.class, IUnitGroupListVO.class, IUnitGroupVO.class, DataSetType.UNITGROUP);
    }

    @Override
    protected String getDataStockField() {
        return "unitGroups";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preCheckAndPersist(UnitGroup dataSet) {
        // Nothing to to :)
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
        if ("referenceUnit.name".equals(sortString)) {
            return typeAlias + ".referenceUnit.name";
        } else {
            return super.getQueryStringOrderJpql(typeAlias, sortString, sortOrder);
        }
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
        UnitGroup ug = (UnitGroup) dataSet;
        RootDataStock stock = ug.getRootDataStock();
        final List<String> ignoreListFinal = ignoreList != null ? new ArrayList<>(ignoreList) : new ArrayList<>();

        UnitGroupDataSet xmlDataset = (UnitGroupDataSet) new UnmarshalHelper().unmarshal(ug);

        try {
            List<GlobalReferenceType> refs = xmlDataset.getAdministrativeInformation().getDataEntryBy()
                    .getReferenceToDataSetFormat()
                    .stream()
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .collect(Collectors.toList());
            addDependencies(refs, stock, dependencies);
        } catch (Exception e) {
            // None found, none added
        }

        try {
            GlobalReferenceType ref = xmlDataset.getAdministrativeInformation().getPublicationAndOwnership().
                    getReferenceToOwnershipOfDataSet();
            if (ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                addDependency(ref, stock, dependencies);
        } catch (Exception e) {
            // None found, none added
        }

        return dependencies;
    }

    @Override
    public UnitGroup getSupersedingDataSetVersion(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

}
