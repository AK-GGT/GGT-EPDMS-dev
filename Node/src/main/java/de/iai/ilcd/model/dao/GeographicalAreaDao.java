package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.common.GeographicalArea;
import de.iai.ilcd.persistence.PersistenceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author clemens.duepmeier
 */
public class GeographicalAreaDao extends AbstractLongIdObjectDao<GeographicalArea> {

    private static final Logger logger = LogManager.getLogger(GeographicalAreaDao.class);
    private EntityManager em = PersistenceUtil.getEntityManager();

    public GeographicalAreaDao() {
        super("GeographicalArea", GeographicalArea.class);
    }

    public List<GeographicalArea> getAreas() {
        return this.getAll();
    }

    @SuppressWarnings("unchecked")
    public List<GeographicalArea> getCountries() {

        List<GeographicalArea> countries = new ArrayList<GeographicalArea>();

        countries = this.em.createQuery("select area from GeographicalArea area where LENGTH(area.areaCode)=2 order by area.name").getResultList();

        return countries;
    }

    public GeographicalArea getArea(Long id) {
        return this.getById(id);
    }

    public GeographicalArea getArea(String areaCode) {
        try {
            return this.em.createQuery("select area from GeographicalArea area where area.areaCode=:areaCode", GeographicalArea.class).setParameter(
                    "areaCode", areaCode).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
