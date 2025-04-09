package de.iai.ilcd.util.converter;

import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.process.Process;

import javax.faces.convert.FacesConverter;

/**
 * Converter for JSF Facelets with Process objects in form selections
 */
@FacesConverter(value = "processConverter", forClass = Process.class)
public class ProcessConverter extends AbstractEntityConverter<Process> {

    /**
     * Create converter
     */
    public ProcessConverter() {
        super(new ProcessDao(), Process.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getId(Process obj) {
        return obj.getId().toString();
    }

}