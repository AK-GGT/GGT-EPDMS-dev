package de.iai.ilcd.service;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.ReportConfig;
import de.iai.ilcd.persistence.PersistenceUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.ResultType;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The service for reading customized reports which are set in config file and
 * processing SQL requests of customized reports.
 *
 * @author sarai
 */
@Service("customReportService")
public class CustomReportService {

    private Logger log = LogManager.getLogger(this.getClass());

    private List<ReportConfig> reportConfigs = new ArrayList<ReportConfig>();

    /**
     * Instantiation of custom report service; reads out config file for
     * customized reports.
     */
    CustomReportService() {
        init();
    }

    public void init() {
        log.debug("initialize CustomReportService");
        Configuration config = ConfigurationService.INSTANCE.getProperties();

        int count = 1;
        log.debug("got all infos from config file.");
        String currentType;
        do {
            currentType = config.getString("feature.reports." + count + ".type");
            if (currentType != null) {
                String currentName = config.getString("feature.reports." + count + ".name");
                String currentFunction = config.getString("feature.reports." + count + ".function");
                Integer currentIndex = config.getInteger("feature.reports." + count + ".index", null);
                String currentIcon = config.getString("feature.reports." + count + ".icon", null);
                String currentFileName = config.getString("feature.reports." + count + ".filename", "report");
                boolean currentTimeStampSuffix = config.getBoolean("feature.reports." + count + ".filename.timestamp", true);
                createReportConfig(currentType, currentName, currentFunction, currentIndex, currentIcon, currentFileName, currentTimeStampSuffix);
                count++;
            }
            log.debug("updated temporary configs");
        } while (currentType != null);
        Collections.sort(reportConfigs);
    }

    public void reInit() {
        reportConfigs.clear();
        init();
    }

    /**
     * Creates a report config instance with given information.
     *
     * @param type     The report type
     * @param name     The shown report name
     * @param function The report function
     * @param index    The order of report shown
     * @param icon     The icon of shown report
     * @param fileName The file name of downloadable report
     */
    private void createReportConfig(String type, String name, String function, Integer index, String icon,
                                    String fileName, boolean timeStampSuffix) {
        if (type.equals("sqlquery") || type.equals("url")) {
            ReportConfig reportConfig = new ReportConfig();
            reportConfig.setType(type);
            reportConfig.setName(name);
            reportConfig.setFunction(function);
            reportConfig.setIndex(index);
            if (icon != null) {
                if (icon.startsWith("ui-icon-")) {
                    reportConfig.setIcon("ui-icon " + icon);
                } else if (icon.startsWith("fa-")) {
                    reportConfig.setIcon("fa " + icon);
                }
            }
            reportConfig.setFileName(fileName);
            reportConfig.setTimeStampSuffix(timeStampSuffix);
            log.debug("Created new ReportConfig with all relevant information");
            reportConfigs.add(reportConfig);
            log.debug("added new report config to list.");
        }
    }

    /**
     * Executes an SQL query for a report and writes the results as CSV to an outputstream.
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void executeSQLQuery(ReportConfig report, OutputStream out) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("request: '" + report.getFunction() + "'");
        }

        EntityManager em = PersistenceUtil.getEntityManager();
        Query query = em.createNativeQuery(report.getFunction());
        query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);

        List<Map<Object, Object>> results = query.getResultList();
        if (log.isDebugEnabled()) {
            log.debug("count of results: " + results.size());
        }

        writeResults(results, out);
    }

    private void writeResults(List<Map<Object, Object>> results, OutputStream out) throws IOException {
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));

        StringBuilder sb = new StringBuilder();
        Map<Object, Object> firstKeySet = results.get(0);
        for (Object firstKey : firstKeySet.keySet()) {
            sb.append(firstKey).append(";");
        }
        sb.append("\n");
        w.write(sb.toString());

        for (Map<Object, Object> result : results) {
            sb = new StringBuilder();
            log.debug("new line");
            for (Object key : result.keySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("result: " + key + " : " + result.get(key));
                }
                sb.append(result.get(key) == null ? " " : result.get(key)).append(";");
            }
            sb.append("\n");
            w.write(sb.toString());
        }

        w.close();
        log.debug("closed writer.");
    }

    public List<ReportConfig> getReportConfigs() {
        return reportConfigs;
    }
}
