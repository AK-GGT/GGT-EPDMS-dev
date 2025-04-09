package de.iai.ilcd.xml.zip;

import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * {@link ZipArchiveBuilder} is a wrapper around Apache commons-compress's
 * {@link ParallelScatterZipCreator} which has the ability to create a a zip
 * archive leveraging the JVM's multiprocessing capabilities.
 *
 * <p>
 * Example usage :
 *
 * <pre>
 * {@code
 * ZipArchiveBuilder builder = new ZipArchiveBuilder(Paths.get("home", "workspace", "archive.zip", Paths.get("home", "draft")));
 * builder.add(new File("file1.xml"), "folder1/file1.xml");
 * builder.add(new File("file2.tif"), "folder2/file2.tif");
 * builder.close();}
 * </pre>
 *
 * @author MK
 * @see ZipScatter
 * @see ParallelScatterZipCreator
 * @since soda4LCA 5.4.0
 */

//@Service
public class ZipArchiveBuilder implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger(ZipArchiveBuilder.class);
    public FileOutputStream zipFileStream;
    public SeekableInMemoryByteChannel mem;
    /**
     * specify archive name and extension
     */

    public Path zipArchiveLocation;
    public String MIME = "application/zip";
    private ZipScatter zipScatter;
    private ZipArchiveOutputStream zipArchiveStream;
    private CompressionMethod method;
    private HashSet<String> entriesAdded;

    /**
     * Call this function before adding using {@link add} any file to the archive.
     * It creates the output stream and initializes the {@link ZipScatter} that
     * enables parallel compression.
     *
     * @param zipPath Path to target zip Archive on file system
     * @param tmpPath Path to any temporary directory.
     * @see ZipScatter
     */

    public ZipArchiveBuilder(Path zipPath, Path tmpPath) {
        setZipArchiveLocation(zipPath);
        _initDisk(tmpPath);
    }

    public Path getZipArchiveLocation() {
        return zipArchiveLocation;
    }

    public void setZipArchiveLocation(Path zipArchiveLocation) {
        this.zipArchiveLocation = zipArchiveLocation;
    }

    public CompressionMethod getCompressionMethod() {
        return method;
    }

    public void setCompressionMethod(CompressionMethod compressionMethod) {
        this.method = compressionMethod;
    }

    /**
     * Call this function before adding using {@link add} any file to the archive.
     * It creates the output stream from a file object and initializes the
     * {@link ZipScatter} which enables parallel compression.
     *
     * @param zipPath Location, name and extension of the zip archive you want to
     *                create on disk.
     * @see ZipArchiveBuilder#add(File, String)
     * @see ZipArchiveBuilder#add(File, String, String)
     * @see ZipScatter
     */

    private void _initDisk(Path tmpPath) {
        zipScatter = new ZipScatter(tmpPath);

        try {
            File f = zipArchiveLocation.toFile();
//			f.mkdirs();
            zipArchiveStream = new ZipArchiveOutputStream(f);
        } catch (IOException e) {
            logger.error("Unable to open an archive stream in the provided location on disk");
        }
        _initSetDefaults();
    }

    private void _initSetDefaults() {
        entriesAdded = new HashSet<String>();
//		zipArchiveStream.setMethod(CompressionMethod.STORED.code);
//		zipArchiveStream.setLevel(CompressionMethod.STORED.code);
//		method = CompressionMethod.STORED;

        method = CompressionMethod.DEFLATED;
        zipArchiveStream.setMethod(CompressionMethod.DEFLATED.code);
        zipArchiveStream.setLevel(CompressionMethod.EXPANDING_LEVEL_1.code);
        zipArchiveStream.setEncoding(StandardCharsets.UTF_8.name());
    }

    /**
     * Add files to an archive, can be used in a batch setting with almost no
     * overhead.
     * <p>
     * the entryName is allowed to have multiple forward slashes "/" indicated a
     * hierarchy structure inside archive.
     * <p>
     * After adding everything, It's <b>crucial</b> to call {@link close()} Failing
     * to call close will result in a corrupted zip archive.
     *
     * @param f         The file to be added inside the zip archive.
     * @param entryName the name or title of said file inside the archive. It is
     *                  allowed to have multiple forward slashes "/" indicated a
     *                  hierarchy structure (directories) inside archive.
     * @see ZipArchiveBuilder#initDisk()
     * @see ZipArchiveBuilder#close()
     */

    public void add(File f, String entryName) {
        if (containsEntry(entryName))
            return;
        zipScatter.addEntry(f, _createEntry(entryName));
    }

    /**
     * Add InputStream of content to an archive, can be used in a batch setting with
     * almost no overhead.
     * <p>
     * the entryName is allowed to have multiple forward slashes "/" indicated a
     * hierarchy structure inside archive.
     * <p>
     * After adding everything, It's <b>crucial</b> to call {@link close()} Failing
     * to call close will result in a corrupted zip archive.
     *
     * @param f         InputStream to the content to add inside the zip archive.
     * @param entryName the name or title of said file inside the archive. It is
     *                  allowed to have multiple forward slashes "/" indicated a
     *                  hierarchy structure (directories) inside archive.
     * @see ZipArchiveBuilder#initDisk()
     * @see ZipArchiveBuilder#close()
     */

    public void add(InputStream f, String entryName) {
        if (containsEntry(entryName))
            return;
        zipScatter.addEntry(f, _createEntry(entryName));
    }

    public void add(String contentOfFile, String entryName) {
        if (containsEntry(entryName))
            return;
        zipScatter.addEntry(new ByteArrayInputStream(contentOfFile.getBytes()), _createEntry(entryName));
    }

    public void addGarbage(byte[] data, String entryName) {
        if (containsEntry(entryName))
            return;
        zipScatter.addEntry(new ByteArrayInputStream(data), _createEntry(entryName));
    }

    /**
     * Add an already compressed zip entry, skipping the uncompression cost.
     * <p>
     * Takes a zip archive that contains <b>a single entry</b>.
     *
     * @param compressedContent A zip archive that contains <b>a single</b>
     *                          compressed entry.
     * @param entryName         The name (with full path) you want inside the new
     *                          zip archive.
     */

    public void addCompressed(byte[] compressedContent, String entryName) {
        if (containsEntry(entryName))
            return;

        InputStream is = null;
//		ByteArrayInputStream is;
        try (ZipFile z = new ZipFile(new SeekableInMemoryByteChannel(compressedContent),
                StandardCharsets.UTF_8.displayName());) {
//			is = z.getInputStream(z.getEntry(oldEntryName));

            // avoid fetching entry by name
            Enumeration<ZipArchiveEntry> e = z.getEntries();
            // expecting an archive with a single entry
            is = z.getInputStream(e.nextElement());

//			z.copyRawEntries(zipArchiveStream, new ZipArchiveEntryPredicate() {
//
//				@Override
//				public boolean test(ZipArchiveEntry zipArchiveEntry) {
//					// TODO Auto-generated method stub
//					return true; // copy everything
//				}
//			});

            ZipArchiveEntry entry = _createEntry(entryName, CompressionMethod.DEFLATED);

            // read input stream first before closing the zip
            byte[] buff = StreamUtils.copyToByteArray(is);
//			zipScatter.addEntry(new ByteArrayInputStream(buff), entry);

            // write compressed files sequentially to save memory
            zipArchiveStream.putArchiveEntry(entry);
            zipArchiveStream.write(buff);
            zipArchiveStream.closeArchiveEntry();
        } catch (IOException e) {
            logger.error("Failed to read compressed content as zip");
        }

    }

    private ZipArchiveEntry _createEntry(String entryName) {
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);

        // each entry can have a different compression method
        entry.setMethod(method.code);
        entriesAdded.add(entryName);

        return entry;
    }

    private ZipArchiveEntry _createEntry(String entryName, CompressionMethod m) {
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);

        // each entry can have a different compression method
        entry.setMethod(m.code);
        entriesAdded.add(entryName);

        return entry;
    }

    /**
     * Add files to an archive, can be used in a batch setting with almost no
     * overhead.
     * <p>
     * the entryName is allowed to have multiple forward slashes "/" indicated a
     * hierarchy structure inside archive.
     * <p>
     * After adding everything, It's <b>crucial</b> to call {@link close()}. Failing
     * to call close will result in a corrupted zip archive.
     *
     * @param f         The file to be added inside the zip archive.
     * @param entryName the name or title of said file inside the archive. It is
     *                  allowed to have multiple forward slashes "/" indicated a
     *                  hierarchy structure (directories) inside archive.
     * @param comment   comment beside given entry that can be retrieved during
     *                  extraction.
     * @see ZipArchiveBuilder#initDisk()
     * @see ZipArchiveBuilder#close()
     */

    @Deprecated
    public void _createEntry(File f, String entryName, String comment) {
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);

        // each entry can have a different compression method
        entry.setMethod(method.code);

        // other metadata can be added here
