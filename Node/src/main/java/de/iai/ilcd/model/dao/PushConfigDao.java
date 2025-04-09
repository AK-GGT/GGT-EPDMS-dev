package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.common.PushConfig;
import de.iai.ilcd.persistence.PersistenceUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

/**
 * The Data Access Object (DAO) for {@link PushConfig}.
 *
 * @author sarai
 */
public class PushConfigDao extends AbstractLongIdObjectDao<PushConfig> {

    /**
     * Initializes a new PshConfiDao.
     */
    public PushConfigDao() {
        super("PushConfig", PushConfig.class);
    }

    /**
     * Gets all PushConfig entries.
     *
     * @return A list containing all PushConfig entries
     */
    public List<PushConfig> getPushConfigList() {
        return this.getAll();
    }

    /**
     * Gets PushConfig entry with given name.
     *
     * @param name The unique name of PushConfig entry
     * @return A PushConfig entry with given name
     */
    public PushConfig getPushConfig(String name) {
        EntityManager em = PersistenceUtil.getEntityManager();
        PushConfig pushConfig = null;
        try {
            pushConfig = (PushConfig) em.createQuery("select p from PushConfig p where p.name=:name")
                    .setParameter("name", name).getSingleResult();
        } catch (NoResultException nre) {

        }
        return pushConfig;
    }

    /**
     * Gets PushConfig entry with given name.
     *
     * @param name The unique name of PushConfig entry
     * @return A PushConfig entry with given name
     */
    @SuppressWarnings("unchecked")
    public List<PushConfig> getFavouritePushConfigs() {
        EntityManager em = PersistenceUtil.getEntityManager();
        List<PushConfig> pushConfigList = null;
        try {
            pushConfigList = (List<PushConfig>) em.createQuery("select p from PushConfig p where p.favourite=true").getResultList();
        } catch (NoResultException nre) {

        }
        return pushConfigList;
    }

}
