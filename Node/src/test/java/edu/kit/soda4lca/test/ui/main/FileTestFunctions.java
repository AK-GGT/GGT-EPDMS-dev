package edu.kit.soda4lca.test.ui.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Provides some functions to work with a file
 *
 * @author sarai
 */
public class FileTestFunctions {

    protected final static Logger log = LogManager.getLogger(FileTestFunctions.class);

    /**
     * Tries to find a file with given name and tries to delete it if it exists
     *
     * @param fileName The file name that shall be deleted
     * @param _class   The class that is calling this method
     */
    public static void tryDeleteFile(String fileName, Class<?> _class) {
        if (_class.getResourceAsStream(TestContext.getInstance().getTmpFolder() + File.separator + fileName) != null) {
            String downld = getPath(fileName, _class);
            if (File.separator.equals("\\"))
                downld = downld.substring(1);
            downld = downld.replace("/", java.io.File.separator);

            if (log.isTraceEnabled())
                log.trace("trying to delete file " + downld);

            File xls = new File(downld);
            xls.delete();
            if (_class.getResourceAsStream(TestContext.getInstance().getTmpFolder() + File.separator + fileName) != null)
                org.testng.Assert.fail("Exported file is still there from previous export and can not be deleted. Path: "
                        + getPath(fileName, _class));
        }
    }

    /**
     * Gets the path of given file name
     *
     * @param fileName The name of download file
     * @param _class   The class that calls this method
     * @return A string containing the path
     */
    public static String getPath(String fileName, Class<?> _class) {
        String path = TestContext.getInstance().getTmpFolder() + File.separator + fileName;
        if (log.isTraceEnabled())
            log.trace("getting path " + path);

        return path;
    }

}

