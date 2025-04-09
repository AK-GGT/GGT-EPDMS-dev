package de.iai.ilcd.webgui.controller.url;

public class PrivacyPolicyURLGenerator extends AbstractURLGenerator {

    /**
     * Initializes the privacy policy URL Generator.
     *
     * @param urlBean The URL generator bean
     */
    public PrivacyPolicyURLGenerator(URLGeneratorBean urlBean) {
        super(urlBean);
    }

    /**
     * Gets the URL to show privacy policy.
     *
     * @return A generated URL to show privacy policy
     */
    public String getShow() {
        return "/privacyPolicy.xhtml";
    }

}
