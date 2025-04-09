package edu.kit.soda4lca.test.ui.admin.t151ExportCacheTest;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.testng.ScreenShooter;
import de.iai.ilcd.service.util.JobState;
import edu.kit.soda4lca.test.ui.main.TestContext;
import edu.kit.soda4lca.test.ui.main.TestFunctions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Listeners;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.*;

@Listeners({ScreenShooter.class})
public class UI {

    private static final Logger log = LogManager.getLogger(UI.class);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Path TMP_DIRECTORY = Path.of(TestContext.getInstance().getTmpFolder());
    private final WebDriver driver;
    private final TestFunctions testFunctions;

    public UI(WebDriver driver) {
        this.driver = driver;
        this.testFunctions = new TestFunctions();
        this.testFunctions.setDriver(this.driver);
    }

    void login_as_admin() {
        final var loginUrl = TestContext.PRIMARY_SITE_URL + "login.xhtml";
        final var username = "admin";
        final var password = "default";

        log.debug("Opening site '{}' ...", loginUrl);
        open(loginUrl);
        testFunctions.waitUntilSiteLoaded();
        log.debug("Entering username ...");
        log.trace("Username: '{}'", username);
        $x("//div[@id='loginPanel']//input[@id='name']").sendKeys(username);

        log.debug("Entering password ...");
        log.trace("Password: '{}'", password);
        $x("//div[@id='loginPanel']//input[@id='passw']").sendKeys(password);

        log.debug("Clicking login ...");
        $x("//button[@id='loginButton']").sendKeys(Keys.ENTER);
    }

    void import_zip(@Nonnull final Path zip,
                    @Nonnull final Stock rootTarget) {
        // ARRANGE
        var url = TestContext.PRIMARY_SITE_URL + "admin/importUpload.xhtml";

        // ### "path finder"
        final Supplier<SelenideElement> pathFinder = // the element that needs the file path as text
                () -> $x( // identify by id
                        "//input[@id='documentToUpload_input']"
                );

        // ### upload button
        final Supplier<SelenideElement> uploadButton = // the button to trigger the upload event ---> click to start upload
                () -> $x(String.format( // identify by class + peek text
                        "//div[@id = 'documentToUpload']" + // make sure we're in the right area
                                "//button[contains(concat(' ', @class, ' '), ' ui-fileupload-upload ')]" +
                                "/span[contains(concat(' ', @class, ' '), ' ui-button-text ') and text() = '%s']",
                        TestContext.lang.getProperty("admin.fileUpload.upload")
                ));

        // ### go to step 2 button
        final Supplier<SelenideElement> goToStepTwoButton = // the button to proceed --> click to load follow-up page
                () -> $x( // identify by id
                        "//button[@id = 'uploadBtn']"
                );

        // ### target stock dropdown
        final Supplier<SelenideElement> targetStockDropDownMenu = // the select one menu holding all root data stocks by name
                () -> $x( // identify by id
                        "//div[@id = 'tableForm:importRootDataStock']"
                );
        final Supplier<SelenideElement> targetStockDropDownMenuTrigger = // the trigger element for the select one menu ---> click to expand
                () -> $x(String.format(
                        "//label[@id = '%s_label']",
                        targetStockDropDownMenu.get().getAttribute("id")
                ));
        final Supplier<SelenideElement> targetStockItem = // the item associated with our target stock ---> click to select correct target stock
                () -> $x(String.format( // identify by id
                        "//ul[@id = '%s']" +
                                "/li[text() = '%s']",
                        targetStockDropDownMenu.get().getAttribute("aria-owns"),
                        rootTarget.getName()
                ));

        // ### do import button
        final Supplier<SelenideElement> importButton = // the button that triggers the import ---> click to start
                () -> $x( // identify by id
                        "//button[@id = 'tableForm:importBtn']"
                );

        SelenideElement tmp;


        // ACT
        log.debug("Going to page '{}' ...", url);
        driver.get(url);

        log.debug("Setting file path ...");
        log.trace("(using '{}')", zip.toAbsolutePath());
        pathFinder.get().sendKeys(zip.toAbsolutePath().toFile().toString());

        log.debug("Uploading ...");
        uploadButton.get().click();

        log.debug("Proceeding to step 2 ...");
        tmp = goToStepTwoButton.get();
        testFunctions.waitUntilTrue(tmp::isEnabled, 15);
        tmp.click();

        log.debug("Selecting target stock ...");
        targetStockDropDownMenuTrigger.get().click(); // expand
        tmp = targetStockItem.get();
        testFunctions.waitUntilTrue(tmp::isDisplayed, 5);
        tmp.click();


        log.debug("Importing ...");
        tmp = importButton.get();
        testFunctions.waitUntilTrue(tmp::isEnabled, 5);
        tmp.sendKeys(Keys.ENTER);


        // VERIFY
        waitForImportToFinish();
        log.debug("Done: Imported '{}' to {}", zip, rootTarget);
    }

