package de.iai.ilcd.util.converter;

import de.iai.ilcd.model.common.PushTarget;
import de.iai.ilcd.model.dao.PushTargetDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * This is the class for converting a {@link PushTarget} object into a printable String.
 *
 * @author sarai
 */
@FacesConverter(value = "targetConverter")
public class PushTargetConverter implements Converter {

    private static Logger logger = LogManager.getLogger(PushTargetConverter.class);

    /**
     * Gets unique name of given PushTarget object.
     *
     * @param obj The PushTarget object
     * @return The name of given PushTarget object.
     */
    protected String getId(PushTarget obj) {
        return obj.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext contect, UIComponent comp, String objStr) {
        try {
            if (logger.isTraceEnabled())
                logger.trace("getAsObject(): " + objStr);
            PushTargetDao dao = new PushTargetDao();
            return dao.getPushTarget(objStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext contect, UIComponent comp, Object obj) {
        if (logger.isTraceEnabled())
            logger.trace("getAsString(): " + obj);
        if (PushTarget.class.isInstance(obj)) {
            return this.getId((PushTarget) obj);
        }
        return "";
    }

}