//		entry.setComment("some comments can go here");

        zipScatter.addEntry(f, entry);
    }

    /**
     * To fulfill the a business requirement that the older versions of a file
     * should have its version tag appended after its name.
     *
     * <b>Assuming entries added in order from latest to oldest.</b>
     *
     * @param entryName the full name (includes path) of the file inside the
     *                  archive.
     * @return whether a given entry has been added before to the archive.
     */

    public boolean containsEntry(String entryName) {
        return entriesAdded.contains(entryName);
    }

    /**
     * Assuming the writing/building process has been completed.
     * <b>Don't</b> use this before close/flushing archive.
     *
     * @return File object that points to zip archive on the file system
     */

    public File getFile() {

        return zipArchiveLocation.toFile();
    }

    /**
     * Get the corresponding content disposition value for a given filename.
     * <p>
     * Used for setting the response header when streaming the archive over HTTP.
     *
     * @param downloadfileName The name of the zip file that appears to the end user.
     * @return value part of content disposition header.
     */

    public String getContentDisposition(String downloadfileName) {
        return String.format("attachment; filename=\"%s\"", downloadfileName);
    }

    /**
     * Call close once after you have added all the files inside the archive.
     * <p>
     * If a file with the same name already exists, it will be overwritten.
     * <p>
     * This method signals to the implicitly instantiated
     * {@link ParallelScatterZipCreator} to start writing the entries added to
     * {@link ZipArchiveOutputStream} which results in fully structured zip archive.
     */

    @Override
    public void close() {
        zipScatter.writeTo(zipArchiveStream);
        try {
            zipArchiveStream.finish();
            zipArchiveStream.close();
        } catch (IOException e) {
            logger.error("Failed to close the archive after adding files");
        }
    }

    /**
     * Returns a message describing the overall statistics of the compression run.
     *
     * <ul>
     * <li>compressionElapsed: The number of milliseconds elapsed in the parallel
     * compression phase
     *
     * <li>mergingElapsed: The number of milliseconds elapsed in merging the results
     * of the parallel compression, the IO phase
     * <p>
     * <ul/>
     *
     * @return ScatterStatistics as String
     * @see ZipScatter
     */

    public String summary() {
        return zipScatter.summary();
    }

    public static enum CompressionMethod {

        /**
         * Compression method 0 for uncompressed entries.
         *
         * @see ZipEntry#STORED
         */

        STORED(0), // 3.3G -> 40 sec

        /**
         * UnShrinking. dynamic Lempel-Ziv-Welch-Algorithm
         *
         * @see <a href=
         * "https://www.pkware.com/documents/casestudies/APPNOTE.TXT">Explanation
         * of fields: compression method: (2 bytes)</a>
         */
        UNSHRINKING(1), // 765M -> 64 sec

        /**
         * Reduced with compression factor 1.
         *
         * @see <a href=
         * "https://www.pkware.com/documents/casestudies/APPNOTE.TXT">Explanation
         * of fields: compression method: (2 bytes)</a>
         */
        EXPANDING_LEVEL_1(2), // 759M -> 56 sec

        /**
         * Reduced with compression factor 2.
         *
         * @see <a href=
         * "https://www.pkware.com/documents/casestudies/APPNOTE.TXT">Explanation
         * of fields: compression method: (2 bytes)</a>
         */
        EXPANDING_LEVEL_2(3),

        /**
         * Imploding.
         *
         * @see <a href=
         * "https://www.pkware.com/documents/casestudies/APPNOTE.TXT">Explanation
         * of fields: compression method: (2 bytes)</a>
         */
        IMPLODING(6), // 759M -> 55 sec

        /**
         * Compression method 8 for compressed (deflated) entries.
         *
         * @see ZipEntry#DEFLATED
         */

        DEFLATED(8), // 727M -> 80 sec

        /**
         * Compression Method 9 for enhanced deflate.
         *
         * @see <a href=
         * "https://www.winzip.com/wz54.htm">https://www.winzip.com/wz54.htm</a>
         */
        ENHANCED_DEFLATED(9), // 721M -> 148 sec

        BZIP2(12);

        private int code;

        private CompressionMethod(int code) {
            this.code = code;
        }
    }
}