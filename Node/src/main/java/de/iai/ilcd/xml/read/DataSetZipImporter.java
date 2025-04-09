package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.AbstractDigitalFileProvider;
import de.iai.ilcd.model.dao.AbstractDigitalFileProvider.FileSystemDigitalFileProvider;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.xml.zip.exceptions.ZipException;
import de.iai.ilcd.xml.zip.exceptions.ZipInvalidException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Importer for ILCD zip files
 */
public class DataSetZipImporter {

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(DataSetZipImporter.class);

    private static final int ZIP_ARCHIVE_CRAWLING_DEPTH = 4;

    /**
     * Data set importer
     */
    private final DataSetImporter dsImporter;

    private Path dataDir;

    /**
     * Create the ZIP importer for ILCD zip files
     *
     * @param dsImporter data set importer
     */
    public DataSetZipImporter(DataSetImporter dsImporter) {
        super();
        if (dsImporter == null) {
            throw new IllegalArgumentException("Data set importer must not be null!");
        }
        this.dsImporter = dsImporter;
    }

    /**
     * Import a ZIP file
     *
     * @param zipPath name of the zip file
     * @param out     output writer
     * @param rds     root data stock
     */
    public void importZipFile(String zipPath, PrintWriter out, RootDataStock rds) throws IOException, ZipException {
        trace("Importing ZIP archive ", zipPath);
        Path path = Paths.get(zipPath);
        trace("Resolved path to ", path);

        try (FileSystem zipFS = FileSystems.newFileSystem(path, (ClassLoader) null)) {
            // Find the ILCD Data directory
            this.dataDir = findILCDDataDirectory(zipFS);
            if (dataDir == null) {
                ZipException ze = new ZipInvalidException("Zip contained no valid ilcd data directory. (Depth = "
                        + ZIP_ARCHIVE_CRAWLING_DEPTH + ")");

                error(out, ze, "No ILCD data directory found in ", path);
                flush(out);
                throw ze;
            }

            // Importing data
            debug(out, "Importing data from directory ", dataDir);
            println(out, System.getProperty("line.separator")); // cosmetics

            // contacts first
            this.processDataSetDirectory(DataSetType.CONTACT, rds, out, null);

            // then sources with a digital file provider to find external docs
            if (logger.isDebugEnabled()) {
                logger.debug("data dir   : {}", dataDir);
                logger.debug("resolved to: {}", dataDir.resolve(DataSetType.SOURCE.getStandardFolderName()).toString());
            }
            FileSystemDigitalFileProvider digitFileProvider = new FileSystemDigitalFileProvider(dataDir.resolve(DataSetType.SOURCE.getStandardFolderName()));
            this.processDataSetDirectory(DataSetType.SOURCE, rds, out, digitFileProvider);

            // and the remaining types in specific order
            this.processDataSetDirectory(DataSetType.UNITGROUP, rds, out, null);
            this.processDataSetDirectory(DataSetType.FLOWPROPERTY, rds, out, null);
            this.processDataSetDirectory(DataSetType.FLOW, rds, out, null);
            this.processDataSetDirectory(DataSetType.LCIAMETHOD, rds, out, null);
            this.processDataSetDirectory(DataSetType.PROCESS, rds, out, null);
            this.processDataSetDirectory(DataSetType.LIFECYCLEMODEL, rds, out, null);

        } catch (IOException ioe) {
            error(out, ioe, "Import was incomplete due to an error.");
            ioe.printStackTrace();
            flush(out);
            throw ioe;
        }

        flush(out);
    }

