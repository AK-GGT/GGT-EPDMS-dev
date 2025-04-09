package de.iai.ilcd.security;

import de.iai.ilcd.model.dao.AbstractDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.model.security.UserGroup;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.security.StockAccessRight.AccessRightType;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for {@link StockAccessRight}
 */
public class StockAccessRightDao extends AbstractDao<StockAccessRight> {

    private static final Logger log = LogManager.getLogger(StockAccessRightDao.class);

    public StockAccessRightDao() {
        super("StockAccessRight", StockAccessRight.class);
    }

    /**
     * Get all access rights for a specific user
     *
     * @param u user to get access rights for
     * @return list of access rights
     */
    public List<StockAccessRight> get(User u) {
        if (u == null) {
            return this.get(0L, null);
        }
        try {
            // groups
            List<Long> groupIds = new ArrayList<Long>();
            for (UserGroup g : u.getGroups()) {
                groupIds.add(g.getId());
            }
            return this.get(u.getId(), groupIds);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get all access rights for a specific user
     *
     * @param uid      user id
     * @param groupIds group IDs (may be <code>null</code> or empty)
     * @return list of access rights
     */
    @SuppressWarnings("unchecked")
    public List<StockAccessRight> get(Long uid, List<Long> groupIds) {
        if (uid == null) {
            uid = 0L;
        }
        try {
            final boolean additionalGuestParam = uid > 0;
            final boolean includeGroups = groupIds != null && !groupIds.isEmpty();

            StringBuilder queryBuilder = new StringBuilder("SELECT sar FROM StockAccessRight sar WHERE ");

            // add guest rights to every registered user
            if (additionalGuestParam) {
                queryBuilder.append("(sar.accessRightType=:artUsr AND (sar.ugId=:uid OR sar.ugId=0)) ");
            } else {
                queryBuilder.append("(sar.accessRightType=:artUsr AND sar.ugId=:uid) ");
            }

            // groups
            if (includeGroups) {
                queryBuilder.append("OR (sar.accessRightType=:artGrp AND sar.ugId IN (").append(StringUtils.join(groupIds, ',')).append("))");
            }

            EntityManager em = PersistenceUtil.getEntityManager();
            Query q = em.createQuery(queryBuilder.toString());
            q.setParameter("artUsr", AccessRightType.User);
            q.setParameter("uid", uid);
            if (includeGroups) {
                q.setParameter("artGrp", AccessRightType.Group);
            }

            return (List<StockAccessRight>) q.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get all access rights for owned by a specific user
     *
     * @param u user
     * @return list of access rights
     */
    @SuppressWarnings("unchecked")
    public List<StockAccessRight> getOwnedByUser(User u) {
        if (u == null) {
            return null;
        }

        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT sar FROM StockAccessRight sar WHERE sar.accessRightType=:artUsr AND sar.ugId=:uid ");

            Query q = em.createQuery(queryBuilder.toString());
            q.setParameter("artUsr", AccessRightType.User);
            q.setParameter("uid", u.getId());

            return (List<StockAccessRight>) q.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get all access rights for owned by a specific group
     *
     * @param g group
     * @return list of access rights
     */
    @SuppressWarnings("unchecked")
    public List<StockAccessRight> getOwnedByGroup(UserGroup g) {
        if (g == null) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT sar FROM StockAccessRight sar WHERE sar.accessRightType=:artGrp AND sar.ugId=:gid ");

            Query q = em.createQuery(queryBuilder.toString());
            q.setParameter("artGrp", AccessRightType.Group);
            q.setParameter("gid", g.getId());

            return (List<StockAccessRight>) q.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get all access rights for a {@link AbstractDataStock data stock}
     *
     * @param ds data stock to get rights for
     * @return list of access rights
     */
    @SuppressWarnings("unchecked")
    public List<StockAccessRight> get(AbstractDataStock ds) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            Query q = em.createQuery("SELECT sar FROM StockAccessRight sar WHERE sar.stockId=:id");
            q.setParameter("id", ds.getId());
            return (List<StockAccessRight>) q.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get all access rights for a {@link AbstractDataStock data stock}
     *
     * @param ds   data stock to get rights for
     * @param type type of access (for user or group)
     * @return list of access rights
     */
    @SuppressWarnings("unchecked")
    public List<StockAccessRight> get(AbstractDataStock ds, AccessRightType type) {
        if (type == null) {
            return this.get(ds);
        }
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            StringBuilder sb = new StringBuilder("SELECT sar FROM StockAccessRight sar ");

            if (type.equals(AccessRightType.User))
                sb.append(" LEFT JOIN User u ON sar.ugId = u.id");

            sb.append(" WHERE sar.stockId=:id AND sar.accessRightType=:type");

            if (type.equals(AccessRightType.User))
                sb.append(" ORDER BY u.userName");

            Query q = em.createQuery(sb.toString());

            q.setParameter("id", ds.getId());
            q.setParameter("type", type);
            return (List<StockAccessRight>) q.getResultList();
        } catch (Exception e) {
            log.error("error retrieving StockAccessRights", e);
            return null;
        }
    }

}