    void move_dataset(@Nonnull final Stock sourceStock,
                      @Nonnull final UUID datasetUuid,
                      @Nonnull final Stock targetStock) {
        // ARRANGE
        var url = TestContext.PRIMARY_SITE_URL + String.format("admin/datasets/manageProcessList.xhtml?stock=%s", sourceStock.getName());

        // ### select data set (row) checkbox
        final Supplier<SelenideElement> dataSetRow = // the row which holds our data set
                () -> $x(String.format( // identify by peeking the uuid (and going back up again)
                        "//tr" +
                                "/td" +
                                "/div" +
                                "/span[contains(@id, 'nameColumnContentUuid') and text()='%s']" +
                                "/../../..", // (return the row ('tr') instead of the uuid label)
                        datasetUuid
                ));
        final Supplier<SelenideElement> dataSetRowSelectCheckbox =  // row select checkbox ---> click to select data set
                () -> dataSetRow.get().$x( // identify by class
                        "td[contains(concat(' ', @class, ' '), ' ui-selection-column ')]" +
                                "/div[contains(concat(' ', @class, ' '), ' ui-chkbox ')]"
                );

        // ### move warning
        final Supplier<SelenideElement> moveWarningDialog =  // the "with great power comes great responsibility" dialog
                () -> $x(String.format( // identify by class and title
                        "//div[contains(concat(' ', @class, ' '), ' ui-confirm-dialog ')]" +
                                "/div" +
                                "/span[text() = '%s' and contains(concat(' ', @class, ' '), ' ui-dialog-title ')]" +
                                "/../..",
                        TestContext.lang.getProperty("admin.stock.move.datasets")
                ));
        final Supplier<SelenideElement> moveWarningYesButton =
                () -> moveWarningDialog.get().$x( // accept-the-warning button ---> click to proceed
                        "div[contains(concat(' ', @class, ' '), ' ui-dialog-buttonpane ')]" +  // it really is 'pane' instead of 'panel', yes
                                "/button[contains(concat(' ', @class, ' '), ' ui-confirmdialog-yes ')]"
                );

        // ### move dialog
        final Supplier<SelenideElement> moveDialog = // the actual move dialog
                () -> $x( // identifying the dialog by class + peeking title
                        "//div[contains(concat(' ', @class, ' '), ' ui-dialog ') and not(contains(concat(' ', @class, ' '), ' ui-confirm-dialog '))]" + // the last bit excludes the warning dialog which has the class 'ui-confirm-dialog' (as opposed to 'ui-dialog')
                                "/div" +
                                "/span[contains(concat(' ', @class, ' '), ' ui-dialog-title ') and text() = 'Move datasets']" +
                                "/../.."
                );

        final Supplier<SelenideElement> targetStockDropDownMenu =  // select one menu to pick the target stock
                () -> moveDialog.get().$x(String.format( // identifying it by label
                        "div//div[text() = '%s']" +
                                "/div[contains(concat(' ', @class, ' '), ' ui-selectonemenu ')]",
                        TestContext.lang.getProperty("admin.stock.move.target")
                ));
        final Supplier<SelenideElement> targetStockOptions = // unordered list of select one menu items. Please note: This list/panel is *not found within* the dropdown element!
                () -> $x(String.format( // identifying it by id.
                        "//ul[@id = '%s_items' and contains(concat(' ', @class, ' '), ' ui-selectonemenu-items ')]",
                        targetStockDropDownMenu.get().getAttribute("id")));
        final Supplier<SelenideElement> targetStockItem = // the item we want ---> click to select
                () -> targetStockOptions.get().$x(String.format( // identifying by text value
                        "li[text() = '%s']",
                        targetStock.getName()));

        final Supplier<SelenideElement> okButton =  // the ok button to trigger the move
                () -> moveDialog.get().$x( // identify by peeking label and then go up to button element
                        "div//div[contains(concat(' ', @class, ' '), ' confirmDlgButtons ')]" +
                                "//span[text() = 'OK']" +
                                "/.."
                );


        // ACT
        log.debug(String.format("Going to page '%s'", url));
        driver.get(url);
        testFunctions.waitUntilSiteLoaded();

        log.debug("Selecting data set ...");
        dataSetRowSelectCheckbox.get().click();

        log.debug("Keyboard shortcut: CTRL + SHIFT + M ...");
        new Actions(driver)
                .keyDown(Keys.LEFT_CONTROL)
                .keyDown(Keys.LEFT_SHIFT)
                .sendKeys("M")
                .build()
                .perform();

        log.debug("Accept warning ...");
        moveWarningYesButton.get().click();

        log.debug("Selecting target stock ...");
        log.trace("Expanding select one menu ...");
        targetStockDropDownMenu.get().click();

        log.trace("Clicking item ...");
        targetStockItem.get().click();

        log.debug("Clicking OK ...");
        okButton.get().sendKeys(Keys.ENTER);

        testFunctions.waitUntilSiteLoaded();

        // VERIFY
        testFunctions.waitUntilTrue(() -> !sourceStock.contains(datasetUuid), 10);
        if (sourceStock.contains(datasetUuid))
            throw new IllegalStateException(String.format("Move operation unsuccessful on DB level. Data set still part of source stock. %sSource: %s%sTarget: %s%s Dataset uuid:%s", LINE_SEPARATOR, sourceStock, LINE_SEPARATOR, targetStock, LINE_SEPARATOR, datasetUuid));
        testFunctions.waitUntilTrue(() -> targetStock.contains(datasetUuid), 10);
        if (!targetStock.contains(datasetUuid))
            throw new IllegalStateException(String.format("Move operation unsuccessful on DB level. Data set not part of target stock. %sSource: %s%sTarget: %s%s Dataset uuid:%s", LINE_SEPARATOR, sourceStock, LINE_SEPARATOR, targetStock, LINE_SEPARATOR, datasetUuid));
        log.trace("Done: Moved data set '{}' from {} to {}.", datasetUuid, sourceStock, targetStock);
    }