    /**
     * Process a data set directory
     *
     * @param type              data set type
     * @param rds               root data stock
     * @param out               output writer
     * @param digitFileProvider digital file provider (may be <code>null</code> for non-{@link Source} data sets)
     */
    private void processDataSetDirectory(DataSetType type, RootDataStock rds, PrintWriter out,
                                         AbstractDigitalFileProvider digitFileProvider) {
        println(out, stringConcat(type.getValue(), "s").toUpperCase(Locale.ROOT)); // cosmetics

        String typeString = type.getValue();
        Path dataTypeDir = dataDir.resolve(type.getStandardFolderName());

        // nonsense handling first
        if (!Files.exists(dataTypeDir) || !Files.isDirectory(dataTypeDir)) {
            String cause;
            if (!Files.exists(dataTypeDir))
                cause = "doesn't exist";
            else
                cause = "is not a directory";
            trace(out, "No ", typeString, "s found. (", dataTypeDir, " ", cause, ".)");
        } else {
            // import the data sets
            this.importFilesOfSpecificType(dataTypeDir, type, out, rds, digitFileProvider);
        }

        println(out, System.getProperty("line.separator")); // cosmetics
        flush(out);
    }

    /**
     * Import all files within the given directory
     *
     * @param dir               data set directory path inside ZIP
     * @param type              data set type
     * @param rds               root data stock
     * @param out               output writer
     * @param digitFileProvider digital file provider (may be <code>null</code> for non-{@link Source} data sets)
     */
    private void importFilesOfSpecificType(Path dir, DataSetType type, PrintWriter out, RootDataStock rds, AbstractDigitalFileProvider digitFileProvider) {
        trace("Importing from ", dir);

        long filesTotal = 0;
        int filesImported = 0;

        if (out != null || logger.isTraceEnabled()) {
            // determine the total count
            try (Stream<Path> content = Files.list(dir)) {
                filesTotal = content.filter(Files::isRegularFile).count();

                if (filesTotal < 1)
                    trace(out, "Directory ", dir, " is empty.");

            } catch (Exception e) {
                error(out, e);
            }
        }

        // import files
        if (filesTotal > 0) {
            try (DirectoryStream<Path> content = Files.newDirectoryStream(dir)) {
                int i = 1;
                for (Path p : content) {
                    if (Files.isRegularFile(p)) {

                        // import
                        boolean persisted =
                                this.dsImporter.importDataSet(type, p.getFileName().toString(), Files.newInputStream(p), out, rds, digitFileProvider) != null;

                        // logging
                        if (persisted) {
                            trace(out, "Imported file (", i, "/", filesTotal, "): ", p.getFileName());
                            filesImported++;
                        } else {
                            trace(out, "FILE WAS NOT IMPORTED: ", p.getFileName());
                        }
                        flush(out);
                    }
                    i++;
                }

            } catch (Exception e) {
                error(out, e, "An error occured during import of directory '", dir, "', it's likely that not all ", type.getValue(), "s were imported.");
            }

            // log summary.
            trace(out, filesImported, " out of ", filesTotal, " files were imported.");

        }
    }

    /**
     * Find ILCD data directory within file system
     *
     * @param fileSystem (Zip-) Filesystem to consider the root directories of
     * @return the (first) ILCD data directory found
     * @see #isILCDDataDirectory(Path dir)
     */
    private Path findILCDDataDirectory(FileSystem fileSystem) throws IOException {
        Path dataDir;
        for (Path p : fileSystem.getRootDirectories()) {
            dataDir = findILCDDataDirectory(p, ZIP_ARCHIVE_CRAWLING_DEPTH);

            if (dataDir != null)
                return dataDir;
        }

        return null;
    }

