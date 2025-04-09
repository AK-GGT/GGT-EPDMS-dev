package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.flow.MaterialPropertyDefinition;
import de.iai.ilcd.persistence.PersistenceUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * DAO for material property definition
 */
public class MaterialPropertyDefinitionDao extends AbstractLongIdObjectDao<MaterialPropertyDefinition> {

    /**
     * Create the DAO
     */
    public MaterialPropertyDefinitionDao() {
        super(MaterialPropertyDefinition.class.getSimpleName(), MaterialPropertyDefinition.class);
    }

    /**
     * Get material property definition by name
     *
     * @param name name to get
     * @return material property for name of <code>null</code> if none found
     */
    public MaterialPropertyDefinition getByName(String name) {
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            Query q = em.createQuery("select a from " + this.getJpaName() + " a WHERE a.name=:name");
            q.setParameter("name", name);

            return (MaterialPropertyDefinition) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
