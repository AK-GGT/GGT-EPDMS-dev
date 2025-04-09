package de.iai.ilcd.webgui.controller.admin.export;

import de.fzk.iai.ilcd.api.Constants;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.fzk.iai.ilcd.zip.ILCDManifest;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.ExportTag;
import de.iai.ilcd.model.datastock.ExportType;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.model.utils.DataSetUUIDVersionComparator;
import de.iai.ilcd.rest.DataStockResource;
import de.iai.ilcd.rest.ProcessResource;
import de.iai.ilcd.util.DependenciesUtil;
import de.iai.ilcd.webgui.controller.admin.export.ExportSQLAdapter.DataStockSQLMeta;
import de.iai.ilcd.webgui.controller.util.csv.CSVFormatter;
import de.iai.ilcd.webgui.controller.util.ExportMode;
import de.iai.ilcd.webgui.controller.util.csv.DecimalSeparator;
import de.iai.ilcd.xml.zip.ZipArchiveBuilder;
import de.iai.ilcd.xml.zip.ZipArchiveBuilder.CompressionMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Named
@ViewScoped
public class DataExportController implements Serializable {

    private static final long serialVersionUID = 3800026862547967419L;

    private final Logger logger = LogManager.getLogger(DataExportController.class);

    private final int pageSize = 1000;

    private StreamedContent file;

    private IDataStockMetaData stock = null;

    private DataStockResource dataStockResource;

    private ExportMode exportMode = ExportMode.LATEST_ONLY_GLOBAL;

    private boolean dependencies = false;

    private boolean compact = true;

    @Inject
    private ExportSQLAdapter exportSQLAdapter;

    public DataExportController() {

    }

