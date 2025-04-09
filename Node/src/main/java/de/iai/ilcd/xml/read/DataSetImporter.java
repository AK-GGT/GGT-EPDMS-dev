package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.api.app.lciamethod.LCIAMethodDataSet;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.dao.AbstractDigitalFileProvider.FileSystemDigitalFileProvider;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.tag.Tag;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.xml.zip.exceptions.ZipException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Importer for data sets
 */
public class DataSetImporter {

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(DataSetImporter.class);

    private Set<Long> processesThatRequireTaggingIds = new HashSet<>();

    private List<Tag> importTags;

    public DataSetImporter() {
        super();
        if (ConfigurationService.INSTANCE.isTagOnImport())
            importTags = new TagDao().getPersistedImportTags();
    }

    /**
     * Import a ILCD ZIP file
     *
     * @param zipFileName name of the zip file
     * @param out         output writer
     * @param rds         root data stock
     */
    public void importZipFile(String zipFileName, PrintWriter out, RootDataStock rds) throws IOException, ZipException {
        importZipFile(zipFileName, out, rds, new HashSet<>());
    }

    /**
     * Import a ILCD ZIP file
     *
     * @param zipFileName name of the zip file
     * @param out         output writer
     * @param rds         root data stock
     * @param tags        data sets will be tagged with these
     */
    public void importZipFile(String zipFileName, PrintWriter out, RootDataStock rds, Set<Tag> tags) throws IOException, ZipException {
        DataSetZipImporter zipImporter = new DataSetZipImporter(this);
        zipImporter.importZipFile(zipFileName, out, rds);
        tagimportedDataSets(tags);
    }

    /**
     * Import files from base directory
     *
     * @param baseDirectory the base directory
     * @param out           output writer
     * @param rds           root data stock
     */
    public void importFiles(String baseDirectory, PrintWriter out, RootDataStock rds) {
        importFiles(baseDirectory, out, rds, new HashSet<>());
    }

    /**
     * Import files from base directory
     *
     * @param baseDirectory the base directory
     * @param out           output writer
     * @param rds           root data stock
     * @param tags          data sets will be tagged with these
     */
    public void importFiles(String baseDirectory, PrintWriter out, RootDataStock rds, Set<Tag> tags) {
        if (!baseDirectory.endsWith("/")) {
            baseDirectory = baseDirectory + "/";
        }

        ContactReader contactReader = new ContactReader();
        DaoProvider<Contact, ContactDao> contactDaoProvider = new DaoProvider<>(new ContactDao());
        importFiles(baseDirectory + DatasetTypes.CONTACTS.getValue(), contactReader, contactDaoProvider, out, rds);

        SourceReader sourceReader = new SourceReader();
        SourceDao dao = new SourceDao();
        importSourceFiles(baseDirectory + DatasetTypes.SOURCES.getValue(), sourceReader, dao, out, rds);

        UnitGroupReader unitGroupReader = new UnitGroupReader();
        DaoProvider<UnitGroup, UnitGroupDao> unitGroupDaoProvider = new DaoProvider<>(new UnitGroupDao());
        importFiles(baseDirectory + DatasetTypes.UNITGROUPS.getValue(), unitGroupReader, unitGroupDaoProvider, out, rds);

        FlowPropertyReader flowPropertyReader = new FlowPropertyReader();
        DaoProvider<FlowProperty, FlowPropertyDao> flowpropDaoProvider = new DaoProvider<>(new FlowPropertyDao());
        importFiles(baseDirectory + DatasetTypes.FLOWPROPERTIES.getValue(), flowPropertyReader, flowpropDaoProvider, out, rds);

        FlowReader flowReader = new FlowReader();
        FlowDaoProvider<Flow> flowDaoProvider = new FlowDaoProvider<>();
        importFiles(baseDirectory + DatasetTypes.FLOWS.getValue(), flowReader, flowDaoProvider, out, rds);

        ProcessReader processReader = new ProcessReader();
        DaoProvider<Process, ProcessDao> processDaoProvider = new DaoProvider<>(new ProcessDao());
        importFiles(baseDirectory + DatasetTypes.PROCESSES.getValue(), processReader, processDaoProvider, out, rds);

        LCIAMethodReader lciaMethodReader = new LCIAMethodReader();
        DaoProvider<LCIAMethod, LCIAMethodDao> lciaMethodDaoProvider = new DaoProvider<>(new LCIAMethodDao());
        importFiles(baseDirectory + DatasetTypes.LCIAMETHODS.getValue(), lciaMethodReader, lciaMethodDaoProvider, out, rds);

        LifeCycleModelReader lifeCycleModelReader = new LifeCycleModelReader();
        DaoProvider<LifeCycleModel, LifeCycleModelDao> lifeCycleModelDaoProvider = new DaoProvider<>(new LifeCycleModelDao());
        importFiles(baseDirectory + DatasetTypes.LIFECYCLEMODELS.getValue(), lifeCycleModelReader, lifeCycleModelDaoProvider, out, rds);

        tagimportedDataSets(tags);
    }

