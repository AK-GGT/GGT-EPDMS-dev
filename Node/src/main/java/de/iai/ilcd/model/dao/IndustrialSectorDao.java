package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.security.IndustrialSector;
import de.iai.ilcd.persistence.PersistenceUtil;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * DAO for industrial sectors
 */
public class IndustrialSectorDao extends AbstractLongIdObjectDao<IndustrialSector> {

    /**
     * Create DAO
     */
    public IndustrialSectorDao() {
        super("IndustrialSector", IndustrialSector.class);
    }

    /**
     * Get the industrial sectors
     *
     * @return industrial sectors
     */
    @SuppressWarnings("unchecked")
    public List<IndustrialSector> getAllOrdered() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery("select s from IndustrialSector s order by s.sector").getResultList();
    }
}
