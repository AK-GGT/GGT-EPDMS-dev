package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.StockAccessRightDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class UserDao extends AbstractLongIdObjectDao<User> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(UserDao.class);

    public UserDao() {
        super("User", User.class);
    }

    /**
     * {@inheritDoc} <br />
     * Deletes the relevant {@link StockAccessRight stock access rights} as well
     */
    @Override
    public User remove(User obj) throws Exception {
        if (obj == null) {
            return null;
        }

        StockAccessRightDao sarDao = new StockAccessRightDao();

        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        try {
            t.begin();
            List<StockAccessRight> sarList = sarDao.getOwnedByUser(obj);
            User tmp = em.contains(obj) ? obj : em.merge(obj);
            em.remove(tmp);
            if (CollectionUtils.isNotEmpty(sarList)) {
                for (StockAccessRight sar : sarList) {
                    em.remove(sar);
                }
            }
            t.commit();
            return tmp;
        } catch (Exception e) {
            t.rollback();
            throw e;
        }
    }

    /**
     * {@inheritDoc} <br />
     * Deletes the relevant {@link StockAccessRight stock access rights} as well
     */
    @Override
    public Collection<User> remove(Collection<User> objs) throws Exception {
        if (CollectionUtils.isEmpty(objs)) {
            return null;
        }

        StockAccessRightDao sarDao = new StockAccessRightDao();
        Collection<User> res = new ArrayList<User>();
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        try {
            t.begin();
            for (User obj : objs) {
                User tmp = em.contains(obj) ? obj : em.merge(obj);
                List<StockAccessRight> sarList = sarDao.getOwnedByUser(obj);
                if (CollectionUtils.isNotEmpty(sarList)) {
                    for (StockAccessRight sar : sarList) {
                        em.remove(sar);
                    }
                }
                em.remove(tmp);
                res.add(tmp);
            }
            t.commit();
            return res;
        } catch (Exception e) {
            t.rollback();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public List<User> getUsers() {
        EntityManager em = PersistenceUtil.getEntityManager();
        List<User> users = em.createQuery("select user from User user order by user.userName").getResultList();
        return users;
    }

    @SuppressWarnings("unchecked")
    public List<Long> getUserIds(List<Organization> orgs) {
        if (orgs == null || orgs.isEmpty()) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        List<Long> orgIds = new ArrayList<Long>();
        for (Organization o : orgs) {
            orgIds.add(o.getId());
        }

        try {
            return (List<Long>) em.createQuery("SELECT DISTINCT u.id FROM User u WHERE u.organization.id IN(" + StringUtils.join(orgIds, ',') + ")")
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<User> getUsers(Organization org, Integer first, Integer pageSize) {
        if (org == null) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            Query q = em.createQuery("SELECT DISTINCT u FROM User u WHERE u.organization.id = :orgId ORDER BY u.userName");
            q.setParameter("orgId", org.getId());
            if (first != null) {
                q.setFirstResult(first.intValue());
            }
            if (pageSize != null) {
                q.setMaxResults(pageSize.intValue());
            }
            return (List<User>) q.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUsersCount(Organization org) {
        if (org == null) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            Query q = em.createQuery("SELECT COUNT(DISTINCT u) FROM User u WHERE u.organization.id = :orgId ORDER BY u.userName");
            q.setParameter("orgId", org.getId());
            return (Long) q.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<User> getUsers(Organization org) {
        if (org == null) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            return (List<User>) em.createQuery("SELECT DISTINCT u FROM User u WHERE u.organization.id = :orgId ORDER BY u.userName").setParameter("orgId",
                    org.getId()).getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<User> getNonPrivilegedUsersByEmail(String email) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return (List<User>) em.createQuery("SELECT DISTINCT u FROM User u WHERE u.email = :email AND u.superAdminPermission = false").setParameter("email", email).getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<User> getOrgLessUsers() {
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            return (List<User>) em.createQuery("SELECT DISTINCT u FROM User u WHERE u.organization is NULL ORDER BY u.userName").getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public User getUser(String userName) {
        EntityManager em = PersistenceUtil.getEntityManager();

        User user = null;
        try {
            user = (User) em.createQuery("select user from User user where user.userName=:userName").setParameter("userName", userName).getSingleResult();
        } catch (NoResultException e) {
            // we do nothing here; just return null
        }
        return user;
    }

    public User getUserByApiKey(String apiKey) {
        EntityManager em = PersistenceUtil.getEntityManager();

        User user = null;
        try {
            user = (User) em.createQuery("select user from User user where user.apiKey=:apiKey").setParameter("apiKey", apiKey).getSingleResult();
        } catch (NoResultException e) {
            // we do nothing here; just return null
        }
        return user;
    }

    public String getSalt(String userName) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return (String) em.createQuery("select u.passwordHashSalt from User u where u.userName=:userName").setParameter("userName", userName)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public String getStandardJoinStatement() {
        List<String> joinStatements = new ArrayList<String>();

        joinStatements.add("LEFT JOIN a.acceptedAdditionalTermsMap addTermMap");
        this.registeredAliases.put("acceptedAdditionalTermsMap", "addTermMap");

        return String.join(" ", joinStatements);
    }

}