    /**
     * Import a data set
     *
     * @param type              type of data set
     * @param desc              description
     * @param datasetStream     stream with data set
     * @param out               output writer
     * @param rds               root data stock
     * @param digitFileProvider provider for digital files, may be <code>null</code> for all non-{@link Source} data sets
     * @return created data set or <code>null</code> on failure
     */
    public DataSet importDataSet(DataSetType type, String desc, InputStream datasetStream, PrintWriter out, RootDataStock rds, AbstractDigitalFileProvider digitFileProvider) {
        return importDataSet(type, desc, datasetStream, out, rds, digitFileProvider, new HashSet<>());
    }

    /**
     * Import a data set
     *
     * @param type              type of data set
     * @param desc              description
     * @param datasetStream     stream with data set
     * @param out               output writer
     * @param rds               root data stock
     * @param digitFileProvider provider for digital files, may be <code>null</code> for all non-{@link Source} data sets
     * @param tags              data set will be tagged with these
     * @return created data set or <code>null</code> on failure
     */
    public DataSet importDataSet(DataSetType type, String desc, InputStream datasetStream, PrintWriter out, RootDataStock rds, AbstractDigitalFileProvider digitFileProvider, Set<Tag> tags) {
        DataSet result = null;

        switch (type) {
            case PROCESS:
                result = this.importProcessDataSet(desc, datasetStream, out, rds);
                break;
            case FLOW:
                result = this.importFlowDataSet(desc, datasetStream, out, rds);
                break;
            case FLOWPROPERTY:
                result = this.importFlowPropertyDataSet(desc, datasetStream, out, rds);
                break;
            case UNITGROUP:
                result = this.importUnitGroupDataSet(desc, datasetStream, out, rds);
                break;
            case SOURCE:
                result = this.importSourceDataSet(desc, datasetStream, digitFileProvider, out, rds);
                break;
            case CONTACT:
                result = this.importContactDataSet(desc, datasetStream, out, rds);
                break;
            case LCIAMETHOD:
                result = this.importLCIAMethodDataSet(desc, datasetStream, out, rds);
                break;
            case LIFECYCLEMODEL:
                result = this.importLifeCycleModelDataSet(desc, datasetStream, out, rds);
                break;
            default:
                break;
        }

        tagimportedDataSets(tags);
        return result;
    }

    private DataSet importLifeCycleModelDataSet(String desc, InputStream datasetStream, PrintWriter out,
                                                RootDataStock rds) {
        LifeCycleModelReader lciaMethodReader = new LifeCycleModelReader();
        DaoProvider<LifeCycleModel, LifeCycleModelDao> lciaMethodDaoProvider = new DaoProvider<>(new LifeCycleModelDao());
        return this.importDataSet(desc, datasetStream, lciaMethodReader, lciaMethodDaoProvider, out, rds);
    }

    /**
     * Import a {@link LCIAMethodDataSet}
     *
     * @param desc          description (mainly for logging)
     * @param datasetStream data set stream
     * @param out           output writer
     * @param rds           root data stock
     * @return created data set
     */
    private LCIAMethod importLCIAMethodDataSet(String desc, InputStream datasetStream, PrintWriter out, RootDataStock rds) {
        LCIAMethodReader lciaMethodReader = new LCIAMethodReader();
        DaoProvider<LCIAMethod, LCIAMethodDao> lciaMethodDaoProvider = new DaoProvider<LCIAMethod, LCIAMethodDao>(new LCIAMethodDao());
        return this.importDataSet(desc, datasetStream, lciaMethodReader, lciaMethodDaoProvider, out, rds);
    }

    /**
     * Import a {@link Process}
     *
     * @param desc          description (mainly for logging)
     * @param datasetStream data set stream
     * @param out           output writer
     * @param rds           root data stock
     * @return created data set
     */
    private Process importProcessDataSet(String desc, InputStream datasetStream, PrintWriter out, RootDataStock rds) {
        ProcessReader processReader = new ProcessReader();
        DaoProvider<Process, ProcessDao> processDaoProvider = new DaoProvider<Process, ProcessDao>(new ProcessDao());
        return this.importDataSet(desc, datasetStream, processReader, processDaoProvider, out, rds);
    }