    void assign_dataset(@Nonnull final Stock sourceStock,
                        @Nonnull final UUID datasetUuid,
                        @Nonnull final Stock targetStock) {
        // ARRANGE
        var url = TestContext.PRIMARY_SITE_URL + String.format("admin/stocks/editStock.xhtml?stockId=%d&tabId=3&stock=%s", targetStock.getId(), sourceStock.getName());

        // ### assign button
        final Supplier<SelenideElement> assignProcessesButton = // the assign button to open the view where we can select data sets (processes) that need to be assigned
                () -> $x( // start with the visible tab, then the button somewhere inside
                        "//button[@id = 'stockTabs:dataSetTabView:assignDataSetBtnProcess']"
                );

        // ### select data set (row) checkbox
        final Supplier<SelenideElement> dataSetRow = // the row which holds our data set
                () -> $x(String.format( // identify by peeking the uuid (and going back up again)
                        "//div[@id = 'stockTabs:dataSetTabView:assignProcessDialog']" + // make sure we're in the right view: the 'assign processes' dialog
                                "//tr" +
                                "/td" +
                                "/div" +
                                "/span[contains(@id, 'nameColumnContentUuid') and text()='%s']" +
                                "/../../..", // (return the row ('tr') instead of the uuid label)
                        datasetUuid
                ));
        final Supplier<SelenideElement> dataSetRowSelectCheckbox =  // row select checkbox ---> click to select data set
                () -> dataSetRow.get().$x( // identify by class
                        "td[contains(concat(' ', @class, ' '), ' ui-selection-column ')]" +
                                "/div[contains(concat(' ', @class, ' '), ' ui-chkbox ')]"
                );

        // ### the assign button
        final Supplier<SelenideElement> assignSelectedProcessesButton =  // the button to assign the selected elements ---> click to trigger dependency dialog
                () -> $x(
                        "//button[@id = 'stockTabs:dataSetTabView:adProcessDataTablebtn']"
                );

        // ### dependency dialog OK button
        final Supplier<SelenideElement> dependencyDialogOkButton = // the OK button in the dependency mode dialog ---> click to confirm order and trigger actual un-assign action// The OK/proceed/confirm button in the dialog where we can choose a dependency mode
                () -> $x(String.format( // it lies in an identifiable span
                        "//span[@id = 'stockTabs:dataSetTabView:adProcessDataTableattachSelectedToStockpanel']" +
                                "//button" +
                                "/span[contains(concat(' ', @class, ' '), 'ui-button-text') and text() = '%s']" +
                                "/..",
                        TestContext.lang.getProperty("admin.ok")
                ));


        // ACT
        log.debug("Going to page '{}'", url);
        driver.get(url);
        testFunctions.waitUntilSiteLoaded();

        log.debug("Opening assign dialog ...");
        assignProcessesButton.get().click();

        log.debug("Selecting data set ...");
        dataSetRowSelectCheckbox.get().click();

        log.debug("Clicking assign button ...");
        assignSelectedProcessesButton.get().click();

        log.debug("Confirming dependency mode ...");
        dependencyDialogOkButton.get().sendKeys(Keys.ENTER);

        testFunctions.waitUntilSiteLoaded();

        // VERIFY
        testFunctions.waitUntilTrue(() -> targetStock.contains(datasetUuid), 10);
        if (!targetStock.contains(datasetUuid))
            throw new IllegalStateException(String.format("Assign operation unsuccessful on DB level.%sTarget data stock: %s%s Dataset uuid:%s", LINE_SEPARATOR, targetStock, LINE_SEPARATOR, datasetUuid));
        log.trace("Done: Assigned data set '{}' to {}", datasetUuid, targetStock);
    }

