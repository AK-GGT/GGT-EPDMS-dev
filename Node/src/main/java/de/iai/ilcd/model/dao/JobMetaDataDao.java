package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.common.JobMetaData;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.service.util.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * The Data Access Object (DAO) for {@link JobMetaData}
 *
 * @author sarai
 */
public class JobMetaDataDao extends AbstractLongIdObjectDao<JobMetaData> {

    public static final Logger LOGGER = LoggerFactory.getLogger(JobMetaDataDao.class);

    /**
     * The dao to Push Target entry
     */
    public JobMetaDataDao() {
        super("JobMetaData", JobMetaData.class);
    }

    /**
     * Gets list of Push Target entries.
     *
     * @return List of push target entries
     */
    public List<JobMetaData> getJobMetaDataList() {
        return this.getAll();
    }

    /**
     * Gets push target entry with given name.
     *
     * @param jobId uuid of the job
     *              The name of desired push target entry
     * @return a Push Target entry with given name
     */
    public JobMetaData getJobMetaData(String jobId) {
        EntityManager em = PersistenceUtil.getEntityManager();
        JobMetaData jobMetaData = null;
        try {
            jobMetaData = (JobMetaData) em.createQuery("select j from JobMetaData j where j.jobId=:jobId")
                    .setParameter("jobId", jobId).getSingleResult();
        } catch (NoResultException nre) {
            nre.printStackTrace();
        }
        return jobMetaData;
    }

    /**
     * Gets Push Target entry with given id.
     *
     * @param id The id of desired push target
     * @return A push target entry with given id
     */
    public JobMetaData getJobMetaData(Long id) {
        return this.getById(id);
    }

    public JobMetaData mergeJobState(String uuid, JobState jobState) throws MergeException {
        EntityManager em = PersistenceUtil.getEntityManager();
        JobMetaData jobMetaData;
        try {
            jobMetaData = (JobMetaData) em.createQuery("select j from JobMetaData j where j.jobId=:uuid")
                    .setParameter("uuid", uuid).getSingleResult();

            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                jobMetaData.setJobState(jobState);
                tx.commit();

            } catch (Exception e) {
                throw new MergeException("Error while updating job state for job with jobId='" + uuid + "'", e);
            }

        } catch (NoResultException nre) {
            throw new MergeException("No job meta data found with jobId='" + uuid + "'", nre);
        }

        return jobMetaData;
    }

    public JobMetaData mergeSuccessInfoErrorLogRunTime(String uuid, Long errorsCount, Long infosCount,
                                                       Long successesCount, String log) throws MergeException {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        JobMetaData jobMetaData;
        try {
            Instant startInstant = Instant.now();
            boolean timeSpentWaiting = false;
            while (tx.isActive() && Duration.between(startInstant, Instant.now()).getSeconds() < 5) {
                // wait
                timeSpentWaiting = true;
            }
            if (timeSpentWaiting)
                LOGGER.warn("Spent " + Duration.between(startInstant, Instant.now()).getSeconds() +
                        "s waiting on transaction to be deactivated");

            tx.begin();
            jobMetaData = (JobMetaData) em.createQuery("select j from JobMetaData j where j.jobId=:uuid")
                    .setParameter("uuid", uuid).getSingleResult();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Updating jmd with name: " + jobMetaData.getJobName());
                LOGGER.trace("log.toString().length() " +
                        (jobMetaData.getLog() != null ? jobMetaData.getLog().length() : 0) + "->" + log.length());
                LOGGER.trace("errorsCount " +
                        jobMetaData.getErrorsCount() + "->" + errorsCount);
                LOGGER.trace("infosCount " +
                        jobMetaData.getInfosCount() + "->" + infosCount);
                LOGGER.trace("successesCount " +
                        jobMetaData.getSuccessesCount() + "->" + successesCount);
            }
            jobMetaData.setLog(log != null ? log : "Unavailable... may be published later in the process...");
            jobMetaData.setErrorsCount(errorsCount);
            jobMetaData.setInfosCount(infosCount);
            jobMetaData.setSuccessesCount(successesCount);
            if (jobMetaData.getJobCompletionTime() == null) {
                Instant startingInstant = jobMetaData.getJobFireTime().toInstant();
                Long runTime = Duration.between(startingInstant, Instant.now()).toMillis();
                jobMetaData.setJobRunTime(runTime);
            }
            em.merge(jobMetaData);
            tx.commit();

            LOGGER.trace("JobMetaData merge successful");

        } catch (NoResultException nre) {
            throw new MergeException("No job meta data found with jobId='" + uuid + "'", nre);

        } catch (Exception e) {
            throw new MergeException("Error while updating job state for job with jobId='" + uuid + "'", e);

        }

        return jobMetaData;
    }

}
