package de.iai.ilcd.model.dao;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provider for the digital files of an Source data set
 */
public abstract class AbstractDigitalFileProvider {

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(AbstractDigitalFileProvider.class);

    /**
     * Get the plain filename without directories
     *
     * @param digitalFileReference reference to digital file
     * @return plain filename without directories
     */
    public String getBasename(String digitalFileReference) {
        return FilenameUtils.getName(digitalFileReference);
    }

    /**
     * Determine if digital file can be provided
     *
     * @param digitalFileReference reference to digital file
     * @return <code>true</code> if digital file can be provided, <code>false</code> otherwise
     */
    public abstract boolean hasDigitalFile(String digitalFileReference);

    /**
     * Get input stream for given name
     *
     * @param digitalFileReference reference to digital file
     * @return input stream for the name or <code>null</code> if none present
     * @throws IOException on I/O errors
     */
    public abstract InputStream getInputStream(String digitalFileReference) throws IOException;

    /**
     * Provider for digital files from file system
     */
    public static class FileSystemDigitalFileProvider extends AbstractDigitalFileProvider {

        /**
         * Base directory to search for files
         */
        private final Path baseDirectory;

        /**
         * Create the provider
         *
         * @param baseDir the data set file to create for
         */
        public FileSystemDigitalFileProvider(Path baseDir) {
            logger.debug("instantiating FileSystemDigitalFileProvider, raw source data set file name is {}", baseDir.toString());
            this.baseDirectory = baseDir;
        }

        /**
         * Get the file path
         *
         * @param digitalFileReference reference
         * @return file path or <code>null</code> if nothing found
         */
        private Path getFilePath(String digitalFileReference) {
            Path result;

            // nothing to do if it's null
            if (digitalFileReference == null)
                return null;

            // get rid of unnecessary white space
            digitalFileReference = digitalFileReference.trim();

            // nothing to do if it's empty or url
            if (digitalFileReference.isEmpty()
                    || digitalFileReference.toLowerCase().startsWith("https://")
                    || digitalFileReference.toLowerCase().startsWith("http://"))
                return null;

            final Path referencedPath = this.baseDirectory.resolve(digitalFileReference);

            logger.debug("absolute path is {}", referencedPath.toAbsolutePath());

            // find first match
            if (Files.exists(referencedPath) && Files.isRegularFile(referencedPath)) {
                logger.debug("file {} exists", referencedPath);
                result = referencedPath; // Reference was fine, we're good!
            } else {
                // We will try by looking through relevant the directories,
                // matching filenames (case-insensitive)
                List<Path> relevantDirs = new ArrayList<>();
                Path suggestedDir = referencedPath.getParent();
                relevantDirs.add(suggestedDir);

                Path stdSubDir = Paths.get("../external_docs");
                if (!suggestedDir.endsWith(stdSubDir))
                    relevantDirs.add(suggestedDir.resolve(stdSubDir.toString()));

                result = findFirstDigitalFileMatchIgnoreCase(relevantDirs, referencedPath.getFileName().toString());

                if (result != null && logger.isDebugEnabled()) {
                    logger.debug("file found at '{}'", result);
                } else {
                    logger.warn("no digital file found");
                }
            }

            return (result != null && Files.isRegularFile(result)) ? result.toAbsolutePath() : null;
        }

        /**
         * We go through all files in the provided directory and all relevant
         * subdirectories (i.e. 'external_docs') to find a match.<br/>
         * <br/>
         * <i>Note: This method is case-insensitive to the provided file name.</i>
         *
         * @param dirs     directories to look through
         * @param fileName the filename to match against
         * @return the path of the match, null if none was found
         */
        private Path findFirstDigitalFileMatchIgnoreCase(Collection<Path> dirs, final String fileName) {
            for (Path dir : dirs) {
                if (!Files.exists(dir) || !Files.isDirectory(dir))
                    continue;

                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
                    // match each file name in dir against the given fileName, ignoring case
                    for (Path p : dirStream)
                        if (p.getFileName().toString().equalsIgnoreCase(fileName)
                                && Files.isRegularFile(p)) // We won't return symbolic links, directories, other...
                            return p.toAbsolutePath();

                } catch (NoSuchFileException e) {
                    // ignore
                } catch (Exception e) {
                    logger.error("Couldn't stream into directory " + dir, e);
                }
            }

            return null; // nothing found...
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasDigitalFile(String digitalFileReference) {
            return this.getFilePath(digitalFileReference) != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream getInputStream(String digitalFileReference) throws IOException {
            final Path path = getFilePath(digitalFileReference);
            if (path != null)
                return Files.newInputStream(path);

            return null;
        }

    }
}