    /**
     * Import a {@link Flow}
     *
     * @param desc          description (mainly for logging)
     * @param datasetStream data set stream
     * @param out           output writer
     * @param rds           root data stock
     * @return created data set
     */
    private Flow importFlowDataSet(String desc, InputStream datasetStream, PrintWriter out, RootDataStock rds) {
        FlowReader flowReader = new FlowReader();
        FlowDaoProvider<Flow> flowDaoProvider = new FlowDaoProvider<Flow>();
        return this.importDataSet(desc, datasetStream, flowReader, flowDaoProvider, out, rds);
    }

    /**
     * Import a {@link FlowProperty}
     *
     * @param desc          description (mainly for logging)
     * @param datasetStream data set stream
     * @param out           output writer
     * @param rds           root data stock
     * @return created data set
     */
    private FlowProperty importFlowPropertyDataSet(String desc, InputStream datasetStream, PrintWriter out, RootDataStock rds) {
        FlowPropertyReader flowpropReader = new FlowPropertyReader();
        DaoProvider<FlowProperty, FlowPropertyDao> flowpropDaoProvider = new DaoProvider<FlowProperty, FlowPropertyDao>(new FlowPropertyDao());
        return this.importDataSet(desc, datasetStream, flowpropReader, flowpropDaoProvider, out, rds);
    }

    /**
     * Import a {@link UnitGroup}
     *
     * @param desc          description (mainly for logging)
     * @param datasetStream data set stream
     * @param out           output writer
     * @param rds           root data stock
     * @return created data set
     */
    private UnitGroup importUnitGroupDataSet(String desc, InputStream datasetStream, PrintWriter out, RootDataStock rds) {
        UnitGroupReader unitGroupReader = new UnitGroupReader();
        DaoProvider<UnitGroup, UnitGroupDao> unitGroupDaoProvider = new DaoProvider<>(new UnitGroupDao());
        return this.importDataSet(desc, datasetStream, unitGroupReader, unitGroupDaoProvider, out, rds);
    }

    /**
     * Import a {@link Contact}
     *
     * @param desc          description (mainly for logging)
     * @param datasetStream data set stream
     * @param out           output writer
     * @param rds           root data stock
     * @return created data set
     */
    private Contact importContactDataSet(String desc, InputStream datasetStream, PrintWriter out, RootDataStock rds) {
        ContactReader contactReader = new ContactReader();
        DaoProvider<Contact, ContactDao> contactDaoProvider = new DaoProvider<Contact, ContactDao>(new ContactDao());
        return this.importDataSet(desc, datasetStream, contactReader, contactDaoProvider, out, rds);
    }

    /**
     * Import a source file
     *
     * @param fileName     name of the file
     * @param sourceReader reader for sources
     * @param dao          DAO to use
     * @param out          output writer
     * @param rds          root data stock
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private boolean importSourceFile(String fileName, SourceReader sourceReader, SourceDao dao, PrintWriter out, RootDataStock rds) {
        try {
            final java.io.FileInputStream sourceStream = new java.io.FileInputStream(fileName);
            final AbstractDigitalFileProvider fsProvider = new FileSystemDigitalFileProvider(Paths.get(fileName));

            return this.importSourceDataSet(fileName, sourceStream, fsProvider, sourceReader, dao, out, rds) != null;
        } catch (IOException ex) {
            logger.error("error importing source", ex);
            return false;
        }
    }

    /**
     * Import source data set
     *
     * @param desc              description (mainly for logging)
     * @param datasetStream     stream with data set
     * @param digitFileProvider provider for digital files
     * @param out               output writer
     * @param rds               root data stock
     * @return created data set
     */
    private Source importSourceDataSet(String desc, InputStream datasetStream, AbstractDigitalFileProvider digitFileProvider, PrintWriter out, RootDataStock rds) {
        SourceReader sourceReader = new SourceReader();
        final SourceDao dao = new SourceDao();

        return this.importSourceDataSet(desc, datasetStream, digitFileProvider, sourceReader, dao, out, rds);
    }

