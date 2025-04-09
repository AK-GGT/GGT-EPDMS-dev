package de.iai.ilcd.model.common;

/**
 * The model for aditional terms defined in config file.
 *
 * @author sarai
 */
public class AdditionalTerm {

    private Boolean requireAcceptance;

    private String requiredMessage;

    private String message;

    private String title;

    private Boolean renderAdditionalTerms;

    private String defaultGroup;

    public Boolean isRequireAcceptance() {
        return requireAcceptance;
    }

    public void setRequireAcceptance(Boolean requireAcceptance) {
        this.requireAcceptance = requireAcceptance;
    }

    public String getRequiredMessage() {
        return this.requiredMessage;
    }

    public void setRequiredMessage(String requiredMessage) {
        this.requiredMessage = requiredMessage;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean IsRenderAdditionalTerms() {
        return renderAdditionalTerms;
    }

    public void setRenderAdditionalTerms(Boolean renderAdditionalTerms) {
        this.renderAdditionalTerms = renderAdditionalTerms;
    }

    public String getDefaultGroup() {
        return this.defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

}
