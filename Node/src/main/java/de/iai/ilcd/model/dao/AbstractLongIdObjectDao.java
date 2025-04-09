package de.iai.ilcd.model.dao;

import de.iai.ilcd.persistence.PersistenceUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Id;

/**
 * Common implementation for DAO objects that load
 * object that have a {@link Long} type {@link Id} annotated field.
 *
 * @param <T> Class that is being accessed
 */
public abstract class AbstractLongIdObjectDao<T> extends AbstractDao<T> {

    /**
     * Create a DAO
     *
     * @param jpaName       the name in JPA of class
     * @param accessedClass the class accessed by this DAO
     */
    public AbstractLongIdObjectDao(String jpaName, Class<T> accessedClass) {
        super(jpaName, accessedClass);
    }

    /**
     * Get the entity of type T by numerical JPA id
     *
     * @param id id of the entity
     * @return entity of type T by JPA id
     */
    public T getById(long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.find(this.getAccessedClass(), new Long(id));
    }

    /**
     * Get the entity of type T by JPA id
     *
     * @param id id of the entity as string
     * @return entity of type T by JPA id
     */
    public T getById(String id) {
        try {
            return this.getById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public T removeById(long id) throws Exception {
        EntityManager em = PersistenceUtil.getEntityManager();
        em.clear();

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T managedEntity = em.getReference(getAccessedClass(), id);
            em.remove(managedEntity);
            tx.commit();

            return managedEntity;

        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();

            throw e;

        }

    }

}
