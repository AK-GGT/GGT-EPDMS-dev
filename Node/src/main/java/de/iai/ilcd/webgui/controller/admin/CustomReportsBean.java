package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.ReportConfig;
import de.iai.ilcd.service.CustomReportService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;

/**
 * The managed bean for retrieving customized reports.
 *
 * @author sarai
 */
@ManagedBean(name = "customReportsBean")
@ApplicationScoped
public class CustomReportsBean {

    private Logger log = LogManager.getLogger(this.getClass());

    private CustomReportService customReportService;

    public CustomReportsBean() {
        WebApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        this.customReportService = ctx.getBean(CustomReportService.class);
    }

    public List<ReportConfig> getReportConfigs() {
        return customReportService.getReportConfigs();
    }

    /**
     * Checks whether custom reports are enabled.
     *
     * @return true if at least one custom report is set.
     */
    public boolean isCustomReportsEnabled() {
        return (customReportService.getReportConfigs() != null)
                && !customReportService.getReportConfigs().isEmpty();
    }

    /**
     * Executes an SQL query for a report and write result directly to the response
     *
     * @param request  The request to execute
     * @param fileName he desired file name of csv result
     */
    public void executeSQL(final ReportConfig report) {
        try {
            String fileName = new String(report.getFileName()).replace(" ", "_");
            if (report.isTimeStampSuffix()) {
                DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm");
                fileName += "_" + dtf.print(new DateTime());
            }
            fileName += ".csv";
            if (log.isDebugEnabled())
                log.debug("filename is " + fileName);

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.setResponseContentType("text/csv");
            externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            customReportService.executeSQLQuery(report, externalContext.getResponseOutputStream());

            facesContext.responseComplete();
        } catch (IOException e) {
            log.error("Error writing response", e);
            if (log.isDebugEnabled())
                e.printStackTrace();
        }
    }

    public CustomReportService getReportService() {
        return this.customReportService;
    }

}
