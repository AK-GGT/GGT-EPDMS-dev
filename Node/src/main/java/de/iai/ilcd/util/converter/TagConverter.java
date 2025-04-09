package de.iai.ilcd.util.converter;

import de.iai.ilcd.model.dao.TagDao;
import de.iai.ilcd.model.tag.Tag;

import javax.faces.convert.FacesConverter;

/**
 * Converter for JSF Facelets with tag objects in pick list
 */
@FacesConverter(value = "tagConverter", forClass = Tag.class)
public class TagConverter extends AbstractEntityConverter<Tag> {

    /**
     * Create converter
     */
    public TagConverter() {
        super(new TagDao(), Tag.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getId(Tag obj) {
        return obj.getId().toString();
    }

}