    void unassign_dataset(@Nonnull final Stock rootSource,
                          @Nonnull final UUID datasetUuid,
                          @Nonnull final Stock logicalSource) {
        // ARRANGE
        var url = TestContext.PRIMARY_SITE_URL + String.format("admin/stocks/editStock.xhtml?stockId=%d&tabId=3&stock=%s", logicalSource.getId(), rootSource.getName());

        // ### select data set (row) checkbox
        final Supplier<SelenideElement> dataSetRow = // the row which holds our data set
                () -> $x(String.format( // identify by peeking the uuid (and going back up again)
                        "//div[@id = 'stockTabs:dataSetTabView:ctProcessDataTable']" + // make sure we're in the right view
                                "//tr" +
                                "/td" +
                                "/div" +
                                "/span[contains(@id, 'nameColumnContentUuid') and text()='%s']" +
                                "/../../..", // (return the row ('tr') instead of the uuid label)
                        datasetUuid
                ));
        final Supplier<SelenideElement> dataSetRowSelectCheckbox =  // row select checkbox ---> click to select data set
                () -> dataSetRow.get().$x( // identify by class
                        "td[contains(concat(' ', @class, ' '), ' ui-selection-column ')]" +
                                "/div[contains(concat(' ', @class, ' '), ' ui-chkbox ')]"
                );

        // ### remove button
        final Supplier<SelenideElement> removeButton = // the button to remove the selected elements ---> click to trigger dependency dialog
                () -> $x(String.format( // identify by id + peeking button label
                        "//button[@id = 'stockTabs:dataSetTabView:ctProcessDataTablebtn']" +
                                "/span[text() = '%s']" +
                                "/..",
                        TestContext.lang.get("admin.removeSelected")
                ));

        // ### dependency dialog OK button
        final Supplier<SelenideElement> dependencyDialogOkButton = // the OK button in the dependency mode dialog ---> click to confirm order and trigger actual un-assign action
                () -> $x(String.format( // identify by peeking text
                        "//span[@id = 'stockTabs:dataSetTabView:ctProcessDataTabledetachSelectedFromStockpanel']" +
                                "/button" +
                                "/span[contains(concat(' ', @class, ' '), 'ui-button-text') and text() = '%s']",
                        TestContext.lang.getProperty("admin.ok")
                ));

        // ACT
        log.debug("Going to page '{}'", url);
        driver.get(url);
        testFunctions.waitUntilSiteLoaded();

        log.debug("Selecting data set ...");
        dataSetRowSelectCheckbox.get().click();

        log.debug("Clicking REMOVE button ...");
        removeButton.get().click();

        log.debug("Confirming dependency mode ...");
        dependencyDialogOkButton.get().click();

        testFunctions.waitUntilSiteLoaded();

        // VERIFY
        testFunctions.waitUntilTrue(() -> !logicalSource.contains(datasetUuid), 10);
        if (logicalSource.contains(datasetUuid))
            throw new IllegalStateException(String.format("Un-assign operation unsuccessful on DB level.%sDataStock: %s%s Dataset uuid:%s", LINE_SEPARATOR, logicalSource, LINE_SEPARATOR, datasetUuid));
        log.trace("Done: Un-assigned dataset '{}' from {}", datasetUuid, logicalSource);
    }

