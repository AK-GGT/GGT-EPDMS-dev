package de.iai.ilcd.webgui.controller.util;

import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.webgui.controller.admin.AutomaticQualityCheckHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ManagedBean
@ViewScoped
public class QualityCheckResultDocumentView extends AbstractDocumentView {

    /**
     *
     */
    private static final long serialVersionUID = -6791366250030071658L;
    private static Logger logger = LogManager.getLogger(QualityCheckResultDocumentView.class);
    @ManagedProperty(value = "#{automaticQualityCheckHandler}")
    private AutomaticQualityCheckHandler automaticQualityCheckHandler;

    @Override
    public void postProcessXLS(Object document) {
        super.postProcessXLS(document);

        List<Process> processes = automaticQualityCheckHandler.getSelectedProcesses();
        StringBuilder sbName = new StringBuilder("Metadata for quality check log ");
        sbName.append(processes.size());
        sbName.append(" datasets");

        StringBuilder process_names = new StringBuilder();
        StringBuilder threshold = new StringBuilder();

        threshold.append(automaticQualityCheckHandler.getThreshold()).append("%");

        StringBuilder stock_names = new StringBuilder();
        List<DataStock> stocks = automaticQualityCheckHandler.getSelectedStocks();
        for (DataStock stock : stocks) {
            stock_names.append(stock.getName());
            if (!stock.equals(stocks.get(stocks.size() - 1))) {
                stock_names.append(", ");
            }
        }

        for (Process process : processes) {
            process_names.append(process.getName().getDefaultValue());
            if (!process.equals(processes.get(processes.size() - 1))) {
                process_names.append(", ");
            }
        }

        Map<String, String> additionalDescription = new HashMap<String, String>();
        additionalDescription.put("Data stocks: ", stock_names.toString());
        additionalDescription.put("Processes:", process_names.toString());
        additionalDescription.put("Threshold:", threshold.toString());

        super.createDescriptionSheet(sbName.toString(), additionalDescription);

    }

    public AutomaticQualityCheckHandler getAutomaticQualityCheckHandler() {
        return automaticQualityCheckHandler;
    }

    public void setAutomaticQualityCheckHandler(AutomaticQualityCheckHandler automaticQualityCheckHandler) {
        this.automaticQualityCheckHandler = automaticQualityCheckHandler;
    }

}