    /**
     * Import source files
     *
     * @param directoryPath path to directory
     * @param reader        reader for source data sets
     * @param dao           source DAO
     * @param out           output writer
     * @param rds           root data stock
     */
    private void importSourceFiles(String directoryPath, SourceReader reader, SourceDao dao, PrintWriter out, RootDataStock rds) {
        try {
            File dir = new File(directoryPath);
            if (!dir.isDirectory()) {
                return; // no files available of this type
            }
            Collection<File> files = FileUtils.listFiles(dir, null, false);
            if (CollectionUtils.isEmpty(files)) {
                return; // no files available of this type
            }
            logger.info("count of files for import: ", files.size());
            if (out != null) {
                out.println("Number of files to import: " + files.size());
                out.flush();
            }
            logger.debug("importing from " + directoryPath);

            Iterator<File> fileIter = files.iterator();
            while (fileIter.hasNext()) {
                File file = fileIter.next();
                String filename = file.getAbsolutePath();
                logger.trace("Import file: {}", filename);
                if (out != null) {
                    out.println("Import file: " + file.getName());
                    out.flush();
                }
                boolean persisted = this.importSourceFile(filename, reader, dao, out, rds);
                String message = "";
                if (persisted) {
                    message = " successfully imported.";
                } else {
                    message = " FILE WAS NOT IMPORTED, see messages above";
                }
                if (out != null) {
                    out.println(message);
                    out.flush();
                }
            }
        } catch (Exception e) {
            logger.error("Exception while importing files from directory {}", directoryPath);
            logger.error("Exception is: ", e);
            if (out != null) {
                out.println("Cannot import all files");
                out.flush();
            }
        }
    }

    /**
     * Import files
     *
     * @param <T>           type of data sets
     * @param directoryPath path to directory
     * @param reader        reader for data sets
     * @param daoProvider   provider for DAOs
     * @param out           output writer
     * @param rds           root data stock
     */
    private <T extends DataSet> void importFiles(String directoryPath, DataSetReader<T> reader, DaoProvider<T, ? extends DataSetDao<T, ?, ?>> daoProvider, PrintWriter out, RootDataStock rds) {
        Collection<File> files = null;

        try {
            File dir = new File(directoryPath);
            if (!dir.isDirectory()) {
                logger.info("not a directory: " + dir.getAbsolutePath());
                return; // no files available of this type
            }
            files = FileUtils.listFiles(dir, null, false);
            if (!(files.size() > 0)) {
                logger.info("no files found in " + dir.getAbsolutePath());
                return; // no files available of this type
            }
            logger.info("count of files for import: ", files.size());
            if (out != null) {
                out.println("Number of files to import: " + files.size());
                out.flush();
            }
            logger.debug("importing from " + directoryPath);

            Iterator<File> fileIter = files.iterator();
            while (fileIter.hasNext()) {
                File file = fileIter.next();
                String filename = file.getAbsolutePath();
                logger.trace("Import file: {}", filename);
                if (out != null) {
                    out.println("Import file: " + file.getName());
                    out.flush();
                }
                boolean persisted = this.importFile(filename, reader, daoProvider, out, rds);
                String message = "";
                if (persisted) {
                    message = " successfully imported.";
                } else {
                    message = " FILE WAS NOT IMPORTED, see messages above";
                }
                if (out != null) {
                    out.println(message);
                    out.flush();
                }
            }
        } catch (Exception e) {
            logger.error("Exception while importing files from directory {}", directoryPath);
            logger.error("Exception is: ", e);
            if (out != null) {
                out.println("Cannot import all files");
                out.flush();
            }
        }
    }

