package de.iai.ilcd.webgui.controller.url;

/**
 * URL Generator for Job list link
 *
 * @author sarai
 */
public class JobsURLGenerator extends AbstractURLGenerator {

    /**
     * Initializes the job list URL Generator.
     *
     * @param urlBean The URL generator bean
     */
    public JobsURLGenerator(URLGeneratorBean urlBean) {
        super(urlBean);
    }

    /**
     * Gets the URL to show the job list.
     *
     * @return A generated URL to show job list
     */
    public String getShowList() {
        return "/admin/showJobs.xhtml";
    }

}
