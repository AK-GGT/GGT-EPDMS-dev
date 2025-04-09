package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.api.binding.helper.DatasetDAO;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.service.ConversionService;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.ui.AvailableStockHandler;
import de.iai.ilcd.xml.read.DataSetImporter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
@ManagedBean
@SessionScoped
public class ImportHandler extends AbstractHandler {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -3653503676524342696L;

    public static Logger logger = LogManager.getLogger(ImportHandler.class);

    private final String handlerId;

    private final List<UploadedFileInformation> uploadedFiles = new ArrayList<UploadedFileInformation>();

    private List<String> availableRootStockNames;

    private String selectedStockName;

    @ManagedProperty(value = "#{availableStocks}")
    private AvailableStockHandler availableStocksHandler;

    @ManagedProperty(value = "#{profileHandler}")
    private ProfileHandler profileHandler;

    public ImportHandler() {
        this.handlerId = UUID.randomUUID().toString();
    }

    public void loadRootDataStocks() {
        this.availableRootStockNames = new ArrayList<String>();
        for (IDataStockMetaData m : this.availableStocksHandler.getAllStocksMeta()) {
            if (m.isRoot() && SecurityUtil.hasImportPermission(m)) {
                this.availableRootStockNames.add(m.getName());
            }
        }
        // if only 1 possible, preset this as selected data stock to import to
        if (this.availableRootStockNames.size() == 1) {
            this.setSelectedStockName(this.availableRootStockNames.get(0).toString());
        }
    }

    public void handleFileUpload(FileUploadEvent event) throws AbortProcessingException {
        UploadedFile file = event.getFile();
        if (file != null) { // i.e. we have success
            try {
                UploadedFileInformation fileInformation = this.analyzeAndCopyFile(file);
                if (fileInformation != null)
                    this.uploadedFiles.add(fileInformation); // add to the list
                ImportHandler.logger.info("Uploaded file: " + file.getFileName());
                this.addI18NFacesMessage("facesMsg.import.fileuploadSuccess", FacesMessage.SEVERITY_INFO);
            } catch (IOException e) {
                logger.error(e.getMessage());
                this.addI18NFacesMessage("facesMsg.import.fileuploadError1", FacesMessage.SEVERITY_ERROR);
            }
        } else {
            this.addI18NFacesMessage("facesMsg.import.fileuploadError2", FacesMessage.SEVERITY_ERROR);
        }
    }

    public void doImport() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        // Since CharsetFilter was applied as workaround for PrimeFaces 3.x charset problems
        // specification of response charset is required
        response.setContentType("text/plain; charset=iso-8859-1");

