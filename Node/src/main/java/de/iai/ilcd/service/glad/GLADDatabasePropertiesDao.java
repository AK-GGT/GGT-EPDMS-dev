package de.iai.ilcd.service.glad;

import de.iai.ilcd.model.dao.AbstractLongIdObjectDao;

/**
 * The Data Access Object (DAO) for GLAD database properties.
 *
 * @author sarai
 */
public class GLADDatabasePropertiesDao extends AbstractLongIdObjectDao<GLADDatabaseProperties> {


    public GLADDatabasePropertiesDao() {
        super("GLADDatabaseProperties", GLADDatabaseProperties.class);
    }

    public GLADDatabaseProperties getGLADDatabaseProperties(Long id) {
        return this.getById(id);
    }
}
