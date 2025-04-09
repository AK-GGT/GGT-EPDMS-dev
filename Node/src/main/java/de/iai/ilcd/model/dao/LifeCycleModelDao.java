package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.service.model.ILifeCycleModelListVO;
import de.fzk.iai.ilcd.service.model.ILifeCycleModelVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.fzk.iai.ilcd.service.model.process.IReview;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.lifecyclemodel.*;
import de.iai.ilcd.persistence.PersistenceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

/**
 * @author MK
 * @since soda4LCA 5.7.0
 */

public class LifeCycleModelDao extends DataSetDao<LifeCycleModel, ILifeCycleModelListVO, ILifeCycleModelVO> {

    private static final Logger LOGGER = LogManager.getLogger(LifeCycleModelDao.class);

    public LifeCycleModelDao() {
        super("LifeCycleModel", LifeCycleModel.class, ILifeCycleModelListVO.class, ILifeCycleModelVO.class,
                DataSetType.LIFECYCLEMODEL);
    }


    public LifeCycleModel getLifeCycleModel(String UUID) {
        return super.getByUuid(UUID);
    }


    @Override
    public Set<DataSet> getDependencies(DataSet lcm, DependenciesMode mode) {
        return getDependencies(lcm, mode, null);
    }

    @Override
    public Set<DataSet> getDependencies(DataSet dataSet, DependenciesMode depMode, Collection<String> ignoreList) {
        Set<DataSet> dependencies = new HashSet<>();
        final List<String> ignoreListFinal = ignoreList != null ? new ArrayList<>(ignoreList) : new ArrayList<>();
        GlobalReferenceDao dao = new GlobalReferenceDao();

        for (IGlobalReference ref : extractReferences((LifeCycleModel) dataSet))
            if (ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                dependencies.add(dao.getByReference(ref));

        if (dependencies.contains(null)) {
            LOGGER.info("Some references couldn't be resolved while gathering dependencies...");
            dependencies.remove(null);
        }

        return dependencies;
    }

    private Set<IGlobalReference> extractReferences(LifeCycleModel lcm) {
        Set<IGlobalReference> erg = new HashSet<IGlobalReference>();

        /** === Key data set information === **/

        // ReferenceToResultingProcess
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getReferenceToResultingProcess)
                .map(ls -> erg.addAll(ls))
                .orElse(null);

        // ReferenceToExternalDocumentation
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getReferenceToExternalDocumentation)
                .map(ls -> erg.addAll(ls))
                .orElse(null);


        /** === Technological representativeness === **/

        // ReferenceToProcess
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getProcesses)
                .orElse(new ArrayList<ProcessInstance>())
                .forEach(p -> erg.add(p.getReferenceToProcess()));

        // ReferenceToDiagram
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getReferenceToDiagram)
                .map(ls -> erg.addAll(ls))
                .orElse(null);


        /** === Modelling and validation === **/

        // ReferencesToReviewers
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getReviews)
                .orElse(new ArrayList<IReview>())
                .forEach(r -> erg.addAll(r.getReferencesToReviewers()));

        // ComplianceSystems
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getComplianceSystems)
                .orElse(new HashSet<IComplianceSystem>())
                .forEach(c -> erg.add(c.getReference()));


        /** === Commissioner and goal === **/

        // ReferenceToCommissioner
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getAdministrativeInformation)
                .map(AdministrativeInformation::getCommissionerAndGoal)
                .map(CommissionerAndGoal::getReferenceToCommissioner)
                .map(ls -> erg.addAll(ls))
                .orElse(null);


        /** === Data set generator / modeller === **/

        // ReferenceToPersonOrEntityGeneratingTheDataSet
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getAdministrativeInformation)
                .map(AdministrativeInformation::getReferenceToPersonOrEntityGeneratingTheDataSet)
                .map(ls -> erg.addAll(ls))
                .orElse(null);

        /** === Data entry by === **/

        // ReferenceToDataSetFormat
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getAdministrativeInformation)
                .map(AdministrativeInformation::getDataEntryBy)
                .map(DataEntryBy::getReferenceToDataSetFormat)
                .map(ls -> erg.addAll(ls))
                .orElse(null);

        // ReferenceToPersonOrEntityEnteringTheData
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getAdministrativeInformation)
                .map(AdministrativeInformation::getDataEntryBy)
                .map(DataEntryBy::getReferenceToPersonOrEntityEnteringTheData)
                .map(ls -> erg.addAll(ls))
                .orElse(null);

        /** === Publication and ownership === **/

        // ReferenceToPrecedingDataSetVersion
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getAdministrativeInformation)
                .map(AdministrativeInformation::getPublicationAndOwnership)
                .map(PublicationAndOwnership::getReferenceToPrecedingDataSetVersion)
                .map(ls -> erg.addAll(ls))
                .orElse(null);

        // ReferenceToOwnershipOfDataSet
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getAdministrativeInformation)
                .map(AdministrativeInformation::getPublicationAndOwnership)
                .map(PublicationAndOwnership::getReferenceToOwnershipOfDataSet)
                .map(l -> erg.add(l))
                .orElse(null);

        // ReferenceToEntitiesWithExclusiveAccess
        Optional.ofNullable(lcm)
                .map(LifeCycleModel::getAdministrativeInformation)
                .map(AdministrativeInformation::getPublicationAndOwnership)
                .map(PublicationAndOwnership::getReferenceToEntitiesWithExclusiveAccess)
                .map(ls -> erg.addAll(ls))
                .orElse(null);

        erg.remove(null);
        return erg;
    }

    @Override
    protected String getDataStockField() {
        return "lifeCycleModels";
    }

    @Override
    protected void preCheckAndPersist(LifeCycleModel dataSet) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params,
                                                                    List<String> whereClauses, Map<String, Object> whereParamValues) {
        // TODO Auto-generated method stub

    }


    public LifeCycleModel getSupersedingDataSetVersion(String uuid) {
        try {
            EntityManager em = PersistenceUtil.getEntityManager();
            Query q = em.createQuery("SELECT lcm FROM LifeCycleModel lcm JOIN lcm.administrativeInformation.publicationAndOwnership.referenceToPrecedingDataSetVersion ref WHERE ref.uuid.uuid = :uuid ORDER BY lcm.version DESC");
            q.setParameter("uuid", uuid);
            q.setMaxResults(1);
            return (LifeCycleModel) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    public List<LifeCycleModel> getSupersedingDataSetVersions(String uuid) {
        try {
            EntityManager em = PersistenceUtil.getEntityManager();
            Query q = em.createQuery("SELECT lcm FROM LifeCycleModel lcm JOIN lcm.administrativeInformation.publicationAndOwnership.referenceToPrecedingDataSetVersion ref WHERE ref.uuid.uuid = :uuid ORDER BY lcm.version DESC");
            q.setParameter("uuid", uuid);
            return q.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }
}
