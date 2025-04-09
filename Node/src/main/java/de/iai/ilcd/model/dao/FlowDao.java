package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.service.model.IFlowListVO;
import de.fzk.iai.ilcd.service.model.IFlowVO;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.flow.FlowPropertyDescription;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.generic.ValueParser;

import java.util.List;
import java.util.Map;

/**
 * Data access object for {@link Flow flows}
 *
 * @param <FT> flow type
 */
public abstract class FlowDao<FT extends Flow> extends DataSetDao<FT, IFlowListVO, IFlowVO> {

    /**
     * Create the DAO for flows
     */
    protected FlowDao(String jpaName, Class<FT> clazz) {
        super(jpaName, clazz, IFlowListVO.class, IFlowVO.class, DataSetType.FLOW);
    }

    /**
     * Provided statically for wrapper class in resources
     *
     * @see #getQueryStringOrderJpql(String, String)
     */
    public static String getQueryStringOrderJpqlStatic(String typeAlias, String sortString, boolean sortOrder) {
        if ("type.value".equals(sortString)) {
            return typeAlias + ".type";
        } else if ("referenceFlowProperty.flowPropertyName.value".equals(sortString)) {
            return typeAlias + ".referencePropertyCache";
        } else if ("referenceFlowProperty.flowPropertyUnit".equals(sortString)) {
            return typeAlias + ".referencePropertyUnitCache";
        } else if (StringUtils.startsWith(sortString, "classification.classHierarchyAsString")) {
            return typeAlias + ".classificationCache";
        } else if ("rootDataStock.name".equals(sortString)) {
            return typeAlias + ".rootDataStock.name";
        } else if ("importDate".equals(sortString)) {
            return typeAlias + ".importDate";
        } else if ("version".equals(sortString)) {
            return typeAlias + ".version";
        } else if ("mostRecentVersion".equals(sortString)) {
            return typeAlias + ".mostRecentVersion";
        } else {
            return typeAlias + ".nameCache";
        }
    }

    /**
     * Provided statically for wrapper class in resources
     *
     * @see #addWhereClausesAndNamedParamesForQueryStringJpql(String, ValueParser, List, Map)
     */
    public static void addWhereClausesAndNamedParamesForQueryStringJpqlStatic(String typeAlias, ValueParser params, List<String> whereClauses, Map<String, Object> whereParamValues) {
        String type = params.getString("type");
        if (type != null && (type.length() > 3) && (!type.equals("select option"))) {
            TypeOfFlowValue typeValue = null;
            try {
                typeValue = TypeOfFlowValue.valueOf(type);
            } catch (Exception e) {
                // ignore it as we do not have a parsable value
            }
            if (typeValue != null) {
                whereClauses.add(typeAlias + ".type=:typeOfFlow");
                whereParamValues.put("typeOfFlow", typeValue);
            }
        }
    }

    public static DataSet lookup(GlobalReference ref) {
        if (ref.getType().equals(GlobalReferenceTypeValue.FLOW_DATA_SET))
            return lookupFlow(ref.getRefObjectId(), ref.getVersion());
        else
            return null;
    }

    public static Flow lookupFlow(GlobalReference ref) {
        return lookupFlow(ref.getRefObjectId(), ref.getVersion());
    }

    public static Flow lookupFlow(String uuid) {
        return lookupFlow(uuid, null);
    }

    public static Flow lookupFlow(String uuid, DataSetVersion version) {
        // check for product
        ProductFlowDao pfDao = new ProductFlowDao();
        ElementaryFlowDao efDao = new ElementaryFlowDao();
        return lookupFlow(uuid, version, pfDao, efDao);
    }

    public static Flow lookupFlow(String uuid, DataSetVersion version, ProductFlowDao pfDao, ElementaryFlowDao efDao) {
        Flow result = null;

        if (version != null && StringUtils.isNotBlank(version.getVersionString())) {
            result = pfDao.getByUuidAndVersion(uuid, version);
            if (result == null)
                result = efDao.getByUuidAndVersion(uuid, version);
        } else {
            result = pfDao.getByUuid(uuid);
            if (result == null)
                result = efDao.getByUuid(uuid);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preCheckAndPersist(FT dataSet) {
        this.findReferenceFlowProperty(dataSet);
    }

    /**
     * Get flows with maximum limit of result items
     *
     * @param maxFlows maximum amount of flows to get
     * @return flows with maximum limit of result items
     */
    public List<FT> getFlows(int maxFlows) {
        return super.get(0, maxFlows);
    }

    /**
     * Get a flow by UUID. Overrides {@link DataSetDao} implementation, because there is an additional step which tries
     * to associate referenced flow property, if available
     *
     * @return flow with provided UUID
     */
    @Override
    public FT getByUuid(String uuid) {
        FT f = super.getByUuid(uuid);
        if (f != null) {
            this.findReferenceFlowProperty(f); // try to associate reference flow property if available
        }
        return f;
    }

    /**
     * Find the reference flow property description and link to provided flow
     *
     * @param flow flow to get reference property for
     */
    private void findReferenceFlowProperty(FT flow) {
        FlowPropertyDao flowpropDao = new FlowPropertyDao();

        FlowPropertyDescription referencePropertyDesc = flow.getReferencePropertyDescription();
        if (referencePropertyDesc != null && referencePropertyDesc.getFlowProperty() == null) {
            // OK, let't try to find the property in the database
            // String permanentUri=referenceProperty.getFlowPropertyRef().getUri();
            String uuid = referencePropertyDesc.getFlowPropertyRef().getUuid().getUuid();
            FlowProperty refprop = flowpropDao.getByUuid(uuid);
            referencePropertyDesc.setFlowProperty(refprop);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getQueryStringOrderJpql(String typeAlias, String sortString, boolean sortOrder) {
        if ("type.value".equals(sortString)) {
            return typeAlias + ".type";
        } else if ("referenceFlowProperty.flowPropertyName.value".equals(sortString)) {
            return typeAlias + ".referencePropertyCache";
        } else if ("referenceFlowProperty.flowPropertyUnit".equals(sortString)) {
            return typeAlias + ".referencePropertyUnitCache";
        } else {
            return super.getQueryStringOrderJpql(typeAlias, sortString, sortOrder);
        }
        // return FlowDao.getQueryStringOrderJpqlStatic( typeAlias, sortString );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params, List<String> whereClauses, Map<String, Object> whereParamValues) {
        FlowDao.addWhereClausesAndNamedParamesForQueryStringJpqlStatic(typeAlias, params, whereClauses, whereParamValues);
    }

}