    void delete_dataset(@Nonnull final UUID datasetUuid,
                        @Nonnull final Stock rootStock) {
        // ARRANGE
        final var deleteUrl = TestContext.PRIMARY_SITE_URL + String.format("admin/datasets/manageProcessList.xhtml?stock=%s", rootStock.getName());
        final var jobOverviewUrl = TestContext.PRIMARY_SITE_URL + "admin/showJobs.xhtml";

        // ### select data set (row) checkbox
        final Supplier<SelenideElement> dataSetRow = // the row which holds our data set
                () -> $x(String.format( // identify by peeking the uuid (and going back up again)
                        "//div[@id = 'processTable']" + // make sure we're in the right view
                                "//tr" +
                                "/td" +
                                "/div" +
                                "/span[contains(@id, 'nameColumnContentUuid') and text()='%s']" +
                                "/../../..", // (return the row ('tr') instead of the uuid label)
                        datasetUuid
                ));
        final Supplier<SelenideElement> dataSetRowSelectCheckbox =  // row select checkbox ---> click to select data set
                () -> dataSetRow.get().$x( // identify by class
                        "td[contains(concat(' ', @class, ' '), ' ui-selection-column ')]" +
                                "/div[contains(concat(' ', @class, ' '), ' ui-chkbox ')]"
                );

        // ### delete button
        final Supplier<SelenideElement> deleteButton = // the button to trigger the delete operation ---> click to trigger dependency dialog
                () -> $x( // identify by id
                        "//div[@id = 'tableWithControls']" + // make sure we're in the right view
                                "//button[@id = 'processTablebtn']"
                );

        // ### dependency dialog OK button
        final Supplier<SelenideElement> dependencyDialogOkButton = // the OK button in the dependency mode dialog ---> click to confirm order and trigger actual un-assign action
                () -> $x(String.format( // identify by peeking text
                        "//span[@id = 'processTabledeleteSelectedpanel']" +
                                "/button" +
                                "/span[contains(concat(' ', @class, ' '), 'ui-button-text') and text() = '%s']" +
                                "/..",
                        TestContext.lang.getProperty("admin.ok")
                ));

        // JOBS
        // ### last job state value
        final Supplier<SelenideElement> lastJobState = // the state of the job in the top row of the jobs table (considered to be the most recent job).
                () -> $x(
                        "//span[@id = 'showJobsTable:0:jobState']" // [table_id]:[row_number]:[column_value_id]
                );

        // ### deletion complete check
        final Supplier<Boolean> deletionComplete = () -> {
            try {
                driver.get(jobOverviewUrl);
                testFunctions.waitUntilSiteLoaded();
                if (JobState.COMPLETE.name().equals(lastJobState.get().getText()))
                    return true;

            } catch (Exception e) {
                return false;
            }
            return false;
        };


        // ACT
        log.debug("Going to page '{}' ...", deleteUrl);
        driver.get(deleteUrl);
        testFunctions.waitUntilSiteLoaded();

        log.debug("Selecting data set ...");
        dataSetRowSelectCheckbox.get().click();

        log.debug("Click delete ...");
        deleteButton.get().click();

        log.debug("Confirming dependency mode ...");
        dependencyDialogOkButton.get().sendKeys(Keys.ENTER);

        log.debug("Going to page '{}' ...", jobOverviewUrl);
        driver.get(jobOverviewUrl);
        testFunctions.waitUntilSiteLoaded();

        log.debug("Waiting until job state is '{}'", JobState.COMPLETE);
        testFunctions.waitUntilTrue(deletionComplete, 10);
        if (!deletionComplete.get())
            throw new IllegalStateException("Delete job ran into timeout.");

        testFunctions.waitUntilSiteLoaded();

        // VERIFY
        if (rootStock.contains(datasetUuid))
            throw new IllegalStateException(String.format("Delete operation unsuccessful on DB level.%sDataStock: %s%s Dataset uuid:%s", LINE_SEPARATOR, rootStock, LINE_SEPARATOR, datasetUuid));

        log.trace("Done: Deleted data set '{}'", datasetUuid);
    }

