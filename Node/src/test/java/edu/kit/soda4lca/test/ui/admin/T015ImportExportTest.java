package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.testng.ScreenShooter;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.xml.zip.exceptions.ZipException;
import de.iai.ilcd.xml.zip.exceptions.ZipMissingException;
import de.iai.ilcd.xml.zip.exceptions.ZipUnreadableException;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Condition.disabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

/**
 * Test import and export
 *
 * @author mark.szabo
 */
@Listeners({ScreenShooter.class})
public class T015ImportExportTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T015ImportExportTest.class);
    private final static Path ZIP_DOWNLOAD_DIRECTORY = Paths.get("target/cargo/configurations/tomcat9x/webapps/Node/WEB-INF/var/zips").toAbsolutePath();
    private final static Path REFERENCE_XML_ARCHIVE = Paths.get("src/test/resources/sample_data_med.zip").toAbsolutePath();
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T015ImportExportTest.xml"));
    }

    /////////////////
    // IMPORT TEST //
    /////////////////

    /**
     * Import data from a file (specified in the Main as datafile-path)
     */
    @Test(priority = 151)
    public void importData() throws ZipException, IOException {
        log.info("Import test: Importing " + REFERENCE_XML_ARCHIVE);
        WebDriverRunner.setWebDriver(driver);
        WebDriverRunner.clearBrowserCache();
        log.trace("Cache cleared.");

        assertDirectoryExists("zip archive containing reference xml files", REFERENCE_XML_ARCHIVE);
        if (log.isTraceEnabled()) {
            log.trace(LINE_SEPARATOR + "Full content of import archive:");
            log.trace(getFullDescriptionOfZipContent(REFERENCE_XML_ARCHIVE));
        }

        navigateToUploadView();

        loadSamplesToUploadDir();

        waitThenNavigateToImportView();

        importUpload();

        switchTo().defaultContent();
        log.info("Import test finished");
    }

    private void navigateToUploadView() {
        log.info("Navigating to upload view.");

        testFunctions.login("admin", "default", true, true); // login as admin
        testFunctions.gotoAdminArea(); // click on Admin area
        $(By.linkText(TestContext.lang.getProperty("admin.dataImport"))).hover(); // Hover over the menu
        $(By.linkText(TestContext.lang.getProperty("admin.import"))).click(); // Click Import
        $x(".//*[@id='admin_footer']").shouldBe(visible); // wait for the site to load

        if (log.isTraceEnabled())
            log.trace("Footer visible, site must be loaded."); // ;-)
        else if (log.isDebugEnabled())
            log.debug("Site has loaded.");
    }

    private void loadSamplesToUploadDir() {
        log.info("Uploading file " + REFERENCE_XML_ARCHIVE);
        WebElement browseButton = driver.findElement(By.id("documentToUpload_input"));
        browseButton.sendKeys(REFERENCE_XML_ARCHIVE.toAbsolutePath().toString());

        $x("//button[contains(.,'" + TestContext.lang.getProperty("admin.fileUpload.upload") + "')]").click();
        $x("//button[contains(.,'" + TestContext.lang.getProperty("admin.fileUpload.upload") + "')]").shouldBe(disabled);
    }

    private void waitThenNavigateToImportView() {
        SelenideElement button = $x("//button[contains(.,'" + TestContext.lang.getProperty("admin.importUpload.goToStep2") + "')]");
        Instant start = Instant.now();
        while (Duration.between(start, Instant.now()).getSeconds() < TestContext.timeout
                && button.exists()) {
            button.click();
            if (log.isTraceEnabled())
                log.trace("Clicking 'next' button...");
        }

        // If the page title suggests we've reached the second step we expect the upload to have succeeded.
        String pageTitle = $x(".//*[@id='admin_content']/h1").text();
        if (!pageTitle.equals((TestContext.lang.getProperty("admin.importUpload.step2"))))
            Assert.fail("Upload of '" + REFERENCE_XML_ARCHIVE + " has not finished within " + TestContext.timeout + " seconds.");
        else {
            log.debug("Upload finished.");
            log.info("Navigated to import view.");
        }
    }

    private void importUpload() {
        log.info("Selecting import target");
        log.debug("Clicking spinner");
        WebElement element = testFunctions.findAndWaitOnElement(
                By.xpath(
                        ".//*[@id='tableForm:importRootDataStock_label']"
                ));
        element.click();
        log.trace("Clicked: " + element);

        // Click on RootStock1
        String key = "RootStock1";
        log.debug("Clicking on '" + key + "'");
        element = testFunctions.findAndWaitOnElement(
                By.xpath(
                        ".//*[@id='tableForm:importRootDataStock_panel']/div/ul/li[contains(.,'" + key + "')]"
                ));
        element.click();
        log.trace("Clicked: " + element);

        // Click on Import files
        key = TestContext.lang.getProperty("admin.importFiles");
        log.info("Starting to import...");
        log.debug("Clicking on '" + key + "'");
        element = testFunctions.findAndWaitOnElement(
                By.xpath(
                        "//button[contains(.,'" + key + "')]"
                ));
        element.sendKeys(Keys.RETURN); // Oftentimes the button is overlapped by the (collapsing) spinner,
        // so sending 'RETURN' is safer.
        log.trace("Clicked: " + element);

        // Status log is on an iframe, so change the driver
        switchTo().frame("console");

        testFunctions.findAndWaitOnConsoleElement(
                By.xpath(
                        "html/body[contains(.,'------ import of files finished ------')]"
                ));

        if (!WebDriverRunner.getWebDriver().getPageSource().contains("------ import of files finished ------"))
            org.testng.Assert.fail("Importing failed. For more details, see the log: " + WebDriverRunner.getWebDriver().getPageSource());
        else
            log.trace("Import finished");
    }


    /////////////////
    // EXPORT TEST //
    /////////////////

    /**
     * Export entire dataset, then save automatically (with firefox profile, see Main). Then with TFile compare some
     * xml files
     * inside the zip with stored ones
     */
    @Test(priority = 152, dependsOnMethods = {"importData"})
    public void exportData() throws URISyntaxException, IOException, InterruptedException, ZipException {
        log.info("Export test: Exporting the whole data base.");

        assertDirectoryExists("download directory", ZIP_DOWNLOAD_DIRECTORY);
        if (log.isTraceEnabled())
            log.trace(getFullDescriptionOfDirContent(ZIP_DOWNLOAD_DIRECTORY));

        log.trace("Clearing zip files from download directory if there are any.");
        clearZipsFromDownloadDirectory(); // We want the 'first' zip to be the right one...

        navigateToExportView();

        doExport();

        Path downloadedArchive = obtainFirstZipDownloadWaiting();
        if (log.isTraceEnabled()) {
            log.trace(LINE_SEPARATOR + "Full content of downloaded archive: ");
            log.trace(getFullDescriptionOfZipContent(downloadedArchive));
        }

        assertZipMatchesReferenceZip(downloadedArchive);

        // Leaving things clean
        clearZipsFromDownloadDirectory();
        log.info("Export test finished");
    }

    private void navigateToExportView() {
        log.info("Navigating to export view.");
        driver.manage().deleteAllCookies();
        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        // wait for the site to load
        testFunctions.waitUntilSiteLoaded();
        // Hover over the menu
        $(By.linkText(TestContext.lang.getProperty("admin.dataImport"))).hover();
        // Click Export
        $(By.linkText(TestContext.lang.getProperty("admin.export"))).click();
    }

    private void doExport() {
        String buttonText = TestContext.lang.getProperty("admin.export.db");

        String logMsg = "Export entire database";
        if (log.isTraceEnabled())
            logMsg += " by clicking on button containing " + buttonText;
        log.info(logMsg);

        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + buttonText + "')]")).click();
        log.debug("Download started.");
    }

    private Path obtainFirstZipDownloadWaiting() {
        Path zipArch = null;

        final long timeout = TestContext.timeout; // 120 seconds
        log.debug("Waiting for zip archive to be available... (timeout = " + timeout + "s)");

        log.trace("Looking for (first) zip within " + ZIP_DOWNLOAD_DIRECTORY);
        final Instant start = Instant.now();
        while (Duration.between(start, Instant.now()).getSeconds() < timeout
                && zipArch == null) {
            zipArch = findFirstZipWithin(ZIP_DOWNLOAD_DIRECTORY); // maps null to null
        }

        if (zipArch == null) {
            org.testng.Assert
                    .fail("No zip archive available at '" +
                            ZIP_DOWNLOAD_DIRECTORY + "'" +
                            "' after " +
                            timeout +
                            " seconds.");
        }
        log.debug("Found " + zipArch);
        return zipArch;
    }

    private void assertZipMatchesReferenceZip(Path dir) {
        log.debug("Compare to reference archive...");

        // We compare one xml per data set type
        // TODO: Include lifecycle models
        boolean integrityCheckDone = false;
        List<Path> filesForContentComparison = new ArrayList<>();
        filesForContentComparison.add(Paths.get("ILCD", DataSetType.PROCESS.getStandardFolderName(), "4cffadc4-e2a2-4d34-9087-e54a2cfa4bd0.xml"));
        filesForContentComparison.add(Paths.get("ILCD", DataSetType.LCIAMETHOD.getStandardFolderName(), "edabaa8b-89d0-4cb6-ba92-444ac5265422.xml"));
        filesForContentComparison.add(Paths.get("ILCD", DataSetType.FLOW.getStandardFolderName(), "0a2a71f2-4f59-4a67-b5f1-d633039ce0ea.xml")); // elementary flow
        filesForContentComparison.add(Paths.get("ILCD", DataSetType.FLOW.getStandardFolderName(), "ffd105a3-1ae2-4af3-b7b0-ebceb224fd40.xml")); // product flow
        filesForContentComparison.add(Paths.get("ILCD", DataSetType.FLOWPROPERTY.getStandardFolderName(), "93a60a56-a3c8-19da-a746-0800200c9a66.xml"));
        filesForContentComparison.add(Paths.get("ILCD", DataSetType.UNITGROUP.getStandardFolderName(), "ff8ed45d-bbfb-4531-8c7b-9b95e52bd41d.xml"));
        filesForContentComparison.add(Paths.get("ILCD", DataSetType.SOURCE.getStandardFolderName(), "dff34d61-54e9-4404-95ca-c09cc01b7f40.xml"));
        filesForContentComparison.add(Paths.get("ILCD", DataSetType.CONTACT.getStandardFolderName(), "d0f67f21-22d8-4e5e-914b-6f7dde923e33.xml"));
        filesForContentComparison.add(Paths.get("ILCD", "external_docs", "Aluminium_EPD.pdf"));
        int total = filesForContentComparison.size();

        int successes = 0;
        try (
                FileSystem downloadArchFS = FileSystems.newFileSystem(dir, null);
                FileSystem referenceArchFS = FileSystems.newFileSystem(REFERENCE_XML_ARCHIVE, null)
        ) {

            // check file integrity
            for (Path relativePath : filesForContentComparison) {
                Path referenceFile = referenceArchFS.getPath(relativePath.toString());
                Path downloadedFile = downloadArchFS.getPath(relativePath.toString());

                if (log.isDebugEnabled())
                    log.debug("Comparing contents of sample files ... " + "[" + (successes + 1) + " of " + total + "]");

                if (log.isTraceEnabled()) {
                    log.trace("Reference: " + referenceFile);
                    log.trace("Download: " + downloadedFile);
                }
                assertEqualFiles(referenceFile, downloadedFile);
                successes++;
            }
            integrityCheckDone = true;

            // check completeness
            assertAllUploadedFilesPresentInDownloadArchive(referenceArchFS, downloadArchFS);

        } catch (IOException ioe) {
            String errorMsg = "Error occurred when";
            if (!integrityCheckDone) {
                errorMsg += " trying to match '" +
                        dir +
                        "'  against  '" +
                        REFERENCE_XML_ARCHIVE +
                        "'. ";
            } else {
                errorMsg += " trying to check completeness of exported archive.";
            }

            if (successes == total)
                log.error(errorMsg, ioe);
            else
                Assert.fail(errorMsg, ioe);
        }
    }

    private void assertAllUploadedFilesPresentInDownloadArchive(FileSystem referenceArchFS, FileSystem downloadArchFS) throws IOException {
        log.info("Checking completeness of downloaded data...");
        for (Path referenceRoot : referenceArchFS.getRootDirectories()) {
            Files.walkFileTree(referenceRoot, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.endsWith("META-INF"))
                        return FileVisitResult.SKIP_SUBTREE;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.endsWith("ILCDLocations.xml")
                            || file.endsWith("ILCDFlowCategorization.xml")
                            || file.endsWith("ILCDClassification.xml"))
                        return FileVisitResult.CONTINUE;

                    boolean existsInDownloadArch = false;
                    for (Path downloadRoot : downloadArchFS.getRootDirectories()) {
                        Path downloadedFile = downloadRoot.resolve(file.toString());
                        if (Files.exists(downloadedFile) && Files.isRegularFile(downloadedFile))
                            existsInDownloadArch = true;
                        break;
                    }

                    if (!existsInDownloadArch) {
                        String downloadArchContentDescription = LINE_SEPARATOR + "Full content of downloaded archive:" + LINE_SEPARATOR;
                        try {
                            downloadArchContentDescription = getFullDescriptionOfDirContent(downloadArchFS.getRootDirectories().iterator().next()); // Ideally we would use the arch file
                            // but we appearently can't get this
                            // out of the corresponding FileSystem
                        } catch (Exception e) {
                            log.error("Can't list contents of download archive!", e);
                        }

                        Assert.fail("Regular file " +
                                file +
                                " not present in download archive." +
                                LINE_SEPARATOR +
                                "Listing contents of downloaded zip filesystem." +
                                LINE_SEPARATOR +
                                downloadArchContentDescription);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        }
        log.info("Downloaded data is complete! ✅");
    }

    /**
     * Asserting that two files are equal to each other
     *
     * @param download  downloaded file
     * @param reference original file
     */
    private void assertEqualFiles(Path download, Path reference) throws IOException {
        if (!Files.exists(reference))
            throw new FileNotFoundException("Reference file does not exist: " + reference);
        if (!Files.isRegularFile(reference))
            throw new IllegalArgumentException("Expected a regular file at reference: " + reference);
        if (!Files.exists(download))
            throw new FileNotFoundException("File should exist in downloads! Relative path: " + reference);
        if (!Files.isRegularFile(download))
            throw new IllegalArgumentException("Expected a regular file at download: " + download);

        // Check equality line by line
        try (LineNumberReader originalReader = new LineNumberReader(new InputStreamReader(Files.newInputStream(reference)));
             LineNumberReader otherReader = new LineNumberReader(new InputStreamReader(Files.newInputStream(download)))) {

            String originalLine;
            String otherLine;
            // compare line by line the stored and the downloaded file
            while ((originalLine = originalReader.readLine()) != null) {
                otherLine = otherReader.readLine();
                if (!originalLine.equals(otherLine))
                    org.testng.Assert.fail("Expected equal files! Difference found in line " +
                            originalReader.getLineNumber() +
                            ": '" +
                            originalLine +
                            "' vs '" +
                            otherLine
                            + "'");
            }
        }
    }

    private void clearZipsFromDownloadDirectory() throws IOException, ZipException {
        if (!Files.exists(ZIP_DOWNLOAD_DIRECTORY) || !Files.isDirectory(ZIP_DOWNLOAD_DIRECTORY))
            throw new IllegalArgumentException("Cannot delete zip files from '" +
                    ZIP_DOWNLOAD_DIRECTORY +
                    "' it does either not exist or it's not a directory.");

        Instant start = Instant.now();

        Path zip = findFirstZipWithin(ZIP_DOWNLOAD_DIRECTORY);
        while (Duration.between(start, Instant.now()).getSeconds() < TestContext.timeout
                && zip != null) {

            try {
                Files.delete(zip);
                zip = findFirstZipWithin(ZIP_DOWNLOAD_DIRECTORY);
            } catch (IOException ioe) {
                log.error("Error occurred during deletion of zip files in " + ZIP_DOWNLOAD_DIRECTORY, ioe);

                if (null != findFirstZipWithin(ZIP_DOWNLOAD_DIRECTORY)) {
                    if (log.isTraceEnabled()) {
                        log.trace(getFullDescriptionOfZipContent(ZIP_DOWNLOAD_DIRECTORY));
                    }

                    Assert.fail("Not all zip files were deleted.", ioe);
                }
            }
        }
    }

    private Path findFirstZipWithin(Path dir) {
        if (!Files.exists(dir)) {
            log.trace(dir + " does not exist.");
            return null;
        } else if (!Files.isDirectory(dir)) {
            log.trace(dir + " is not a directory.");
            return null;
        }

        try (DirectoryStream<Path> contents = Files.newDirectoryStream(dir)) {
            for (Path p : contents) {
                if (Files.isRegularFile(p) && p.getFileName().toString().endsWith(".zip"))
                    return p;
            }
        } catch (Exception e) {
            log.trace("Exception caught while scanning!!", e);
        }

        return null;
    }


    /////////////////////////
    // ADDITIONAL LOGGING  //
    /////////////////////////

    private String getFullDescriptionOfDirContent(Path dir) throws IOException {
        // Nonsense handling
        if (dir == null)
            throw new IllegalArgumentException("Path to directory: null");
        else if (!Files.exists(dir))
            throw new NoSuchFileException(dir.toAbsolutePath().toString());
        else if (!Files.isReadable(dir))
            throw new IllegalArgumentException("Unreadable: " + dir.toAbsolutePath());
        else if (!Files.isDirectory(dir))
            throw new NotDirectoryException(dir.toString());

        StringBuilder sb = new StringBuilder("Listing all contents of directory " + dir);
        sb.append(LINE_SEPARATOR);
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                sb.append(file).append(LINE_SEPARATOR);

                return FileVisitResult.CONTINUE;
            }
        });
        return sb.toString();
    }

    private String getFullDescriptionOfZipContent(Path zipArchive) throws ZipException, NotDirectoryException, NoSuchFileException {
        // Nonsense handling
        if (zipArchive == null)
            throw new IllegalArgumentException("Path to zip is null!");
        if (!Files.exists(zipArchive))
            throw new ZipMissingException(zipArchive.toString());
        if (!Files.isReadable(zipArchive))
            throw new ZipUnreadableException(zipArchive.toString());
        if (!Files.isRegularFile(zipArchive))
            throw new IllegalArgumentException("Not a file: " + zipArchive);
        else if (zipArchive.getFileName() == null || zipArchive.getFileName().toString().trim().isEmpty())
            throw new IllegalArgumentException("Empty file name: " + zipArchive);
        else if (!zipArchive.getFileName().toString().endsWith(".zip"))
            throw new IllegalArgumentException("Does not end with '.zip' -- file: " + zipArchive);

        StringBuilder sb = new StringBuilder(LINE_SEPARATOR + "Full contents of zip filesystem " + zipArchive);
        try (FileSystem zipArch = FileSystems.newFileSystem(zipArchive, null)) {
            for (Path path : zipArch.getRootDirectories()) {
                sb.append(LINE_SEPARATOR).append(getFullDescriptionOfDirContent(path));
            }

        } catch (UnsupportedOperationException uoe) { // This concerns the ZipFileSystem
            Assert.fail("Expected a zip archive at " + zipArchive, uoe);
        } catch (NoSuchFileException | NotDirectoryException | IllegalArgumentException iae) {
            throw iae;
        } catch (IOException ioe) {
            log.error("Error occurred when trying to obtain zip file system for '" + zipArchive + "'", ioe);
        }

        return sb.toString();
    }

    private void assertDirectoryExists(String description, Path dir) {
        log.trace("Expecting to find " + description + " at " + dir);
        log.trace(".......... relative path: " + Paths.get(".").toAbsolutePath().relativize(dir.toAbsolutePath()));
        Assert.assertTrue(Files.exists(dir), "The directory '" + dir + "' does not exist.");
    }

}