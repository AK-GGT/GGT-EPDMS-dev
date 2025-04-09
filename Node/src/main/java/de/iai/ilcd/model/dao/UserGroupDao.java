package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.model.security.UserGroup;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.StockAccessRightDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

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
public class UserGroupDao extends AbstractLongIdObjectDao<UserGroup> {

    public UserGroupDao() {
        super("UserGroup", UserGroup.class);
    }

    public List<UserGroup> getGroups() {
        return this.getAll();
    }

    /**
     * {@inheritDoc} <br />
     * Deletes the relevant {@link StockAccessRight stock access rights} as well
     */
    @Override
    public UserGroup remove(UserGroup obj) throws Exception {
        if (obj == null) {
            return null;
        }

        StockAccessRightDao sarDao = new StockAccessRightDao();

        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        try {
            t.begin();
            List<StockAccessRight> sarList = sarDao.getOwnedByGroup(obj);
            UserGroup tmp = em.contains(obj) ? obj : em.merge(obj);
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
    public Collection<UserGroup> remove(Collection<UserGroup> objs) throws Exception {
        if (CollectionUtils.isEmpty(objs)) {
            return null;
        }

        StockAccessRightDao sarDao = new StockAccessRightDao();
        Collection<UserGroup> res = new ArrayList<UserGroup>();
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        try {
            t.begin();
            for (UserGroup obj : objs) {
                UserGroup tmp = em.contains(obj) ? obj : em.merge(obj);
                List<StockAccessRight> sarList = sarDao.getOwnedByGroup(obj);
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
    public List<Long> getGroupIds(List<Organization> orgs) {
        if (orgs == null || orgs.isEmpty()) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        List<Long> orgIds = new ArrayList<Long>();
        for (Organization o : orgs) {
            orgIds.add(o.getId());
        }

        try {
            return (List<Long>) em.createQuery("SELECT DISTINCT g.id FROM UserGroup g WHERE g.organization.id IN(" + StringUtils.join(orgIds, ',') + ")")
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<UserGroup> getGroups(Organization org) {
        if (org == null) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            return (List<UserGroup>) em.createQuery("SELECT DISTINCT g FROM UserGroup g WHERE g.organization.id = :orgId ORDER BY g.groupName").setParameter(
                    "orgId", org.getId()).getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<UserGroup> getGroups(Organization org, Integer first, Integer pageSize) {
        if (org == null) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            Query q = em.createQuery("SELECT DISTINCT g FROM UserGroup g WHERE g.organization.id = :orgId ORDER BY g.groupName");
            q.setParameter("orgId", org.getId());
            if (first != null) {
                q.setFirstResult(first.intValue());
            }
            if (pageSize != null) {
                q.setMaxResults(pageSize.intValue());
            }
            return (List<UserGroup>) q.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getGroupsCount(Organization org) {
        if (org == null) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            Query q = em.createQuery("SELECT COUNT(DISTINCT g) FROM UserGroup g WHERE g.organization.id = :orgId ORDER BY g.groupName");
            q.setParameter("orgId", org.getId());
            return (Long) q.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<UserGroup> getOrgLessGroups() {
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            return (List<UserGroup>) em.createQuery("SELECT DISTINCT g FROM UserGroup g WHERE g.organization is NULL ORDER BY g.groupName").getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public UserGroup getGroup(String groupName) {
        EntityManager em = PersistenceUtil.getEntityManager();
        UserGroup group = null;
        try {
            group = (UserGroup) em.createQuery("select g from UserGroup g where g.groupName=:groupName").setParameter("groupName", groupName)
                    .getSingleResult();
        } catch (NoResultException e) {
            // we do nothing here; just return null
        }
        return group;
    }

    public UserGroup getGroup(long groupId) {
        return this.getById(groupId);
    }

}
