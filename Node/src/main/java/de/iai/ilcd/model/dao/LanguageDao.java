package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.common.Language;
import de.iai.ilcd.persistence.PersistenceUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * DAO for material property definition
 */
public class LanguageDao extends AbstractLongIdObjectDao<Language> {

    /**
     * Create the DAO
     */
    public LanguageDao() {
        super(Language.class.getSimpleName(), Language.class);
    }

    /**
     * Get language definition by name
     *
     * @param name name to get
     * @return language for name of <code>null</code> if none found
     */
    public Language getByName(String name) {
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            Query q = em.createQuery("select a from " + this.getJpaName() + " a WHERE a.name=:name");
            q.setParameter("name", name);

            return (Language) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Get material property definition by name
     *
     * @param languageCode languageCode to get
     * @return language for languageCode of <code>null</code> if none found
     */
    public Language getByLanguageCode(String languageCode) {
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            Query q = em.createQuery("select a from " + this.getJpaName() + " a WHERE a.languageCode=:languageCode");
            q.setParameter("languageCode", languageCode);

            return (Language) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
