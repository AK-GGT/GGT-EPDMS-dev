package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.common.CategoryDefinition;

public class CategoriesDefinitionDao extends AbstractLongIdObjectDao<CategoryDefinition> {

    /**
     * Default constructor
     */
    public CategoriesDefinitionDao() {
        super("CategoryDefinition", CategoryDefinition.class);
    }

}