    void exportStockAsZip(final Stock stock) {
        // ARRANGE
        driver.get(TestContext.PRIMARY_SITE_URL + "admin/stocks/manageStockList.xhtml");
        final var tmpDirectoryFileCount = countFilesInDirectory(TMP_DIRECTORY);

        // ACT
        $x(String.format(
                "//tbody[@id='stockTable_data']" +
                        "/tr" +
                        "/td" +
                        "/a[text()='%s']" +
                        "/../../td/button/span[text()='%s']",
                stock.getName(), TestContext.lang.getProperty("admin.export.xml")
        )).click();// Export new (empty) root stock as xm

        testFunctions.waitUntilSiteLoaded();

        // VERIFY
        testFunctions.waitUntilTrue(() ->
                (1 >= countFilesInDirectory(TMP_DIRECTORY) - tmpDirectoryFileCount), 120);
        if (1 < countFilesInDirectory(TMP_DIRECTORY) - tmpDirectoryFileCount)
            throw new IllegalStateException(String.format("Download probably timed out: Failed to find new zip file within '%s'", TMP_DIRECTORY));
    }

    void exportStockAsCSV(final Stock stock) {
        // ARRANGE
        driver.get(TestContext.PRIMARY_SITE_URL + "admin/stocks/manageStockList.xhtml");
        long tmpDirectoryFileCount = countFilesInDirectory(TMP_DIRECTORY);

        // ACT
        $x(String.format(
                "//tbody[@id='stockTable_data']" +
                        "/tr" +
                        "/td" +
                        "/a[text()='%s']" +
                        "/../../td/button/span[text()='%s']", stock.getName(), TestContext.lang.getProperty("admin.export.csv")
        )).click();// Export new (empty) root stock as csv

        testFunctions.waitUntilSiteLoaded();

        // VERIFY
        testFunctions.waitUntilTrue(() ->
                (1 >= countFilesInDirectory(TMP_DIRECTORY) - tmpDirectoryFileCount), 120);
        if (1 < countFilesInDirectory(TMP_DIRECTORY) - tmpDirectoryFileCount)
            throw new IllegalStateException(String.format("Download probably timed out: Failed to find new zip file within '%s'", TMP_DIRECTORY));
    }

    ///////////
    // Utils //
    ///////////

    private void waitForImportToFinish() {
        switchTo().frame("console");
        testFunctions.findAndWaitOnConsoleElement(By.xpath("html/body[contains(.,'------ import of files finished ------')]"));
        if (!driver.getPageSource().contains("------ import of files finished ------"))
            throw new IllegalStateException(String.format("Import failed. For more details, see the log: %s%s", LINE_SEPARATOR, driver.getPageSource()));

        if (log.isTraceEnabled())
            log.trace(driver.getPageSource());
    }

    private long countFilesInDirectory(Path directory) {
        try (Stream<Path> pathS = Files.list(directory)) {
            return pathS.count();
        } catch (IOException ioe) {
            throw new RuntimeException(String.format("Failed to count files in dir '%s'", directory.toAbsolutePath()), ioe);
        }
    }

}