    public static String timeStampSuffix() {
        return "_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss"));
    }

    /**
     * export an entire data stock, serve cached file if existing
     *
     * @param stock
     * @return
     * @throws IOException
     */
    public Path export(IDataStockMetaData stock, ExportType type) throws IOException {

        if (stock == null) // means the whole database
            return doExport();

        CommonDataStockDao stockDao = new CommonDataStockDao();

        AbstractDataStock ads = stockDao.getDataStockById(stock.getId());

        if (logger.isDebugEnabled()) {
            logger.debug("exporting data stock: " + ads.getName() + " (" + type.getValue() + " " + type.ordinal()
                    + " / " + this.exportMode + " " + this.exportMode.ordinal() + ")");
        }

        /// Cache routine //
        if (!ads.getExportTag(type, this.exportMode).isModified()) {
            if (logger.isDebugEnabled())
                logger.debug("serving cached file " + ads.getExportTag(type, this.exportMode).getFile());
            try {
                File file = new File(ads.getExportTag(type, this.exportMode).getFile());
                if (file.exists())
                    return file.toPath();
                else {
                    logger.info("cached file not found, generating fresh one");
                    return doExport(stock, type);
                }
            } catch (Exception e) {
                logger.info("cached file not found, generating fresh one");
                return doExport(stock, type);
            }
        } else {
            if (logger.isDebugEnabled())
                logger.debug("cached file is not up-to-date, generating fresh one");
            return doExport(stock, type);
        }
    }

    /**
     * export a list of datasets
     *
     * @param datasets
     * @return
     * @throws IOException
     */
    public Path export(List<DataSet> datasets) throws IOException {
        return doExport(datasets);
    }

    protected Path doExport(IDataStockMetaData stock, ExportType type) throws IOException {
        if (type.equals(ExportType.ZIP))
            return doExport(stock);
        else
            return doExportCSV(stock, type);
    }

    private void putDatasetInList(DataSet ds, List<DataSet> processes, List<DataSet> prodflows, List<DataSet> elemflows,
                                  List<DataSet> flowproperties, List<DataSet> unitgroups, List<DataSet> sources, List<DataSet> contacts,
                                  List<DataSet> lciamethods, List<DataSet> lifecyclemodels) {
        if (ds instanceof Process)
            processes.add(ds);
        else if (ds instanceof Flow) {
            Flow flow = (Flow) ds;
            if (flow.getType().equals(TypeOfFlowValue.ELEMENTARY_FLOW))
                elemflows.add(ds);
            else
                prodflows.add(ds);
        } else if (ds instanceof Source)
            sources.add(ds);
        else if (ds instanceof Contact)
            contacts.add(ds);
        else if (ds instanceof FlowProperty)
            flowproperties.add(ds);
        else if (ds instanceof UnitGroup)
            unitgroups.add(ds);
        else if (ds instanceof LCIAMethod)
            lciamethods.add(ds);
        else if (ds instanceof LifeCycleModel)
            lifecyclemodels.add(ds);
    }

    protected Path doExport(List<DataSet> datasets) throws IOException {
        // the new archive
//		TFile zip = createZipFile(true);
        Path zipPath = createZipFile(null, true);
        ZipArchiveBuilder zipArchiveBuilder = new ZipArchiveBuilder(zipPath, zipPath.getParent()); // tmp file in the same directory as zip file

//		TConfig config = TConfig.open();
//		// Set FsAccessOption.GROW for appending-to for better performance
//		config.setAccessPreference(FsAccessOption.GROW, true);

        zipArchiveBuilder.setCompressionMethod(CompressionMethod.STORED);

        List<DataSet> processes = new ArrayList<DataSet>();
        List<DataSet> prodflows = new ArrayList<DataSet>();
        List<DataSet> elemflows = new ArrayList<DataSet>();
        List<DataSet> flowproperties = new ArrayList<DataSet>();
        List<DataSet> unitgroups = new ArrayList<DataSet>();
        List<DataSet> sources = new ArrayList<DataSet>();
        List<DataSet> contacts = new ArrayList<DataSet>();
        List<DataSet> lciamethods = new ArrayList<DataSet>();
        List<DataSet> lifecyclemodels = new ArrayList<DataSet>();

        for (DataSet d : datasets) {
            if (d instanceof Process) {
                processes.add(d);
                if (dependencies) {
                    ProcessDao pDao = new ProcessDao();
                    Set<DataSet> deps = pDao.getDependencies(d, DependenciesMode.ALL);
                    for (DataSet dep : deps) {
                        putDatasetInList(dep, processes, prodflows, elemflows, flowproperties, unitgroups, sources,
                                contacts, lciamethods, lifecyclemodels);
                    }
                }
            } else
                putDatasetInList(d, processes, prodflows, elemflows, flowproperties, unitgroups, sources, contacts,
                        lciamethods, lifecyclemodels);
        }

        this.logger.info("exporting processes");
        this.exportDatasets(new ProcessDao(), removeDupes(processes), zipArchiveBuilder);

        this.logger.info("exporting flow props");
        this.exportDatasets(new FlowPropertyDao(), removeDupes(flowproperties), zipArchiveBuilder);

        this.logger.info("exporting flows");
        this.exportDatasets(new ProductFlowDao(), removeDupes(prodflows), zipArchiveBuilder);
        this.exportDatasets(new ElementaryFlowDao(), removeDupes(elemflows), zipArchiveBuilder);

        this.logger.info("exporting LCIA methods");
        this.exportDatasets(new LCIAMethodDao(), removeDupes(lciamethods), zipArchiveBuilder);

        this.logger.info("exporting sources");
        this.exportDatasets(new SourceDao(), removeDupes(sources), zipArchiveBuilder);

        this.logger.info("exporting contacts");
        this.exportDatasets(new ContactDao(), removeDupes(contacts), zipArchiveBuilder);

        this.logger.info("exporting unit groups");

        this.exportDatasets(new UnitGroupDao(), removeDupes(unitgroups), zipArchiveBuilder);

        this.logger.info("exporting lifecycle models");
        this.exportDatasets(new LifeCycleModelDao(), removeDupes(lifecyclemodels), zipArchiveBuilder);

        zipArchiveBuilder.close();
        return zipPath;
    }

    /**
     * export the data stock
     *
     * @param stock
     * @return Path of the zip archive on the file system
     * @throws IOException
     */
    protected Path doExport(IDataStockMetaData stock) throws IOException {

        Path zipPath = createZipFile(stock.getName(), false);

        ZipArchiveBuilder zipArchiveBuilder = new ZipArchiveBuilder(zipPath, zipPath.getParent()); // tmp file in the same directory as zip file
        zipArchiveBuilder.setCompressionMethod(CompressionMethod.STORED); // by default

        this.logger.trace("zipArchiveBuilder's stream is ready");
        this.logger.trace(zipPath.toAbsolutePath().toString());

        writeManifest(zipArchiveBuilder);

        long start = System.currentTimeMillis();

        long stockID = stock.getId();

        this.logger.info("exporting flow props");

        exportSQLAdapter.dumpXML(DataStockSQLMeta.FlowProperty, zipArchiveBuilder, exportMode, stockID);

        this.logger.info("exporting flows");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.FlowGeneric, zipArchiveBuilder, exportMode, stockID);

        this.logger.info("exporting processes");

        exportSQLAdapter.dumpXML(DataStockSQLMeta.Process, zipArchiveBuilder, exportMode, stockID);

        this.logger.info("exporting LCIA methods");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.LCIAMethod, zipArchiveBuilder, exportMode, stockID);

        this.logger.info("exporting sources");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.Source, zipArchiveBuilder, exportMode, stockID);

        this.logger.info("exporting contacts");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.Contact, zipArchiveBuilder, exportMode, stockID);

        this.logger.info("exporting unit groups");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.UnitGroup, zipArchiveBuilder, exportMode, stockID);

        this.logger.info("exporting lifecycle models");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.LifeCycleModel, zipArchiveBuilder, exportMode, stockID);

        this.logger.info("exporting external docs");

        Path datafiles = Paths.get(ConfigurationService.INSTANCE.getDigitalFileBasePath());

        // external docs are kept uncompressed by default
        exportSQLAdapter.dumpExternalDocs(datafiles, zipArchiveBuilder, stockID);

        long stop = System.currentTimeMillis();

        this.logger.info("done. export took " + (stop - start) / 1000 + " seconds");

        zipArchiveBuilder.close();

        if (stock != null)
            storeExportTag(ExportType.ZIP, this.exportMode, zipPath, stock);

        return zipPath;
    }

    /**
     * export entire database
     *
     * @return Path of the zip archive on the file system
     * @throws IOException
     */
    protected Path doExport() throws IOException {

        Path zipPath = createZipFile(null, false);

        ZipArchiveBuilder zipArchiveBuilder = new ZipArchiveBuilder(zipPath, zipPath.getParent()); // tmp file in the same directory as zip file
        zipArchiveBuilder.setCompressionMethod(CompressionMethod.STORED); // by default

        this.logger.trace("zipArchiveBuilder's stream is ready");
        this.logger.trace(zipPath.toAbsolutePath().toString());

        writeManifest(zipArchiveBuilder);

        long start = System.currentTimeMillis();

        this.logger.info("exporting flow props");

        exportSQLAdapter.dumpXML(DataStockSQLMeta.FlowProperty, zipArchiveBuilder, exportMode);

        this.logger.info("exporting flows");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.FlowGeneric, zipArchiveBuilder, exportMode);

        this.logger.info("exporting processes");

        exportSQLAdapter.dumpXML(DataStockSQLMeta.Process, zipArchiveBuilder, exportMode);

        this.logger.info("exporting LCIA methods");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.LCIAMethod, zipArchiveBuilder, exportMode);

        this.logger.info("exporting sources");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.Source, zipArchiveBuilder, exportMode);

        this.logger.info("exporting contacts");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.Contact, zipArchiveBuilder, exportMode);

        this.logger.info("exporting unit groups");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.UnitGroup, zipArchiveBuilder, exportMode);

        this.logger.info("exporting lifecycle models");
        exportSQLAdapter.dumpXML(DataStockSQLMeta.LifeCycleModel, zipArchiveBuilder, exportMode);

        this.logger.info("exporting external docs");

        Path datafiles = Paths.get(ConfigurationService.INSTANCE.getDigitalFileBasePath());

        // external docs are kept uncompressed by default
        exportSQLAdapter.dumpExternalDocs(datafiles, zipArchiveBuilder);

        long stop = System.currentTimeMillis();

        this.logger.info("done. export took " + (stop - start) / 1000 + " seconds");

        zipArchiveBuilder.close();

        if (stock != null)
            storeExportTag(ExportType.ZIP, this.exportMode, zipPath, stock);

        return zipPath;
    }

    /**
     * export the data stock
     *
     * @param stock
     * @return
     * @throws IOException
     */
    protected Path doExportCSV(IDataStockMetaData stock, ExportType type) throws IOException {

        Path csvPath = Paths.get(ConfigurationService.INSTANCE.getZipFileDirectory(),
                Long.toString(System.currentTimeMillis()) + stockNameSuffix(stock.getName()) + ".csv");

        long start = System.currentTimeMillis();

        this.logger.info("exporting processes");

        this.exportDatasetsCSV(new ProcessDao(), stock, type, csvPath);

        long stop = System.currentTimeMillis();

        this.logger.info("done. export took " + (stop - start) / 1000 + " seconds");

        storeExportTag(type, this.exportMode, csvPath, stock);

        return csvPath;
    }

    private <T extends DataSet> void exportDatasets(DataSetDao<T, ?, ?> dao, List<DataSet> dataSetList, ZipArchiveBuilder zipArchiveBuilder) {

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(dataSetList.size() + " datasets");
        }

        this.writeDatasets(dao, dataSetList, 1, dataSetList.size(), zipArchiveBuilder);

    }

    private void exportDatasetsCSV(ProcessDao pDao, IDataStockMetaData stock, ExportType type, Path csv) {

        long dataSetCount;

        if (stock != null) {
            dataSetCount = pDao.getCount(new IDataStockMetaData[]{stock}, null, true);
        } else {
            dataSetCount = pDao.getAllCount();
        }

        long pages = calculatePages(dataSetCount);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(dataSetCount + " datasets, pagesize: " + pageSize + ", " + pages + " pages");
        }

        List<Process> dataSetList;

        try {
            BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(csv.toFile()), "ISO8859_1"));

            CSVFormatter f = null;

            if (type.equals(ExportType.CSV_EPD))
                f = new CSVFormatter(DecimalSeparator.DOT);
            else if (type.equals(ExportType.CSV_EPD_C))
                f = new CSVFormatter(DecimalSeparator.COMMA);

            f.writeHeader(w);

            for (int currentPage = 0; currentPage < pages; currentPage++) {
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("exporting page " + (currentPage + 1) + " of " + pages);
                }

                if (stock != null) {
                    dataSetList = (List<Process>) pDao.getDataSets(new IDataStockMetaData[]{stock}, null,
                            !this.exportMode.equals(ExportMode.ALL), (currentPage * pageSize), pageSize);
                } else {
                    dataSetList = (List<Process>) pDao.get((currentPage * pageSize), pageSize);
                }

                logger.trace("exporting " + dataSetList.size() + " datasets");

                Map<String, ProductFlow> flowProperties = new HashMap<String, ProductFlow>();
                for (Process process : dataSetList) {
                    String uuid = null;
                    try {
                        uuid = process.getReferenceExchanges().get(0).getReference().getRefObjectId();
                    } catch (Exception e) {
                        continue;
                    }

                    ProductFlowDao pfdao = new ProductFlowDao();
                    ProductFlow productFlow = pfdao.getByUuid(uuid);
                    flowProperties.put(process.getUuid().getUuid(), productFlow);
                }

                f.formatCSV(dataSetList, flowProperties, w);
            }

            w.close();
        } catch (IOException e) {
            this.logger.error("Error exporting CSV", e);
        }
    }

    private <T extends DataSet> void writeDatasets(DataSetDao<T, ?, ?> dao, List<DataSet> dataSetList, int page,
                                                   int pageSize, ZipArchiveBuilder zipArchiveBuilder) {

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("  exporting datasets " + (page * pageSize) + " through "
                    + ((page * pageSize) + dataSetList.size() - 1));
        }

        if (dataSetList.equals(Collections.EMPTY_LIST)) {
            this.logger.trace("(no datasets found)");
            return;
        }

        for (DataSet dataset : dataSetList) {

            if ((this.exportMode.equals(ExportMode.LATEST_ONLY_GLOBAL)
                    || this.exportMode.equals(ExportMode.LATEST_ONLY)) && !dataset.isMostRecentVersion()) {
                // do not skip stuff
//				if (!(this.exportMode.equals(ExportMode.LATEST_ONLY) && !dataset.getDataSetType().equals(DataSetType.PROCESS))) {
//					logger.trace("dataset " +  dataset.toString() + " has a newer version, skipping");
//					continue;
//				}
            }
            if (this.logger.isTraceEnabled())
                this.logger.trace("writing dataset " + dataset.getUuid());

            dataset.writeInZip(zipArchiveBuilder, this.exportMode);
        }

    }

    /**
     * A helper method first used in the ProcessHandler.java to export a
     * single process with all its dependencies.
     * <p>
     * The process itself is also included.
     *
     * @param process The process you want to export all of it's dependencies.
     * @param zip     A pre-initialized ZipArchiveBuilder. This method will
     *                <b>NOT</b> flush or close the archive.
     * @see ProcessResource#getDependencies(String, String)
     */

    public void writeDependencies(Process process, ZipArchiveBuilder zip) {
        Set<DataSet> deps = DependenciesUtil.getDependencyCluster(process, DependenciesMode.ALL);

        for (DataSet dataset : deps)
            dataset.writeInZip(zip, ExportMode.ALL);
    }

    public void writeDependencies(LifeCycleModel lcm, ZipArchiveBuilder zip) {
        boolean ignoreProcess = ConfigurationService.INSTANCE.isDependenciesIgnoreProcesses();

        Set<DataSet> deps = DependenciesUtil.getDependencyCluster(lcm, DependenciesMode.ALL);

        for (DataSet dataset : deps)
            dataset.writeInZip(zip, ExportMode.ALL);

        lcm.writeInZip(zip, ExportMode.ALL);
    }

    /**
     * create a new ZIP file in the configured directory
     *
     * @param temporary if true, the file extension will be "tmp.zip"
     * @return
     */
    protected Path createZipFile(String stockName, boolean temporary) {
        return Paths.get(ConfigurationService.INSTANCE.getZipFileDirectory())
                .resolve(Long.toString(System.currentTimeMillis()) + (stockName != null ? stockNameSuffix(stockName) : "") + (temporary ? ".tmp" : "") + ".zip");
    }

    protected String stockNameSuffix(String stockName) {
        return "_" + stockName.replaceAll("[^A-Za-z0-9_\\-]", "");
    }

    /**
     * determine in how many pages an export operation will be divided
     *
     * @param dataSetCount
     * @return
     */
    private long calculatePages(long dataSetCount) {
        long pages = dataSetCount / pageSize;
        long remainder = dataSetCount % pageSize;
        if (remainder > 0) {
            pages++;
        }
        return pages;
    }

    /**
     * remove duplicates
     *
     * @param dataSetList
     * @return
     */
    private List<DataSet> removeDupes(List<DataSet> dataSetList) {
        return removeDupes(dataSetList, false);
    }

    /**
     * remove duplicates and optionally sort by version
     *
     * @param dataSetList
     * @return
     */
    private List<DataSet> removeDupes(List<DataSet> dataSetList, boolean sortByVersion) {
        List<DataSet> dataSetListNoDupes = new ArrayList<DataSet>(new HashSet<DataSet>(dataSetList));
        this.logger.trace("removing dupes, going from " + dataSetList.size() + " to " + dataSetListNoDupes.size());
        if (sortByVersion)
            sortByVersion(dataSetListNoDupes);
        return dataSetListNoDupes;
    }

    /**
     * sort by version
     */
    private void sortByVersion(List<DataSet> dataSetList) {
        dataSetList.sort(new DataSetUUIDVersionComparator<DataSet>());
    }

    /**
     * write manifest file to a ZIP file that includes the format and soda4LCA
     * versions
     *
     * @param zipArchiveBuilder
     * @throws IOException
     */
    protected void writeManifest(ZipArchiveBuilder zipArchiveBuilder) throws IOException {
        ILCDManifest manifest = new ILCDManifest(Constants.FORMAT_VERSION,
                "soda4LCA " + ConfigurationService.INSTANCE.getVersionTag());

        zipArchiveBuilder.add(manifest.toString(), "META-INF/MANIFEST.MF");
    }

    /**
     * store information about the cached export file along with the data stock
     *
     * @param path
     * @param stock
     */
    private ExportTag storeExportTag(ExportType type, ExportMode mode, Path path, IDataStockMetaData stock) {
        CommonDataStockDao dao = new CommonDataStockDao();
        AbstractDataStock actualStock = dao.getById(stock.getId());
        ExportTag exportTag = actualStock.getExportTag(type, mode);
        exportTag.update(path.toString());
        try {
            dao.merge(actualStock);
        } catch (MergeException e) {
            this.logger.error("error updating export tag", e);
            return null;
        }
        return exportTag;
    }

    public String getZipFileName() {
        return getFileName() + ".zip";
    }

    public String getCSVFileName() {
        return getFileName() + ".csv";
    }

    private String getFileName() {
        if (this.stock == null) {
            this.logger.debug("no datastock selected, exporting entire database");
            return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + timeStampSuffix() + ".zip";
        } else {
            this.logger.info("starting export of datastock " + this.stock.getName());
            return this.stock.getName() + timeStampSuffix();
        }
    }

    public StreamedContent getFile() {

        Path zipPath;

        try {
            zipPath = this.export(this.stock, ExportType.ZIP);

            if (zipPath != null) {
                FileInputStream stream = new FileInputStream(zipPath.toFile());
                this.file = DefaultStreamedContent.builder()
                        .contentType("application/zip")
                        .name(this.getZipFileName())
                        .stream(() -> {
                            try {
                                return new FileInputStream(zipPath.toFile());
                            } catch (FileNotFoundException e) {
                                logger.error("Error exporting ZIP", e);
                            }
                            return null;
                        }).build();
            }
        } catch (IOException e) {
            logger.error("Error exporting ZIP", e);
        }

        return this.file;
    }

    public StreamedContent getCSVFile(ExportType type) {

        Path csvPath;
        try {
            csvPath = this.export(this.stock, type);

            if (csvPath != null) {
                this.file = DefaultStreamedContent.builder()
                        .contentType("text/csv")
                        .name(this.getCSVFileName())
                        .stream(() -> {
                            try {
                                return new FileInputStream(csvPath.toFile());
                            } catch (FileNotFoundException e) {
                                logger.error("Error exporting CSV", e);
                            }
                            return null;
                        }).build();
            }
        } catch (IOException e) {
            logger.error("Error exporting CSV", e);
        }

        return this.file;
    }

    public IDataStockMetaData getStock() {
        return this.stock;
    }

    public void setStock(IDataStockMetaData stock) {
        this.stock = stock;
    }

    public ExportMode getExportMode() {
        return this.exportMode;
    }

    public void setExportMode(ExportMode exportMode) {
        this.exportMode = exportMode;
    }

    public DataStockResource getDataStockResource() {
        return dataStockResource;
    }

    public void setDataStockResource(DataStockResource dataStockResource) {
        this.dataStockResource = dataStockResource;
    }

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    public boolean isDependencies() {
        return dependencies;
    }

    public void setDependencies(boolean dependencies) {
        this.dependencies = dependencies;
    }

    public ExportMode[] getExportModes() {
        return ExportMode.values();
    }
}