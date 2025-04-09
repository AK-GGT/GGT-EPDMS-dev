package de.iai.ilcd.webgui.controller.admin;

import com.okworx.ilcd.validation.profile.Profile;
import com.okworx.ilcd.validation.profile.ProfileManager;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@FacesConverter("profileConverter")
public class ProfileConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null && value.trim().length() > 0) {
            try {
                Collection<Profile> collProfiles = ProfileManager.getInstance().getProfiles();
                List<Profile> profiles = new ArrayList<Profile>(collProfiles);

                for (Profile profile : profiles) {
                    if (profile.getName().equals(value)) {
                        return profile;
                    }
                }

                return null;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid profile."));
            }
        } else {
            return null;
        }

    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        if (object instanceof Profile) {
            return ((Profile) object).getName();
        } else {
            return "";
        }

    }


}
