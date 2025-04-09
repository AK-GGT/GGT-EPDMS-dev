package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.common.PushTarget;
import de.iai.ilcd.persistence.PersistenceUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

/**
 * The Data Access Object (DAO) for {@link PushTarget}.
 *
 * @author sarai
 */
public class PushTargetDao extends AbstractLongIdObjectDao<PushTarget> {

    /**
     * The dao to Push Target entry
     */
    public PushTargetDao() {
        super("PushTarget", PushTarget.class);
    }

    /**
     * Gets list of Push Target entries.
     *
     * @return List of push target entries
     */
    public List<PushTarget> getPushTargetList() {
        return this.getAll();
    }

    /**
     * Gets push target entry with given name.
     *
     * @param name The name of desired push target entry
     * @return a Push Target entry with given name
     */
    public PushTarget getPushTarget(String name) {
        EntityManager em = PersistenceUtil.getEntityManager();
        PushTarget pushTarget = null;
        try {
            pushTarget = (PushTarget) em.createQuery("select p from PushTarget p where p.name=:name")
                    .setParameter("name", name).getSingleResult();
        } catch (NoResultException nre) {
        }
        return pushTarget;
    }

    /**
     * Gets Push Target entry with given id.
     *
     * @param id The id of desired push target
     * @return A push target entry with given id
     */
    public PushTarget getPushTarget(Long id) {
        return this.getById(id);
    }

}
