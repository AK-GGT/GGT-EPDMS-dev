package de.iai.ilcd.util.converter;

import de.iai.ilcd.model.dao.DependenciesMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.io.Serializable;

/**
 * Converter for dependencies options, must be a bean due to dependency injection, use via
 * <code>converter=&quot;#{dependenciesOptionConverter}&quot;</code>
 */
@SessionScoped
@ManagedBean(name = "dependenciesOptionConverter")
public class DependenciesOptionConverter implements Converter, Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -6898653056457977172L;
    private static Logger logger = LogManager.getLogger(DependenciesOptionConverter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent ui, String value) {
        try {
            if (logger.isTraceEnabled())
                logger.trace("getAsObject(): " + value);
            return DependenciesMode.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent ui, Object value) {
        if (logger.isTraceEnabled())
            logger.trace("getAsString(): " + value);
        return value != null ? ((DependenciesMode) value).name() : null;
    }

}
