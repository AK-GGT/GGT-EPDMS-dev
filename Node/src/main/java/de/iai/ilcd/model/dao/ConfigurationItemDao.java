package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.common.ConfigurationItem;
import de.iai.ilcd.persistence.PersistenceUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

public class ConfigurationItemDao extends AbstractLongIdObjectDao<ConfigurationItem> {

    public ConfigurationItemDao() {
        super("ConfigurationItem", ConfigurationItem.class);
    }

    /**
     * Gets list of ConfigurationItem entries.
     *
     * @return List of configuration item entries
     */
    public List<ConfigurationItem> getConfigurationItemList() {
        return this.getAll();
    }

    /**
     * Gets configuration item with given name.
     *
     * @param property The name of wanted configuration item entry
     * @return a configuration item entry with given name
     */
    public ConfigurationItem getConfigurationItem(String property) {
        EntityManager em = PersistenceUtil.getEntityManager();
        ConfigurationItem configurationItem = null;
        try {
            configurationItem = (ConfigurationItem) em.createQuery("select c from ConfigurationItem c where c.property=:property")
                    .setParameter("property", property).getSingleResult();
        } catch (NoResultException nre) {
            // return null

        }
        return configurationItem;
    }

    /**
     * Gets Configuration item with given id.
     *
     * @param id The id of wanted configuration item
     * @return A configuration item with given id
     */
    public ConfigurationItem getConfigurationItem(Long id) {
        return this.getById(id);
    }

}