    /**
     * Import a file
     *
     * @param <T>         type of data set
     * @param fileName    name of file
     * @param reader      reader to use
     * @param daoProvider DAO provider to use
     * @param out         output writer
     * @param rds         root data stock
     * @return created data set
     */
    private <T extends DataSet> boolean importFile(String fileName, DataSetReader<T> reader, DaoProvider<T, ? extends DataSetDao<T, ?, ?>> daoProvider, PrintWriter out, RootDataStock rds) {
        try {
            T foo = this.importDataSet(fileName.substring(fileName.lastIndexOf(File.separator), fileName.length() - 1), new FileInputStream(fileName), reader, daoProvider, out, rds);
            return foo != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Import a source data set
     *
     * @param desc              description (mainly for logging)
     * @param datasetStream     stream with data set content
     * @param digitFileProvider provider for digital files
     * @param reader            reader for source data set
     * @param dao               source dao to use
     * @param out               output writer
     * @param rds               root data stock
     * @return created data set
     */
    private Source importSourceDataSet(String desc, InputStream datasetStream, AbstractDigitalFileProvider digitFileProvider, SourceReader reader, SourceDao dao, PrintWriter out, RootDataStock rds) {
        if (rds == null) {
            out.println("Cannot create source data set without a root data stock");
            return null;
        }
        boolean persisted = false;

        Source dataset;
        try {
            dataset = reader.readDataSetFromStream(datasetStream, out);
            dataset.setImportDate(new Date());
            dataset.setRootDataStock(rds);
        } catch (Exception ex) {
            if (out != null) {
                out.println("Warning: Cannot import data set " + desc);
                out.flush();
            }
            logger.error("Cannot read data set {}", desc);
            logger.error("Exception is: ", ex);
            return null;
        }

        persisted = dao.checkAndPersist(dataset, PersistType.ONLYNEW, digitFileProvider, out);
        if (out != null) {
            out.flush();
        }

        return persisted ? dataset : null;
    }

    /**
     * Import a data set
     *
     * @param <T>           type of data set
     * @param desc          description (mainly for logging)
     * @param datasetStream stream with data set content
     * @param reader        reader for data set
     * @param daoProvider   dao provider
     * @param out           output writer
     * @param rds           root data stock
     * @return created data set
     */
    private <T extends DataSet> T importDataSet(String desc, InputStream datasetStream, DataSetReader<T> reader,
                                                DaoProvider<T, ? extends DataSetDao<T, ?, ?>> daoProvider,
                                                PrintWriter out, RootDataStock rds) {
        if (rds == null) {
            out.println("Cannot create data set without a root data stock");
            return null;
        }
        boolean persisted = false;

        T dataset;
        try {
            dataset = reader.readDataSetFromStream(datasetStream, out);
            dataset.setImportDate(new Date());
            dataset.setRootDataStock(rds);
        } catch (Exception ex) {
            if (out != null) {
                out.println("Warning: Cannot import data set " + desc);
                out.flush();
            }
            logger.error("Cannot read data set {}", desc);
            logger.error("Exception is: ", ex);
            return null;
        }

        DataSetDao<T, ?, ?> dao = daoProvider.getDao(dataset);
        persisted = dao.checkAndPersist(dataset, PersistType.ONLYNEW, out);
        dataset = dao.getByUuidAndVersion(dataset.getUuidAsString(), dataset.getDataSetVersion());

        boolean isTagProcesses = ConfigurationService.INSTANCE.isTagOnImport()
                && (persisted
                || ConfigurationService.INSTANCE.isTagExistingOnImport());
        if (dataset instanceof Process && isTagProcesses) {
            processesThatRequireTaggingIds.add(dataset.getId());
        }

        if (out != null) {
            out.flush();
        }

        return persisted ? dataset : null;
    }

    private void tagimportedDataSets(Set<Tag> tags) {

        if (processesThatRequireTaggingIds.size() > 0) {
            if (importTags != null)
                tags.addAll(importTags);
            new TagDao().batchTagProcessesByProcessIds(processesThatRequireTaggingIds, tags);
            processesThatRequireTaggingIds.clear();
        }
    }

    /**
     * Provider for DAO object. Required due to {@link ElementaryFlow} / {@link ProductFlow}.
     *
     * @param <T> type of data set
     * @param <D> type of DAO
     */
    public static class DaoProvider<T extends DataSet, D extends DataSetDao<T, ?, ?>> {

        /**
         * default DAO object
         */
        private final D dao;

        /**
         * Create provider
         *
         * @param dao default DAO
         */
        public DaoProvider(D dao) {
            this.dao = dao;
        }

        /**
         * Get the DAO object
         *
         * @param subject subject to DAO
         * @return DAO object
         */
        public D getDao(T subject) {
            return this.dao;
        }
    }

    /**
     * Provider for flow DAO
     *
     * @param <T> type of flow
     */
    public static class FlowDaoProvider<T extends Flow> extends DaoProvider<T, FlowDao<T>> {

        /**
         * DAO for {@link ProductFlow}
         */
        private final ProductFlowDao daoProd = new ProductFlowDao();

        /**
         * Create provider
         */
        @SuppressWarnings("unchecked")
        public FlowDaoProvider() {
            super((FlowDao<T>) new ElementaryFlowDao());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public FlowDao<T> getDao(T subject) {
            if (subject instanceof ElementaryFlow) {
                return super.getDao(subject);
            } else {
                return (FlowDao<T>) this.daoProd;
            }
        }
    }
}
