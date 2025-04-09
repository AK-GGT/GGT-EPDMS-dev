package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.api.app.flow.FlowDataSet;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.flow.ComplianceType;
import de.fzk.iai.ilcd.api.binding.generated.flow.FlowPropertyType;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.util.UnmarshalHelper;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Data access object for {@link ElementaryFlow}s
 */
public class ElementaryFlowDao extends FlowDao<ElementaryFlow> {

    /**
     * Create the Dao
     */
    public ElementaryFlowDao() {
        super("ElementaryFlow", ElementaryFlow.class);
    }

    /**
     * Get the flows by category
     *
     * @param mainClass main class name
     * @return flows by category
     */
    @SuppressWarnings("unchecked")
    public List<ElementaryFlow> getFlowsByCategory(String mainClass) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return (List<ElementaryFlow>) em.createQuery("select f from " + this.getJpaName() + " f join f.categorization.classes cl where cl.level=0 and cl.name=:className").setParameter("className", mainClass).getResultList();
    }

    /**
     * Get the number of flows in category
     *
     * @param mainClass main class name
     * @return number of flows in category
     * @see #getFlowsByCategory(String)
     */
    public long getNumberOfFlowsInCategory(String mainClass) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return (Long) em.createQuery("select count(f) from " + this.getJpaName() + " f join f.categorization.classes cl where cl.level=0 and cl.name=:className").setParameter("className", mainClass).getSingleResult();
    }

    /**
     * Get flows by category
     *
     * @param mainClass main class name
     * @param subClass  sub class name
     * @return flows by category
     */
    @SuppressWarnings("unchecked")
    public List<ElementaryFlow> getFlowsByCategory(String mainClass, String subClass) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return (List<ElementaryFlow>) em.createQuery("select f from " + this.getJpaName() + " f join f.categorization.classes cl join f.categorization.classes cl2 where cl.level=0 and cl.name=:mainClass and cl2.level=1 and cl2.name=:subClass").setParameter("mainClass", mainClass).setParameter("subClass", subClass).getResultList();
    }

    /**
     * Get flows by sub categories
     *
     * @param subClass  sub class 1 name
     * @param subClass2 sub class 2 name
     * @return flows by sub categories
     */
    @SuppressWarnings("unchecked")
    public List<ElementaryFlow> getFlowsBySubCategories(String subClass, String subClass2) {
        EntityManager em = PersistenceUtil.getEntityManager();

        Query q = em.createQuery("select f from " + this.getJpaName() + " f join f.categorization.classes cl1 join f.categorization.classes cl2 where cl1.level=1 and cl1.name=:subClass and cl2.level=2 and cl2.name=:subClass2");
        q.setParameter("subClass", subClass);
        q.setParameter("subClass2", subClass2);

        return (List<ElementaryFlow>) q.getResultList();
    }

    /**
     * Get the number of flows in category
     *
     * @param mainClass main class
     * @param subClass  sub class
     * @return number of flows in category
     */
    public long getNumberOfFlowsInCategory(String mainClass, String subClass) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return (Long) em.createQuery("select count(f) from " + this.getJpaName() + " f join f.categorization.classes cl join f.categorization.classes cl2 where cl.level=0 and cl.name=:mainClass and cl2.level=1 and cl2.name=:subClass").setParameter("mainClass", mainClass).setParameter("subClass", subClass).getSingleResult();
    }

    /**
     * Get the top categories
     *
     * @return top categories
     */
    @SuppressWarnings("unchecked")
    public List<String> getTopCategories() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return (List<String>) em.createQuery("select distinct cl.name from " + this.getJpaName() + " f join f.categorization.classes cl where cl.level=:level order by cl.name").setParameter("level", 0).getResultList();
    }

    /**
     * Get the sub categories
     *
     * @param className parent class name
     * @param level     parent level
     * @return sub categories
     */
    @SuppressWarnings("unchecked")
    public List<String> getSubCategories(String className, String level) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return (List<String>) em.createQuery("select distinct cl.name from " + this.getJpaName() + " f join f.categorization.classes cl join f.categorization.classes cl2 where cl.level=:level and cl2.name=:className order by cl.name").setParameter("level", Integer.parseInt(level)).setParameter("className", className).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataStockField() {
        return "elementaryFlows";
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
        ElementaryFlow ef = (ElementaryFlow) dataSet;
        RootDataStock stock = ef.getRootDataStock();
        final List<String> ignoreListFinal = ignoreList != null ? new ArrayList<>(ignoreList) : new ArrayList<>();

        FlowDataSet xmlDataset = (FlowDataSet) new UnmarshalHelper().unmarshal(ef);

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
            tmp = xmlDataset.getAdministrativeInformation().getPublicationAndOwnership()
                    .getReferenceToOwnershipOfDataSet();
            if (!ignoreListFinal.contains(tmp.getRefObjectId()))
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
    public ElementaryFlow getSupersedingDataSetVersion(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

}
