package de.iai.ilcd.service.glad;

import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.GenericDAOImpl;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.persistence.PersistenceUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * The implementation of the GLAD registration Data Access Object (DAO).
 *
 * @author sarai
 */
@Repository(value = "GLADRegistrationDataDao")
public class GLADRegistrationDataDaoImpl extends GenericDAOImpl<GLADRegistrationData, Long>
        implements GLADRegistrationDataDao {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GLADRegistrationData> getRegistered() {
        EntityManager em = PersistenceUtil.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<GLADRegistrationData> c = cb.createQuery(GLADRegistrationData.class);
        TypedQuery<GLADRegistrationData> q = em.createQuery(c);
        return q.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GLADRegistrationData> getListOfRegistrations(Process process) {
        EntityManager em = PersistenceUtil.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<GLADRegistrationData> c = cb.createQuery(GLADRegistrationData.class);
        Root<GLADRegistrationData> dsrdR = c.from(GLADRegistrationData.class);
        Path<String> uuidP = dsrdR.get(GLADRegistrationData_.uuid);
        Path<DataSetVersion> versionP = dsrdR.get(GLADRegistrationData_.version);
        c.where(cb.and(cb.equal(uuidP, process.getUuidAsString()), cb.equal(versionP, process.getVersion())));
        TypedQuery<GLADRegistrationData> q = em.createQuery(c);
        return q.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GLADRegistrationData findByUUIDAndVersion(String UUID, String version) {
        EntityManager em = PersistenceUtil.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<GLADRegistrationData> c = cb.createQuery(GLADRegistrationData.class);
        Root<GLADRegistrationData> dsrdR = c.from(GLADRegistrationData.class);
        Path<String> uuidP = dsrdR.get(GLADRegistrationData_.uuid);
        Path<DataSetVersion> versionP = dsrdR.get(GLADRegistrationData_.version);
        try {
            c.where(cb.and(cb.equal(uuidP, UUID), cb.equal(versionP, DataSetVersion.parse(version))));
        } catch (FormatException e) {
            throw new NoResultException();
        }
        TypedQuery<GLADRegistrationData> q = em.createQuery(c);
        return q.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GLADRegistrationData findByUUID(String UUID) {
        EntityManager em = PersistenceUtil.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<GLADRegistrationData> c = cb.createQuery(GLADRegistrationData.class);
        Root<GLADRegistrationData> dsrdR = c.from(GLADRegistrationData.class);
        Path<String> uuidP = dsrdR.get(GLADRegistrationData_.uuid);
        c.where(cb.and(cb.equal(uuidP, UUID)));

        TypedQuery<GLADRegistrationData> q = em.createQuery(c);
        return q.getSingleResult();

    }

}
