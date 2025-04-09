package de.iai.ilcd.webgui.controller.url;

public class ImprintURLGenerator extends AbstractURLGenerator {

    /**
     * Initializes the imprint URL Generator.
     *
     * @param urlBean The URL generator bean
     */
    public ImprintURLGenerator(URLGeneratorBean urlBean) {
        super(urlBean);
    }

    /**
     * Gets the URL to show imprint.
     *
     * @return A generated URL to show imprint
     */
    public String getShow() {
        return "/imprint.xhtml";
    }

}