    /**
     * Find ILCD data directory
     *
     * @param dir   path of the directory to be searched
     * @param depth Maximum number of directories to traverse
     * @return absolute path of directory that should contain subdirectories with datasets (by type)
     */
    // TODO: Improve alogrithm -- Make shallow matches more efficient.
    private Path findILCDDataDirectory(Path dir, int depth) throws IOException {
        // We try to parse several different zip file formats
        // We will process
        // we allow data sets to be directly in subdirectories, like /processes/, etc. in the root directory of the zip
        // file
        // or data sets directories are under a subdirectory. Official ILCD zip files have them in "ILCD" or "ilcd"
        // subdirectories.
        // So this case is also handled

        // nonsense handling + bottom level reached
        if (dir == null)
            throw new IllegalArgumentException("Directory null.");
        if (!Files.isDirectory(dir))
            throw new IllegalArgumentException("Not a directory: " + dir);
        if (depth < 1) {
            return null;

        } else if (isILCDDataDirectory(dir)) { // Maybe we have the directory already?
            trace("Found ILCD data directory at ", dir);
            return dir;

        } else {
            // We test subdirectories
            trace("Looking for ILCD data directory within ", dir);
            try (DirectoryStream<Path> contents = Files.newDirectoryStream(dir)) {
                for (Path p : contents) {
                    // We only care about directories
                    if (!Files.isDirectory(p))
                        continue;

                    p = findILCDDataDirectory(p, depth - 1); // ! Recursion
                    if (p != null)
                        return p;
                }
            }
        }

        // Otherwise, there's none to be found within dir
        return null;
    }

    /**
     * Determine if a directory is an ILCD data set directory
     *
     * @param dir path to directory
     * @return <code>true</code> if directory is an ILCD data directory,
     * <code>false</code> otherwise
     */
    private boolean isILCDDataDirectory(Path dir) throws IOException {
        // nonsense handling
        if (dir == null || !Files.isDirectory(dir))
            return false;

        // the directory should have a subdirectory that
        // is an ILCD data set type directory
        try (DirectoryStream<Path> contents = Files.newDirectoryStream(dir)) {
            for (Path p : contents)
                if (isILCDDataSetTypeDir(p))
                    return true;
        }

        // else the directory won't be considered an ILCD data directory
        return false;
    }

    /**
     * Determine if the directory is named after an ILCD data set type
     *
     * @param dir path to directory
     * @return <code>true</code> if directory is an ILCD data set type directory,
     * <code>false</code> otherwise
     */
    private boolean isILCDDataSetTypeDir(Path dir) {
        // nonsense handling
        if (dir == null
                || !Files.isDirectory(dir)
                || dir.getFileName() == null)
            return false;
        trace("Checking whether '", dir, "' is data set type directory.");

        // the directory should have a specific name
        String name = dir.getFileName().toString();
        for (DatasetTypes v : DatasetTypes.values())
            if (name.equalsIgnoreCase(v.getValue()))
                return true;

        // if the name doesn't match the directory
        // will not be considered an ILCD data set type directory
        trace("'", dir, "' doesn't fit the criteria for being an data set type directory.");
        return false;
    }

    /////////////////////////////////////////
    // Convenience for (efficient) logging //
    /////////////////////////////////////////

    private void trace(PrintWriter out, Object... objs) {
        if (out == null && !logger.isTraceEnabled())
            return;

        String message = stringConcat(objs);
        trace(message);
        println(out, message);
    }

    private void debug(PrintWriter out, Object... objs) {
        if (out == null && !logger.isDebugEnabled())
            return;

        String message = stringConcat(objs);
        debug(message);
        println(out, message);
    }

    private void error(PrintWriter out, Throwable t, Object... objs) {
        String message = stringConcat(objs);
        println(out, message);
        logger.error(message, t);
    }

    private void trace(Object... objs) {
        if (logger.isTraceEnabled()) {
            String message = stringConcat(objs);
            logger.trace(message);
        }
    }

    private void debug(Object... objs) {
        if (logger.isDebugEnabled()) {
            String message = stringConcat(objs);
            logger.debug(message);
        }
    }

    private void println(PrintWriter out, Object... objs) {
        if (out != null && objs != null) {
            String message = stringConcat(objs);
            out.println(message);
        }
    }

    private void flush(PrintWriter out) {
        if (out != null)
            out.flush();
    }

    private String stringConcat(Object... objs) {
        if (objs == null || objs.length < 1)
            return null;
        else if (objs.length == 1)
            return String.valueOf(objs[0]);

        StringBuilder sb = new StringBuilder();
        for (Object o : objs)
            sb.append(o);

        return sb.toString();
    }

}