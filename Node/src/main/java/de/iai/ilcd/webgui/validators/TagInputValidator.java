package de.iai.ilcd.webgui.validators;

import eu.europa.ec.jrc.lca.commons.view.util.Messages;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.io.UnsupportedEncodingException;

@FacesValidator("de.iai.ilcd.webgui.validators.TagInputValidator")
public class TagInputValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (!isStringValid((String) value)) {
            FacesMessage message = Messages.getMessage("resources.lang", "admin.tags.edit.invalidInput", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }

    }

    private boolean isStringValid(String text) {
        try {
            text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        return true;
    }
}
