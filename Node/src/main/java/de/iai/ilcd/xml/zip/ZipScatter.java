package de.iai.ilcd.xml.zip;

import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;

/**
 * ZipScatter is responsible for the underlying mechanism for creating a zip
 * archive in parallel using Apache commons-compress, the skeleton for it has
 * been written according to best practices found in Apache commons-compress'
 * <p>
 * <a href=
 * "https://commons.apache.org/proper/commons-compress/examples.html">User
 * Guide</a> and Apache commons-compress'
 * <p>
 * <a href=
 * "https://github.com/apache/commons-compress/tree/master/src/test/java/org/apache/commons/compress/archivers/zip">
 * Tests source code </a>
 *
 * @author MK
 * @see ZipArchiveBuilder
 * @since soda4LCA 5.4.0
 */

//@Configuration
//@Controller
public class ZipScatter implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(ZipScatter.class);

    ParallelScatterZipCreator scatterZipCreator;
    ScatterZipOutputStream dirs;

    /**
     * @param tmpDirPath a directory used by the {@link #scatterZipCreator} for it's
     *                   output stream. <s>must be different than your target zip
     *                   path.</s>. A file with the name "scatter-dirs" will be
     *                   create there for the compression process. That file is
     *                   expected to self delete after it flushing.
     *
     *                   <i>Avoid using default /tmp since some linux distributions
     *                   mount it as ram disk.</i>
     */

    public ZipScatter(Path tmpDirPath) {
        scatterZipCreator = new ParallelScatterZipCreator();
        try {
            // Avoid using default /tmp since some linux distributions mount it as ram disk.
//			dirs = ScatterZipOutputStream.fileBased(File.createTempFile("scatter-dirs", "tmp"));
            File tmp = new File(tmpDirPath.toString(), "scatter-dirs");
            dirs = ScatterZipOutputStream.fileBased(tmp, ZipEntry.STORED);
        } catch (IOException e) {
            logger.error("Failed to create tmp file for zip scatter");
        }

    }

    public void addEntry(File f, ZipArchiveEntry zipArchiveEntry) {

        /**
         * InputStreamSupplier is a private Apache compress class required to support
         * thread-handover <b>Should never be null</b>, but may be an empty stream.
         */
        streamEntry(zipArchiveEntry, new InputStreamSupplier() {

            @Override
            public InputStream get() {
                try {
//					return new ByteArrayInputStream(Files.readAllBytes(f.toPath()));
                    return new FileInputStream(f);

                } catch (FileNotFoundException e) {
                    logger.error("Unable to read contents of the file: " + f.toPath().toString());

                }
                return new ByteArrayInputStream(new byte[0]);
            }
        });
    }

    public void addEntry(InputStream is, ZipArchiveEntry zipArchiveEntry) {
        streamEntry(zipArchiveEntry, new InputStreamSupplier() {
            @Override
            public InputStream get() {
                return is;
            }
        });
    }

    public void addEntryUnPeeled(byte[] compressedContent, ZipArchiveEntry zipArchiveEntry) {
        streamEntry(zipArchiveEntry, new InputStreamSupplier() {
            @Override
            public InputStream get() {
                return new ByteArrayInputStream(compressedContent);
            }
        });
    }

    public void streamEntry(ZipArchiveEntry zipArchiveEntry, InputStreamSupplier streamSupplier) {
        if (zipArchiveEntry.isDirectory() && !zipArchiveEntry.isUnixSymlink())
            try {
                dirs.addArchiveEntry(
                        ZipArchiveEntryRequest.createZipArchiveEntryRequest(zipArchiveEntry, streamSupplier));
            } catch (IOException e) {
                logger.error("Unable to write a zip archive entry request for the zip scatter");
            }
        else
            scatterZipCreator.addArchiveEntry(zipArchiveEntry, streamSupplier);
    }

    public void writeTo(ZipArchiveOutputStream zipArchiveOutputStream) {
        logger.info("ZipScatter is writing to the stream");
        long t = System.currentTimeMillis();
        try {
            dirs.writeTo(zipArchiveOutputStream);
        } catch (IOException e) {
            logger.error("Failed to flush entries to given zip output stream");
        }
        try {
            dirs.close();
        } catch (IOException e) {
            logger.error("Unable to gracefully close the tmp file");
        }
        try {
            scatterZipCreator.writeTo(zipArchiveOutputStream);
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Failed in communication between parallel scatter and zip output stream");
        }
        t = System.currentTimeMillis() - t;
        logger.info("ZipScatter finished writing in " + t + " ms");
    }

    public String summary() {
        return scatterZipCreator.getStatisticsMessage().toString();
    }

    @Override
    public void close() {
        try {
            dirs.close();
        } catch (Exception e) {
            logger.error("Failed to close ScatterZipOutputStream");
        }
    }

}