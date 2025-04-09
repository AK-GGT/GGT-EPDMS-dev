package de.iai.ilcd.util.converter;

import de.iai.ilcd.model.dao.IndustrialSectorDao;
import de.iai.ilcd.model.security.IndustrialSector;

import javax.faces.convert.FacesConverter;

/**
 * Converter for JSF Facelets with sector objects in form selections
 */
@FacesConverter(value = "sectorConverter", forClass = IndustrialSector.class)
public class SectorConverter extends AbstractEntityConverter<IndustrialSector> {

    /**
     * Create converter
     */
    public SectorConverter() {
        super(new IndustrialSectorDao(), IndustrialSector.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getId(IndustrialSector obj) {
        return obj.getId().toString();
    }

}