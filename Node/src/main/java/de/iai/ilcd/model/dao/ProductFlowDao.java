package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.api.app.flow.FlowDataSet;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.flow.ComplianceType;
import de.fzk.iai.ilcd.api.binding.generated.flow.FlowPropertyType;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.flow.MaterialProperty;
import de.iai.ilcd.model.flow.MaterialPropertyDefinition;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.util.UnmarshalHelper;
import org.apache.commons.collections.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data access object for {@link ProductFlow}s. <br />
 * <b>Caution:</b> ProductFlow on model means {@link TypeOfFlowValue#PRODUCT_FLOW} or {@link TypeOfFlowValue#WASTE_FLOW}
 * or {@link TypeOfFlowValue#OTHER_FLOW}!
 */
public class ProductFlowDao extends FlowDao<ProductFlow> {

    /**
     * Create Dao
     */
    public ProductFlowDao() {
        super("ProductFlow", ProductFlow.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataStockField() {
        return "productFlows";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preCheckAndPersist(ProductFlow dataSet) {
        super.preCheckAndPersist(dataSet);

        MaterialPropertyDefinitionDao propDefDao = new MaterialPropertyDefinitionDao();
        Set<MaterialProperty> originalProps = dataSet.getMaterialProperties();
        for (MaterialProperty p : originalProps) {
            MaterialPropertyDefinition managedPropDef = propDefDao.getByName(p.getDefinition().getName());
            if (managedPropDef != null) {
                p.setDefinition(managedPropDef);
            }
        }
    }

    /**
     * Get all products ({@link ProductFlow#getType()} != {@link TypeOfFlowValue#OTHER_FLOW})
     *
     * @param specific   <code>true</code> for {@link ProductFlow#isSpecificProduct() specific} products, <code>false</code>
     *                   for generic. <code>null</code> will find both.
     * @param startIndex start index
     * @param pageSize   page size
     * @return all products
     */
    @SuppressWarnings("unchecked")
    public List<ProductFlow> getProducts(IDataStockMetaData[] stocks, Boolean specific, int startIndex, int pageSize) {
        Query q = buildProductQuery(stocks, specific, false);
        return q.setFirstResult(startIndex).setMaxResults(pageSize).getResultList();
    }

    /**
     * Get all products count ({@link ProductFlow#getType()} != {@link TypeOfFlowValue#OTHER_FLOW})
     *
     * @param specific <code>true</code> for {@link ProductFlow#isSpecificProduct() specific} products, <code>false</code>
     *                 for generic. <code>null</code> will find both.
     * @return all products count
     */
    public Long getProductsCount(IDataStockMetaData[] stocks, Boolean specific) {
        Query q = buildProductQuery(stocks, specific, true);
        return (Long) q.getSingleResult();
    }

    private Query buildProductQuery(IDataStockMetaData[] stocks, Boolean specific, boolean count) {
        List<String> wheres = new ArrayList<String>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        wheres.add("a.type!=:type");
        if (specific != null) {
            wheres.add("a.specificProduct=:specific");
            paramMap.put("specific", specific);
        }

        paramMap.put("type", TypeOfFlowValue.OTHER_FLOW);

        return getQueryForDataStocks(stocks, null, true, count, true, null, wheres, paramMap);
    }

    /**
     * Get all product flows that are flagged as vendor specific.
     *
     * @return the flows
     */
    public List<ProductFlow> getSpecificProducts(IDataStockMetaData[] stocks) {
        return this.getProducts(stocks, Boolean.TRUE);
    }

    /**
     * Get all flows with the type "Product flow" that are not flagged as vendor specific.
     *
     * @return the flows
     */
    public List<ProductFlow> getGenericProducts(IDataStockMetaData[] stocks) {
        return this.getProducts(stocks, Boolean.FALSE);
    }

    /**
     * Get all flows with the type &quot;Product flow&quot; that are flagged as vendor specific and have an
     * &quot;isA&quot;
     * relationship to
     * the flow with the specified id.
     *
     * @param uuid the UUID of the flow
     * @return the flows
     */
    public List<ProductFlow> getSpecificProducts(IDataStockMetaData[] stocks, String uuid) {
        return this.getProducts(stocks, uuid, Boolean.TRUE);
    }

    /**
     * Get all flows with the type "Product flow" that are flagged as not vendor specific and are linked to the
     * specified flow by an "isA" relationship.
     *
     * @param uuid the UUID of the flow
     * @return the flows
     */
    public List<ProductFlow> getGenericProducts(IDataStockMetaData[] stocks, String uuid) {
        return this.getProducts(stocks, uuid, Boolean.FALSE);
    }

    /**
     * Get all product flows filtered by specific/generic
     *
     * @param specific flag to indicate if specific or generic product flows are wanted
     * @return filtered list
     */
    @SuppressWarnings("unchecked")
    private List<ProductFlow> getProducts(IDataStockMetaData[] stocks, Boolean specific) {
        List<String> wheres = new ArrayList<String>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        wheres.add("a.type!=:type");
        wheres.add("a.specificProduct=:specific");

        paramMap.put("type", TypeOfFlowValue.OTHER_FLOW);
        paramMap.put("specific", specific);
        paramMap.put("mrv", Boolean.TRUE);

        Query q = getQueryForDataStocks(stocks, null, true, false, false, null, wheres, paramMap);
        return (List<ProductFlow>) q.getResultList();
    }

    /**
     * Get all product flows filtered by specific/generic
     *
     * @param specific flag to indicate if specific or generic product flows are wanted
     * @return filtered list
     */
    @SuppressWarnings("unchecked")
    private List<ProductFlow> getProducts(IDataStockMetaData[] stocks, String uuid, Boolean specific) {
        List<String> wheres = new ArrayList<String>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        wheres.add("a.type!=:type");
        wheres.add("a.specificProduct=:specific");
        wheres.add("a.uuid.uuid=:uuid");

        paramMap.put("type", TypeOfFlowValue.OTHER_FLOW);
        paramMap.put("specific", specific);
        paramMap.put("uuid", uuid);

        Query q = getQueryForDataStocks(stocks, null, true, false, false, null, wheres, paramMap);
        return (List<ProductFlow>) q.getResultList();
    }

    /**
     * Get the ancestor of provided flow
     *
     * @param uuid     UUID of flow in question
     * @param specific flag to indicate if next specific/generic ancestor (<code>null</code> = don't care, direct ancestor)
     * @return ancestor of provided flow (or <code>null</code> if none)
     */
    public ProductFlow getAncestor(String uuid, Boolean specific) {
        return this.handleAncestorChain(this.getDirectAncestor(uuid), specific);
    }

    /**
     * Get the ancestor of provided flow
     *
     * @param an       <b>!! direct ancestor !!</b> of product flow in question
     * @param specific flag to indicate if next specific/generic ancestor (<code>null</code> = don't care, direct ancestor)
     * @return ancestor of provided flow (or <code>null</code> if none)
     */
    private ProductFlow handleAncestorChain(ProductFlow an, Boolean specific) {
        if (an == null) {
            return null;
        } else {
            if (specific != null) {
                if (specific.booleanValue() == an.isSpecificProduct()) {
                    return an;
                } else {
                    return this.handleAncestorChain(this.getDirectAncestor(an), specific);
                }
            } else {
                return an;
            }
        }
    }

    /**
     * Get the ancestor of provided flow
     *
     * @param uuid UUID of flow in question
     * @return ancestor of provided flow (or <code>null</code> if none)
     */
    public ProductFlow getDirectAncestor(String uuid) {
        return this.getDirectAncestor(this.getByUuid(uuid));
    }

    /**
     * Get the ancestor of provided flow
     *
     * @param pf product flow in question
     * @return ancestor of provided flow (or <code>null</code> if none)
     */
    private ProductFlow getDirectAncestor(ProductFlow pf) {
        if (pf != null && pf.hasIsAReference()) {
            GlobalReference ancestorReference = pf.getIsAReference();
            if (ancestorReference.getVersion() != null && !ancestorReference.getVersion().isZero()) {
                return this.getByUuidAndVersion(ancestorReference.getRefObjectId(), ancestorReference.getVersion());
            } else {
                return this.getByUuid(ancestorReference.getRefObjectId());
            }
        }
        return null;
    }

    /**
     * Get list of ancestors
     *
     * @param uuid     UUID of product flow in question
     * @param maxDepth maximum depth to follow (value &lt;=0 will be ignored)
     * @return list of ancestors
     */
    public List<ProductFlow> getAncestors(String uuid, int maxDepth) {
        ArrayList<ProductFlow> tmp = new ArrayList<ProductFlow>();
        ProductFlow an = this.getDirectAncestor(uuid);
        maxDepth = maxDepth > 0 ? maxDepth : Integer.MAX_VALUE;
        int currentDepth = 0;
        while (an != null && currentDepth < maxDepth) {
            tmp.add(an);
            an = this.getDirectAncestor(an);
            currentDepth++;
        }

        return tmp;
    }


    /**
     * Load the descendants for specified product flows
     *
     * @param uuid     UUID of origin flow
     * @param maxDepth maximum depth to follow (value &lt;=0 will be ignored)
     * @return descendants for specified product flows
     */
    public List<ProductFlow> getDescendants(String uuid, int maxDepth) {
        maxDepth = maxDepth > 0 ? maxDepth : Integer.MAX_VALUE;
        List<ProductFlow> tmp = new ArrayList<ProductFlow>();

        List<ProductFlow> originFlow = new ArrayList<ProductFlow>();
        originFlow.add(this.getByUuid(uuid));

        if (CollectionUtils.isNotEmpty(originFlow)) {
            List<ProductFlow> currentDeptResult = originFlow;
            for (int depth = 0; depth < maxDepth; depth++) {
                currentDeptResult = this.loadDescendants(currentDeptResult);
                if (CollectionUtils.isNotEmpty(currentDeptResult)) {
                    tmp.addAll(currentDeptResult);
                } else {
                    break;
                }
            }
        }
        return tmp;
    }

    /**
     * Load the descendants for specified product flows
     *
     * @param loadDescendantsFor flows to load descendants for
     * @return descendants for specified product flows
     */
    @SuppressWarnings("unchecked")
    private List<ProductFlow> loadDescendants(List<ProductFlow> loadDescendantsFor) {

        EntityManager em = PersistenceUtil.getEntityManager();
        ArrayList<ProductFlow> loaded = new ArrayList<ProductFlow>();

        Query q = em.createQuery("SELECT a FROM " + this.getJpaName() + " a WHERE a.isAReference IS NOT NULL AND a.isAReference.uuid.uuid=:uuid");
        for (ProductFlow pf : loadDescendantsFor) {
            // TODO this is a workaround for issue #236
            if (pf == null) {
                break;
            }
            q.setParameter("uuid", pf.getUuidAsString());
            List<ProductFlow> tmp = q.getResultList();
            if (CollectionUtils.isNotEmpty(tmp)) {
                loaded.addAll(tmp);
            }
        }

        return loaded;
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
        Set<DataSet> dependencies = new HashSet<DataSet>();
        ProductFlow pf = (ProductFlow) dataSet;
        RootDataStock stock = pf.getRootDataStock();
        final List<String> ignoreListFinal = ignoreList != null ? new ArrayList<>(ignoreList) : new ArrayList<>();

        FlowDataSet xmlDataset = (FlowDataSet) new UnmarshalHelper().unmarshal(pf);

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
            xmlDataset.getModellingAndValidation().getComplianceDeclarations().getCompliance().stream()
                    .map(ComplianceType::getReferenceToComplianceSystem)
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .forEach(ref -> addDependency(ref, stock, dependencies));
        } catch (Exception e) {
            // None found, none added
        }

        try {
            tmp = xmlDataset.getAdministrativeInformation().getPublicationAndOwnership().getReferenceToOwnershipOfDataSet();
            if (tmp != null && !ignoreListFinal.contains(tmp.getRefObjectId()))
                addDependency(tmp, stock, dependencies);
        } catch (Exception e) {
            // None found, none added
        }

        try {
            xmlDataset.getFlowProperties().getFlowProperty().stream()
                    .map(FlowPropertyType::getReferenceToFlowPropertyDataSet)
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .forEach(ref -> addDependency(ref, stock, dependencies));

        } catch (Exception e) {
            // None found, none added
        }

        return dependencies;
    }

    @Override
    public ProductFlow getSupersedingDataSetVersion(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

}
