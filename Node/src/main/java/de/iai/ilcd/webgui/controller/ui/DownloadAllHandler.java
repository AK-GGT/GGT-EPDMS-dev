package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@ManagedBean
public class DownloadAllHandler extends AbstractHandler {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -1279348075136230465L;

    private Logger logger = LogManager.getLogger("DownloadAllHandler");

    private StreamedContent file;

    public DownloadAllHandler() {

        String zipDirectory = ConfigurationService.INSTANCE.getZipFileDirectory();
        File zipFile = new File(zipDirectory + "/ilcd.zip");
        if (!zipFile.canRead()) {
            this.addI18NFacesMessage("facesMsg.zipNotAvailable", FacesMessage.SEVERITY_ERROR);
            this.logger.error("Cannot find zip file {}", zipFile.getAbsoluteFile());
            return;
        }
        try {
            this.file = DefaultStreamedContent.builder()
                    .contentType("application/zip")
                    .name("ilcd.zip")
                    .stream(() -> {
                        try {
                            return new FileInputStream(zipFile);
                        } catch (FileNotFoundException e) {
                            logger.error("Zip file not found or cannot be opened (e.g. being a directory).", e);
                        }
                        return null;
                    }).build();
        } catch (Exception e) {
            this.addI18NFacesMessage("facesMsg.zipOpeningError", FacesMessage.SEVERITY_ERROR);
            this.logger.error("Cannot open zip file with all data sets");
            return;
        }
    }

    public StreamedContent getFile() {
        return this.file;
    }

    public void setFile(StreamedContent file) {
        this.file = file;
    }
}
