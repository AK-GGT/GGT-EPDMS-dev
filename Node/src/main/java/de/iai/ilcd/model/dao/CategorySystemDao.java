package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.common.CategorySystem;
import de.iai.ilcd.persistence.PersistenceUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;

public class CategorySystemDao extends AbstractLongIdObjectDao<CategorySystem> {

    /**
     * Default constructor
     */
    public CategorySystemDao() {
        super("CategorySystem", CategorySystem.class);
    }

    public CategorySystem getByName(String name) {
        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery("SELECT c FROM CategorySystem c WHERE c.name=:name");
        q.setParameter("name", name);
        q.setMaxResults(1);
        CategorySystem result = (CategorySystem) q.getSingleResult();
        return result;
    }

    public Date getLastModified(String name) {
        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery("SELECT cd.importDate FROM CategorySystem c LEFT JOIN c.categoryDefinitions cd WHERE c.name=:name ORDER BY cd.importDate DESC");
        q.setParameter("name", name);
        q.setMaxResults(1);
        return (Date) q.getSingleResult();
    }

}