        try {
            OutputStream outStream = response.getOutputStream();
            PrintWriter out = new PrintWriter(outStream);
            DataSetImporter xmlReader = new DataSetImporter();

            out.println("------ Begin import of files ------");
            out.println("Please DO NOT reload this page until finished!");
            // 1. import all files which are plain XML-files

            long start = System.currentTimeMillis();

            IDataStockMetaData dsMeta = this.availableStocksHandler.getStock(this.selectedStockName);

            CommonDataStockDao dsDao = new CommonDataStockDao();
            RootDataStock rds = dsDao.getRootDataStockById(dsMeta.getId());

            SecurityUtil.assertCanImport(rds);

            if (rds == null) {
                out.println("No valid root data stock selected - exiting!");
                return;
            } else {
                out.println("Data sets will be imported into root data stock: " + rds.getName() + " (" + rds.getLongTitle().getDefaultValue() + ")");
            }

            xmlReader.importFiles(this.getUploadDirectory() + "/", out, rds);

            for (UploadedFileInformation fileInf : this.uploadedFiles) {
                if (fileInf.getFileName().endsWith(".zip")) {
                    if (out != null) {
                        out.println("Import zip archive " + fileInf.getFileName());
                        out.flush();
                    }
                    xmlReader.importZipFile(fileInf.getPathName(), out, rds);
                }
            }


            long stop = System.currentTimeMillis();

            long duration = (stop - start) / 1000;

            ImportHandler.logger.info("import finished in " + duration + " seconds");

            out.println("------ import of files finished ------");
            out.flush();

            // OK, clean up the resources
            this.deleteUploadDirectory();
            this.uploadedFiles.clear();

        } catch (Exception e) {
            ImportHandler.logger.error("error: file import failed", e);
        } finally {
        }
        context.responseComplete();

    }

    public List<UploadedFileInformation> getUploadedFiles() {
        return this.uploadedFiles;
    }

    private void copyFile(InputStream inStream, String directory, String fileName) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            FileUtils.forceMkdir(dir);
        } else {
            // cleanup the directory first
            // FileUtils.cleanDirectory(dir);
        }
        String outputFilePath;
        if (directory.endsWith("/")) {
            outputFilePath = directory + fileName;
        } else {
            outputFilePath = directory + "/" + fileName;
        }

        File outputFile = new File(outputFilePath);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        IOUtils.copy(inStream, outputStream);
        outputStream.close();
    }

    private String getUploadDirectory() {
        return ConfigurationService.INSTANCE.getUploadDirectory() + File.separator + this.handlerId.toString();
    }

    private void deleteUploadDirectory() {

        String directory = this.getUploadDirectory();

        try {
            File dir = new File(directory);
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            ImportHandler.logger.error("Cannot delete upload directory " + directory);
        }
    }

    private String getDataSetDirectory(ILCDTypes dataSetType) {
        String directory = this.getUploadDirectory() + File.separator;
        switch (dataSetType) {
            case CONTACT:
                directory += DatasetTypes.CONTACTS.getValue();
                break;
            case FLOWPROPERTY:
                directory += DatasetTypes.FLOWPROPERTIES.getValue();
                break;
            case FLOW:
                directory += DatasetTypes.FLOWS.getValue();
                break;
            case PROCESS:
                directory += DatasetTypes.PROCESSES.getValue();
                break;
            case SOURCE:
                directory += DatasetTypes.SOURCES.getValue();
                break;
            case UNITGROUP:
                directory += DatasetTypes.UNITGROUPS.getValue();
                break;
            case LCIAMETHOD:
                directory += DatasetTypes.LCIAMETHODS.getValue();
                break;
            case LIFECYCLEMODEL:
                directory += DatasetTypes.LIFECYCLEMODELS.getValue();
                break;
            default:
                directory = null;
        }

        return directory;
    }

    private UploadedFileInformation analyzeAndCopyFile(UploadedFile file) throws IOException {

        UploadedFileInformation fileInformation = null;
        String ext = FilenameUtils.getExtension(file.getFileName()).toLowerCase();
        switch (ext) {
            case "xml":
                fileInformation = this.analyzeAndCopyXmlFile(file);
                break;
            case "zip":
                fileInformation = this.analyzeAndCopyArchive(file);
                break;

            // Ask conversion service to convert spreadsheets into zip.
            // Upload directory is exposed to the outside for ConvLCA to grab from.
            case "xls":
            case "xlsx":
                // ping baseURL of the conversion service
                // skip conversion if service not
                // 200 OK before 5 seconds timeout
//			if (ConversionService.ping(ConfigurationService.INSTANCE.getConvertXLSXAPI(), 5000) == 200)
                fileInformation = this.analyzeAndCopyXLS(file);
                break;
            default:
                fileInformation = this.analyzeAndCopyOtherFile(file);
        }

        return fileInformation;

    }

    private UploadedFileInformation analyzeAndCopyXmlFile(UploadedFile file) throws IOException {
        // xml file
        UploadedFileInformation fileInformation = new UploadedFileInformation();
        /*
         * CG / 2012-01-06: don't use only file.getFileName() - which uses FileItem.getName() -
         * because IE will return the whole path but not just the file name,
         * see http://commons.apache.org/fileupload/faq.html#whole-path-from-IE
         */
        String fileName = FilenameUtils.getName(file.getFileName());
        fileInformation.setFileName(fileName);
        fileInformation.setFileType(file.getContentType());
        fileInformation.setFileSize(file.getSize());
        InputStream in = file.getInputStream();
        DatasetDAO dataSetDao = new DatasetDAO();

        ILCDTypes type = dataSetDao.determineDatasetType(in);
        in.close();
        fileInformation.setIlcdType(type);
        if (type == null) {
            ImportHandler.logger.error("Cannot determine file type");
            return fileInformation;
        }

        String targetDirectory = this.getDataSetDirectory(type);
        if (targetDirectory == null) {
            fileInformation.setMessage("Warning: The XML file does not contain an ILCD data set; file ignored");
            return fileInformation;
        }
        in = file.getInputStream();
        this.copyFile(in, targetDirectory, fileName);
        in.close();
        fileInformation.setPathName(targetDirectory + File.separator + fileName);

        return fileInformation;
    }


    private UploadedFileInformation analyzeAndCopyXLS(UploadedFile file) throws IOException {
        UploadedFileInformation fileInformation = new UploadedFileInformation();

        String fileName = FilenameUtils.getName(file.getFileName());
        fileInformation.setFileName(fileName);

        String targetDirectory = ConfigurationService.INSTANCE.getUploadDirectory();
        InputStream in = file.getInputStream();
        this.copyFile(file.getInputStream(), targetDirectory, fileName);
        in.close();
        fileInformation.setPathName(targetDirectory + File.separator + fileName);

        // the whole upload directory is expected to be exposed to for ConvLCA.
        // ConversionService will also populate the remaining fileInformation
        ConversionService.XLSX2XML(fileInformation);
        return fileInformation;
    }

    private UploadedFileInformation analyzeAndCopyArchive(UploadedFile file) throws IOException {
        UploadedFileInformation fileInformation = new UploadedFileInformation();
        /*
         * CG / 2012-01-06: don't use only file.getFileName() - which uses FileItem.getName() -
         * because IE will return the whole path but not just the file name,
         * see http://commons.apache.org/fileupload/faq.html#whole-path-from-IE
         */
        String fileName = FilenameUtils.getName(file.getFileName());
        fileInformation.setFileName(fileName);
        fileInformation.setFileType(file.getContentType());
        fileInformation.setFileSize(file.getSize());

        String targetDirectory = this.getUploadDirectory() + File.separator + fileInformation.getSubFolder();
        InputStream in = file.getInputStream();
        this.copyFile(file.getInputStream(), targetDirectory, fileName);
        in.close();
        fileInformation.setPathName(targetDirectory + File.separator + fileName);

        return fileInformation;
    }

    private UploadedFileInformation analyzeAndCopyOtherFile(UploadedFile file) throws IOException {
        UploadedFileInformation fileInformation = new UploadedFileInformation();
        /*
         * CG / 2012-01-06: don't use only file.getFileName() - which uses FileItem.getName() -
         * because IE will return the whole path but not just the file name,
         * see http://commons.apache.org/fileupload/faq.html#whole-path-from-IE
         */
        String fileName = FilenameUtils.getName(file.getFileName());
        fileInformation.setFileName(fileName);
        fileInformation.setFileType(file.getContentType());
        fileInformation.setFileSize(file.getSize());

        String targetDirectory = this.getUploadDirectory() + File.separator + fileInformation.getSubFolder() + File.separator + "external_docs";
        InputStream in = file.getInputStream();
        this.copyFile(file.getInputStream(), targetDirectory, fileName);
        in.close();
        fileInformation.setPathName(targetDirectory + File.separator + fileName);

        return fileInformation;
    }

    public AvailableStockHandler getAvailableStocksHandler() {
        return this.availableStocksHandler;
    }

    public void setAvailableStocksHandler(AvailableStockHandler availableStocksHandler) {
        this.availableStocksHandler = availableStocksHandler;
    }

    public void setAvailableStocks(List<String> availableStocks) {
        this.availableRootStockNames = availableStocks;
    }

    public String getSelectedStockName() {
        return this.selectedStockName;
    }

    public void setSelectedStockName(String currentStockName) {
        this.selectedStockName = currentStockName;
    }

    public List<String> getAvailableRootStockNames() {
        loadRootDataStocks();
        return this.availableRootStockNames;
    }

    public void setAvailableRootStockNames(List<String> availableRootStockNames) {
        this.availableRootStockNames = availableRootStockNames;
    }


    public ProfileHandler getProfileHandler() {
        return profileHandler;
    }

    public void setProfileHandler(ProfileHandler profileHandler) {
        this.profileHandler = profileHandler;
    }

    public String clearFilesList() {
        logger.info("clearing list of uploaded files");
        this.uploadedFiles.clear();
        if (SecurityUtil.hasAdminAreaAccessRight())
            return "/admin/importUpload.xhtml?faces-redirect=true";
        else
            return "/upload.xhtml?faces-redirect=true";
    }

}

