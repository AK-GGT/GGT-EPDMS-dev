package de.iai.ilcd.webgui.validators;

import eu.europa.ec.jrc.lca.commons.view.util.Messages;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("de.iai.ilcd.webgui.validators.PasswordRequirementsValidator")
public class PasswordRequirementsValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        // helper variable to determine which requirement is not met
        String whichRequirement = matchesRequirements((String) value);
        // if any requirements are not met throw an exception with the corresponding error resource
        if (whichRequirement != "") {
            FacesMessage message = Messages.getMessage("resources.lang", "admin.user." + whichRequirement, null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }

    }

    private String matchesRequirements(String password) {
        // returns the error resource for password not long enough if not met
        if (password.length() < 10) return "passwLengthNotMet";

        int requirementsCounter = 0;

        // checks if password contains any digits
        if (password.matches(".*\\d.*")) requirementsCounter++;
        // checks if password contains lowercase letters
        if (password.matches(".*[a-z].*")) requirementsCounter++;
        // checks if password contains uppercase letters
        if (password.matches(".*[A-Z].*")) requirementsCounter++;
        // checks if password contains any non-word character
        if (password.matches(".*[^\\w].*")) requirementsCounter++;

        // returns the error resource for complexity not met if at least 3 requirements were not met
        if (requirementsCounter < 3) return "passwComplexityNotMet";
        // return empty string if all requirements were met
        return "";
    }

}
