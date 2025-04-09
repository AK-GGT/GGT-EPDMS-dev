package edu.kit.soda4lca.test.ui.admin.t151ExportCacheTest;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.testng.ScreenShooter;
import de.iai.ilcd.model.datastock.ExportType;
import de.iai.ilcd.webgui.controller.util.ExportMode;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.codeborne.selenide.Selenide.open;

/**
 * Testing the caching mechanism(s) for the export features<br/>
 * <br/>
 * <i>Note:</i>
 * Make sure to <i>deactivate caching</i> (i.e. eclipse-link)
 * when running this test.
 * (DB state is queried for some assertions)
 */
@Listeners({ScreenShooter.class})
public class T151ExportCacheTest extends AbstractUITest {

    static final Logger log = LogManager.getLogger(T151ExportCacheTest.class);

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Path TARGET_DIR = Paths.get("target", "tmp");
    private static final Path DUMMY_ZIP = TARGET_DIR.resolve("T151_tmp_dummy_cache_file.zip");
    private static final Path DUMMY_CSV_DOT = TARGET_DIR.resolve("T151_tmp_dummy_cache_file.csv");
    private static final Path DUMMY_CSV_COMMA = TARGET_DIR.resolve("T151_tmp_dummy_cache_file_C.csv");
    private static final Path TEST_DATA_ZIP = Paths.get("src", "test", "resources", "sample_epd_process_with_dependencies.zip");
    private static final Path INVALID_PATH = Paths.get("not", "a", "valid", "path");
    private static final UUID TEST_DATA_EPD_UUID = UUID.fromString("f3dafca9-7688-48ce-91f5-b77f6037e217");

    private Stock DEFAULT_STOCK;
    private Stock ROOT_API_IMPORT_TARGET;
    private Stock ROOT_UI_IMPORT_TARGET;
    private Stock LOGICAL_API_ASSIGN_TARGET;
    private Stock LOGICAL_UI_ASSIGN_TARGET;
    private Stock ROOT_UNMODIFIED_WITH_CACHE;
    private Stock ROOT_MODIFIED_WITH_CACHE;
    private Stock ROOT_MISSING_CACHE_FILE;
    private Stock ROOT_NO_CACHE;
    private Stock LOGICAL_UNMODIFIED_WITH_CACHE;
    private Stock LOGICAL_MODIFIED_WITH_CACHE;
    private Stock LOGICAL_MISSING_CACHE_FILE;
    private Stock LOGICAL_NO_CACHE;
    private String superAdminToken;

    private JdbcTemplate jdbcTemplate;

    private UI ui;

    private API api;

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return List.of(List.of("DB_pre_T151ExportCacheTest.xml")); // This dbunit file can't be generated
        // by other tests (as 'yDB_post_T...Test.xml' that is).
        // It has to be maintained manually.
        // Reason:
        // It's designed for the specific needs of this class.
    }

    @AfterClass
    public void afterClass() throws Exception {
        log.debug("Cleaning ...");
        Files.deleteIfExists(DUMMY_ZIP);
        Files.deleteIfExists(DUMMY_CSV_DOT);
        Files.deleteIfExists(DUMMY_CSV_COMMA);
        log.trace("Dummy files removed.");
    }

    @BeforeClass
    public void setup() throws Exception {
        super.setup();
        this.jdbcTemplate = super.newJDBCTemplate();

        WebDriverRunner.setWebDriver(driver);
        WebDriverRunner.clearBrowserCache();

        open(TestContext.PRIMARY_SITE_URL);

        this.api = new API(this.driver);
        this.ui = new UI(this.driver);

        // prepare test data
        if (Files.notExists(TARGET_DIR))
            Files.createDirectory(TARGET_DIR);
        Files.deleteIfExists(DUMMY_ZIP.toAbsolutePath());
        Files.copy(Paths.get("src", "test", "resources", "dummy_zip.zip").toAbsolutePath(), DUMMY_ZIP.toAbsolutePath());//        Files.createFile(DUMMY_ZIP.toAbsolutePath());
        Files.deleteIfExists(DUMMY_CSV_DOT.toAbsolutePath());
        Files.copy(Paths.get("src", "test", "resources", "dummy_csv_dot-separated.csv").toAbsolutePath(), DUMMY_CSV_DOT.toAbsolutePath());//Files.createFile(DUMMY_CSV_DOT.toAbsolutePath());
        Files.deleteIfExists(DUMMY_CSV_COMMA.toAbsolutePath());
        Files.copy(Paths.get("src", "test", "resources", "dummy_csv_comma-separated.csv").toAbsolutePath(), DUMMY_CSV_COMMA.toAbsolutePath());//Files.createFile(DUMMY_CSV_COMMA.toAbsolutePath());
        try {
            DEFAULT_STOCK = Stock.initFromDB("default", jdbcTemplate);
            ROOT_API_IMPORT_TARGET = Stock.initFromDB("root_api_import_target", jdbcTemplate);
            ROOT_UI_IMPORT_TARGET = Stock.initFromDB("root_ui_import_target", jdbcTemplate);
            LOGICAL_API_ASSIGN_TARGET = Stock.initFromDB("logical_api_assign_target", jdbcTemplate);
            LOGICAL_UI_ASSIGN_TARGET = Stock.initFromDB("logical_ui_assign_target", jdbcTemplate);
            ROOT_UNMODIFIED_WITH_CACHE = Stock.initFromDB("root_unmodified_with_cache", jdbcTemplate);
            ROOT_MODIFIED_WITH_CACHE = Stock.initFromDB("root_modified_with_cache", jdbcTemplate);
            ROOT_MISSING_CACHE_FILE = Stock.initFromDB("root_missing_cache_file", jdbcTemplate);
            ROOT_NO_CACHE = Stock.initFromDB("root_no_cache", jdbcTemplate);
            LOGICAL_UNMODIFIED_WITH_CACHE = Stock.initFromDB("logical_unmodified_with_cache", jdbcTemplate);
            LOGICAL_MODIFIED_WITH_CACHE = Stock.initFromDB("logical_modified_with_cache", jdbcTemplate);
            LOGICAL_MISSING_CACHE_FILE = Stock.initFromDB("logical_missing_cache_file", jdbcTemplate);
            LOGICAL_NO_CACHE = Stock.initFromDB("logical_no_cache", jdbcTemplate);
        } catch (IllegalStateException ise) {
            throw new IllegalStateException("Unexpected DB state", ise);
        }

        // db
        pointExistingCacheToDummyFiles(); // We want to store absolute paths, so will do this at runtime to not depend on some set-up or some platform.
        if (Files.exists(INVALID_PATH))
            throw new IllegalStateException(String.format("Expected path '%s' to NOT exist.", INVALID_PATH));

        // prepare for testing as super admin
        this.superAdminToken = api.generateSuperAdminJWT();
        ui.login_as_admin();
    }

    ////////////
    // TEST 1 //
    ////////////

    /**
     * Testing whether modifying state of data stocks via UI will lead to
     * export cache being invalidated.
     */
    @Test(priority = 1)
    public void assert_cache_invalidated_when_UI_modifying_stock_state() {
        log.info("Asserting that modifying stock state via UI invalidates cache ...");
        UI_import_zip_to_rootstock_asserting_cache_invalidation(TEST_DATA_ZIP, ROOT_UI_IMPORT_TARGET);
        UI_move_data_set_from_root_source_to_root_target_asserting_cache_invalidation(ROOT_UI_IMPORT_TARGET, TEST_DATA_EPD_UUID, DEFAULT_STOCK);
        UI_assign_data_set_to_logical_stock_asserting_cache_invalidation(DEFAULT_STOCK, TEST_DATA_EPD_UUID, LOGICAL_UI_ASSIGN_TARGET);
        UI_unassign_data_set_from_logical_stock_asserting_cache_invalidation(DEFAULT_STOCK, TEST_DATA_EPD_UUID, LOGICAL_UI_ASSIGN_TARGET);
        UI_delete_data_set_from_root_stock_asserting_cache_invalidation(TEST_DATA_EPD_UUID, DEFAULT_STOCK);
        log.debug("All good: UI modifications invalidate cache as expected!");
    }

    public void UI_import_zip_to_rootstock_asserting_cache_invalidation(@Nonnull final Path zipPath,
                                                                        @Nonnull final Stock rootStock) {
        log.debug("UI: Importing zip archive ...");
        log.trace("Zip: '{}'", zipPath);
        log.trace("Target: {}", rootStock);

        // ARRANGE
        rootStock.flag(ExportCacheFlag.NOT_MODIFIED);
        if (!rootStock.isUnmodified())
            throw new IllegalStateException(String.format("Unexpected DB state. %s is expected to be flagged unmodified for all export types but actually is flagged modified.", rootStock));

        // ACT
        ui.import_zip(zipPath, rootStock);

        // ASSERT
        Assert.assertTrue(String.format("After importing data sets to %s, it should have been flagged modified.", rootStock),
                rootStock.isModified());
        log.trace("Root target cache is invalidated.");

        log.trace("All good.");
    }

    public void UI_move_data_set_from_root_source_to_root_target_asserting_cache_invalidation(@Nonnull final Stock rootSource,
                                                                                              @Nonnull final UUID datasetUuid,
                                                                                              @Nonnull final Stock rootTarget) {
        log.debug("UI: Moving data set ...");
        log.trace("Source: {}", rootSource);
        log.trace("Target: {}", rootTarget);
        log.trace("Dataset uuid: '{}'", datasetUuid);

        // ARRANGE
        if (!rootSource.contains(datasetUuid))
            throw new IllegalStateException(String.format("%s is expected to contain process data set with uuid '%s'", rootSource, datasetUuid));

        rootSource.flag(ExportCacheFlag.NOT_MODIFIED);
        rootTarget.flag(ExportCacheFlag.NOT_MODIFIED);

        // ACT
        ui.move_dataset(rootSource, datasetUuid, rootTarget);

        // ASSERT
        Assert.assertTrue(String.format("After moving data set away from stock %s, it should have been flagged modified", rootSource),
                rootSource.isModified());
        log.trace("Root source stock cache is invalidated.");

        Assert.assertTrue(String.format("After moving data set to stock %s, it should have been flagged modified", rootTarget),
                rootTarget.isModified());
        log.trace("Root target stock cache is invalidated.");

        log.trace("All good.");
    }

    public void UI_assign_data_set_to_logical_stock_asserting_cache_invalidation(@Nonnull final Stock rootSource,
                                                                                 @Nonnull final UUID datasetUuid,
                                                                                 @Nonnull final Stock logicalTarget) {
        log.debug("UI: Assigning data set ...");
        log.trace("Source: {}", rootSource);
        log.trace("Target: {}", logicalTarget);
        log.trace("Dataset uuid: '{}'", datasetUuid);

        // ARRANGE
        if (!rootSource.contains(datasetUuid))
            throw new IllegalStateException(String.format("%s is expected to contain process data set with uuid '%s'", rootSource, datasetUuid));

        rootSource.flag(ExportCacheFlag.NOT_MODIFIED);
        logicalTarget.flag(ExportCacheFlag.NOT_MODIFIED);

        // ACT
        ui.assign_dataset(rootSource, datasetUuid, logicalTarget);

        // ASSERT
        Assert.assertTrue(String.format("After assigning data set stock %s, the affected stock should not have been flagged modified", rootSource),
                rootSource.isUnmodified());
        log.trace("Root cache remains valid.");

        Assert.assertTrue(String.format("After assigning data set to stock %s, the affected stock should have been flagged modified", logicalTarget),
                logicalTarget.isModified());
        log.trace("Logical target stock cache is invalidated.");

        log.trace("All good.");
    }

    public void UI_unassign_data_set_from_logical_stock_asserting_cache_invalidation(@Nonnull final Stock rootSourceStock,
                                                                                     @Nonnull final UUID datasetUuid,
                                                                                     @Nonnull final Stock logicalSourceStock) {
        log.debug("UI: Un-assigning data set ...");
        log.trace("Root source: {}", rootSourceStock);
        log.trace("Logical source: {}", logicalSourceStock);
        log.trace("Dataset uuid: '{}'", datasetUuid);

        // ARRANGE
        if (!rootSourceStock.contains(datasetUuid))
            throw new IllegalStateException(String.format("%s is expected to contain dataset '%s'", rootSourceStock, datasetUuid));
        if (!logicalSourceStock.contains(datasetUuid))
            throw new IllegalStateException(String.format("%s is expected to contain dataset '%s'", logicalSourceStock, datasetUuid));

        logicalSourceStock.flag(ExportCacheFlag.NOT_MODIFIED);
        rootSourceStock.flag(ExportCacheFlag.NOT_MODIFIED);

        // ACT
        ui.unassign_dataset(rootSourceStock, datasetUuid, logicalSourceStock);

        // ASSERT
        Assert.assertTrue("After un-assigning the data set, the affected stock should have been flagged modified.",
                logicalSourceStock.isModified());
        log.trace("Logical source stock cache invalidated.");

        Assert.assertTrue("After un-assigning data set from the logical stock, the root stock should not have been flagged modified",
                rootSourceStock.isUnmodified());
        log.trace("Root source stock cache remains valid.");

        log.trace("All good.");
    }


    public void UI_delete_data_set_from_root_stock_asserting_cache_invalidation(@Nonnull final UUID datasetUuid,
                                                                                @Nonnull final Stock rootStock) {
        log.debug("UI: Deleting data set ...");
        log.trace("Dataset uuid: '{}'", datasetUuid);
        log.trace("Root source stock: {}", rootStock);

        // ARRANGE
        if (!rootStock.contains(datasetUuid))
            throw new IllegalStateException(String.format("%s is expected to contain data set '%s'", rootStock, datasetUuid));

        var logicalStock = LOGICAL_UI_ASSIGN_TARGET;
        ui.assign_dataset(rootStock, datasetUuid, logicalStock); // We also want to check that the logical stocks get flagged as modified.

        logicalStock.flag(ExportCacheFlag.NOT_MODIFIED);
        rootStock.flag(ExportCacheFlag.NOT_MODIFIED);

        // ACT
        ui.delete_dataset(datasetUuid, rootStock);

        // ASSERT
        Assert.assertTrue("After deleting data set the root stock should be flagged modified",
                rootStock.isModified());
        log.trace("Root source stock cache is invalidated.");

        Assert.assertTrue("After deleting data set the associated logical stocks should be flagged modified",
                logicalStock.isModified());
        log.trace("Logical source stock cache is invalidated.");

        log.trace("All good.");
    }

    ////////////
    // TEST 2 //
    ////////////

    /**
     * Testing whether modifying state of data stocks via API will lead to
     * export cache being invalidated.
     */
    @Test(priority = 2)
    public void assert_cache_invalidated_when_API_modifying_stock_state() {
        log.info("Asserting that modifying stock state via API invalidates cache ...");
        API_import_zip_to_rootstock_asserting_cache_invalidation(TEST_DATA_ZIP, ROOT_API_IMPORT_TARGET);
        API_assign_data_set_to_logical_stock_asserting_cache_invalidation(ROOT_API_IMPORT_TARGET, TEST_DATA_EPD_UUID, LOGICAL_API_ASSIGN_TARGET);
        API_unassign_data_set_from_logical_stock_asserting_cache_invalidation(ROOT_API_IMPORT_TARGET, TEST_DATA_EPD_UUID, LOGICAL_API_ASSIGN_TARGET);

        ui.delete_dataset(TEST_DATA_EPD_UUID, ROOT_API_IMPORT_TARGET); // There's no API endpoint for this (as of now)
        log.debug("All good: API modifications invalidate cache as expected!");
    }

    private void API_import_zip_to_rootstock_asserting_cache_invalidation(@Nonnull final Path zipPath,
                                                                          @Nonnull final Stock rootTarget) {
        log.debug("API: Importing ...");
        log.trace("Zip: '{}'", zipPath);
        log.trace("Target: {}", rootTarget);

        // ARRANGE
        if (!Files.exists(zipPath))
            throw new IllegalStateException("Test file does not exist.");
        rootTarget.flag(ExportCacheFlag.NOT_MODIFIED);
        log.trace("Parameters ok.");

        // ACT
        final ResponseEntity<String> response = api.importZip(zipPath, rootTarget, superAdminToken);

        // ASSERT
        final int status = response.getStatusCodeValue();
        final var msg = response.getBody();
        if (status != 202)
            throw new RuntimeException(String.format("Zip import failed (API) %sStatus: %d %sMessage: %s", LINE_SEPARATOR, status, LINE_SEPARATOR, msg));
        testFunctions.waitUntilTrue(() -> rootTarget.contains(TEST_DATA_EPD_UUID), 120);
        if (!rootTarget.contains(TEST_DATA_EPD_UUID))
            throw new IllegalStateException(String.format("Zip import apparently failed (API): Data set '%s' is not present in %s (after extended wait period) %sStatus: %d %sMessage: %s", TEST_DATA_EPD_UUID, rootTarget, LINE_SEPARATOR, status, LINE_SEPARATOR, msg));

        Assert.assertTrue("Target stock is expected to be flagged as modified.",
                rootTarget.isModified());
        log.trace("Target stock cache invalidated.");

        log.trace("All good.");
    }

    private void API_assign_data_set_to_logical_stock_asserting_cache_invalidation(@Nonnull final Stock rootSource,
                                                                                   @Nonnull final UUID datasetUuid,
                                                                                   @Nonnull final Stock logicalTarget) {
        log.debug("API: Assigning ... ");
        log.trace("Root source: {}", rootSource);
        log.trace("Logical target: {}", logicalTarget);
        log.trace("Dataset uuid: '{}'", datasetUuid);


        // ARRANGE
        if (!rootSource.contains(datasetUuid))
            throw new IllegalStateException(String.format("Data set '%s' should be present in %s", datasetUuid, rootSource));
        if (logicalTarget.contains(datasetUuid))
            throw new IllegalStateException(String.format("Data set '%s' should not be contained in %s", datasetUuid, rootSource));
        rootSource.flag(ExportCacheFlag.NOT_MODIFIED);
        logicalTarget.flag(ExportCacheFlag.NOT_MODIFIED);
        log.trace("Parameters ok.");

        // ACT
        final ResponseEntity<String> response = api.assignProcess(datasetUuid, logicalTarget, superAdminToken);

        // ASSERT
        log.debug("Assigning ...");
        final int status = response.getStatusCodeValue();
        final var msg = response.getBody();
        if (status != 200)
            throw new RuntimeException(String.format("Failed to assign '%s' to %s (via API) %sStatus: %d %sMessage: %s", datasetUuid, logicalTarget, LINE_SEPARATOR, status, LINE_SEPARATOR, msg));
        testFunctions.waitUntilTrue(() -> logicalTarget.contains(datasetUuid), 10);
        if (!logicalTarget.contains(datasetUuid))
            throw new IllegalStateException(String.format("Failed to assign '%s' to %s (via API): Data set not contained in target (after extended wait period) %sStatus: %d %sMessage: %s", datasetUuid, logicalTarget, LINE_SEPARATOR, status, LINE_SEPARATOR, msg));

        log.trace("Assignment done and verified.");

        Assert.assertTrue("Logical target stock should be flagged as modified after assigning data set.",
                logicalTarget.isModified());
        log.trace("Logical target stock cache invalidated.");

        Assert.assertTrue("Root source stock should not be flagged modified after assigning data set to logical stock.",
                rootSource.isUnmodified());
        log.trace("Root source stock cache remains valid.");

        log.trace("All good.");
    }

    private void API_unassign_data_set_from_logical_stock_asserting_cache_invalidation(@Nonnull final Stock rootSource,
                                                                                       @Nonnull final UUID datasetUuid,
                                                                                       @Nonnull final Stock logicalSource) {
        log.debug("API: Un-assigning ...");
        log.trace("Root source: {}", rootSource);
        log.trace("Logical source: {}", logicalSource);
        log.trace("Dataset uuid: '{}'", datasetUuid);

        // ARRANGE
        if (!rootSource.contains(datasetUuid))
            throw new IllegalStateException(String.format("Data set '%s' should be present in %s", datasetUuid, rootSource));
        if (!logicalSource.contains(datasetUuid))
            throw new IllegalStateException(String.format("Data set '%s' should be contained in %s", datasetUuid, logicalSource));
        rootSource.flag(ExportCacheFlag.NOT_MODIFIED);
        logicalSource.flag(ExportCacheFlag.NOT_MODIFIED);
        log.trace("Parameters ok.");

        // ACT
        final ResponseEntity<String> response = api.unassignProcess(datasetUuid, logicalSource, superAdminToken);

        // ASSERT
        final int status = response.getStatusCodeValue();
        final var msg = response.getBody();
        if (status != 200)
            throw new RuntimeException(String.format("Failed to un-assign '%s' from %s (via API) %sStatus: %d %sMessage: %s", datasetUuid, logicalSource, LINE_SEPARATOR, status, LINE_SEPARATOR, msg));
        testFunctions.waitUntilTrue(() -> !logicalSource.contains(datasetUuid), 10);
        if (logicalSource.contains(datasetUuid))
            throw new IllegalStateException(String.format("Failed to un-assign '%s' from %s (via API): Data set not present in target (after extended wait period) %sStatus: %d %sMessage: %s", datasetUuid, logicalSource, LINE_SEPARATOR, status, LINE_SEPARATOR, msg));

        log.trace("Un-assignment done and verified.");

        Assert.assertTrue("Logical target stock should be flagged as modified after un-assigning data set.",
                logicalSource.isModified());
        log.trace("Logical source stock cache invalidated.");

        Assert.assertTrue("Root source stock should not be flagged modified after un-assigning data set from logical stock.",
                rootSource.isUnmodified());
        log.trace("Root source stock cache remains valid.");

        log.debug("All good.");
    }

    ////////////
    // TEST 3 //
    ////////////

    /**
     * Testing whether a modified stock is flagged unmodified after export via UI
     */
    @Test(priority = 3)
    public void assert_stock_flagged_unmodified_when_UI_exported() {
        log.info("Asserting that exporting modified stocks via UI flags them unmodified ...");

        // ARRANGE
        final var rootTestStock = ROOT_UI_IMPORT_TARGET;
        final var logicalTestStock = LOGICAL_UI_ASSIGN_TARGET;

        rootTestStock.flag(ExportCacheFlag.MODIFIED);
        logicalTestStock.flag(ExportCacheFlag.MODIFIED);

        // ACT
        log.debug("Exporting ...");
        ui.exportStockAsZip(rootTestStock);
        ui.exportStockAsCSV(rootTestStock);

        ui.exportStockAsZip(logicalTestStock);
        ui.exportStockAsCSV(logicalTestStock);

        // ASSERT
        if (ExportType.values().length != 3)
            throw new UnsupportedOperationException("This test is not currently not suited to test all relevant cases. Add or remove missing."); // I'm sorry

        Assert.assertFalse("Root stock should be flagged unmodified after UI export",
                rootTestStock.isModified(ExportMode.LATEST_ONLY_GLOBAL, ExportType.ZIP, ExportType.CSV_EPD)); // note: Stock::isUnmodified will not work here! Not all tags are unmodified -- just 2 are...
        log.trace("Root test stock is flagged unmodified.");

        Assert.assertFalse("Logical stock should be flagged unmodified after UI export",
                logicalTestStock.isModified(ExportMode.LATEST_ONLY_GLOBAL, ExportType.ZIP, ExportType.CSV_EPD));
        log.trace("Logical test stock is flagged unmodified.");

        log.debug("All good: UI-export has flagged the stocks unmodified.");
    }

    ////////////
    // TEST 4 //
    ////////////

    /**
     * Testing whether a modified stock is flagged unmodified after export via API
     */
    @Test(priority = 4)
    public void assert_stock_flagged_unmodified_when_API_exported() {
        log.info("Asserting that exporting modified stocks via API flags them unmodified ...");

        // ARRANGE
        final var rootTestStock = ROOT_API_IMPORT_TARGET;
        final var logicalTestStock = LOGICAL_API_ASSIGN_TARGET;

        rootTestStock.flag(ExportCacheFlag.MODIFIED);
        logicalTestStock.flag(ExportCacheFlag.MODIFIED);

        // ACT
        log.debug("Requesting/Exporting both logical and root stock in all export types (via API) ...");
        log.trace("Root stock as zip ...");
        final var rootZipStatus = api.requestStockAsZip(rootTestStock, superAdminToken).getStatusCodeValue();
        if (rootZipStatus != 200)
            throw new RuntimeException(String.format("Unexpected status code when requesting %s as ZIP.", rootTestStock));

        log.trace("Logical stock as zip ...");
        final var logicalZipStatus = api.requestStockAsZip(logicalTestStock, superAdminToken).getStatusCodeValue();
        if (logicalZipStatus != 200)
            throw new RuntimeException(String.format("Unexpected status code when requesting %s as ZIP.", logicalTestStock));

        log.trace("Root stock as dot-separated csv ...");
        final var rootDotCSVStatus = api.requestAsCsv(rootTestStock, DecimalSeparator.DOT, superAdminToken).getStatusCodeValue();
        if (rootDotCSVStatus != 200)
            throw new RuntimeException(String.format("Unexpected status code when requesting %s as dot-separated CSV.", rootTestStock));

        log.trace("Logical stock as dot-separated csv ...");
        final var logicalDotCSVStatus = api.requestAsCsv(logicalTestStock, DecimalSeparator.DOT, superAdminToken).getStatusCodeValue();
        if (logicalDotCSVStatus != 200)
            throw new RuntimeException(String.format("Unexpected status code when requesting %s as dot-separated CSV.", logicalTestStock));

        log.trace("Root stock as comma-separated csv ...");
        final var rootCommaCSVStatus = api.requestAsCsv(rootTestStock, DecimalSeparator.COMMA, superAdminToken).getStatusCodeValue();
        if (rootCommaCSVStatus != 200)
            throw new RuntimeException(String.format("Unexpected status code when requesting %s as comma-separated CSV.", rootTestStock));

        log.trace("Logical stock as comma-separated csv ...");
        final var logicalCommaCSVStatus = api.requestAsCsv(logicalTestStock, DecimalSeparator.COMMA, superAdminToken).getStatusCodeValue();
        if (logicalCommaCSVStatus != 200)
            throw new RuntimeException(String.format("Unexpected status code when requesting %s as comma-separated CSV.", rootTestStock));

        log.trace("Export done and verified.");

        // ASSERT
        if (ExportType.values().length != 3)
            throw new UnsupportedOperationException("This test is not currently not suited to test all relevant cases. Add or remove missing."); // I'm sorry

        Assert.assertTrue("Root stock should be flagged unmodified after API export",
                rootTestStock.isUnmodified(ExportMode.LATEST_ONLY));
        log.trace("Root test stock is flagged unmodified");

        Assert.assertTrue("Logical stock should be flagged unmodified after API export",
                logicalTestStock.isUnmodified(ExportMode.LATEST_ONLY));
        log.trace("Logical test stock is flagged unmodified.");

        log.debug("All good: API-export has flagged the stocks unmodified.");
    }

    ////////////
    // Test 5 //
    ////////////

    /**
     * Feature test: Previously exported, unmodified data stocks should make use of cache
     */
    @Test(priority = 5, dependsOnMethods = {"assert_stock_flagged_unmodified_when_UI_exported", "assert_stock_flagged_unmodified_when_API_exported"})
    public void assert_unmodified_stocks_use_cache_for_export() throws IOException {
        log.info("Feature test: Asserting that previously exported, unmodified data stocks use cache for export ...");
        assert_unmodified_previously_cached_stock_uses_cache_for_export(LOGICAL_UNMODIFIED_WITH_CACHE);
        assert_unmodified_previously_cached_stock_uses_cache_for_export(ROOT_UNMODIFIED_WITH_CACHE);
        log.debug("All good: Cache is used for cached stocks!");
    }

    private void assert_unmodified_previously_cached_stock_uses_cache_for_export(Stock stock) throws IOException {
        log.debug("Exporting ...");
        log.trace("Stock: {}", stock);

        // ARRANGE
        for (ExportMode mode : ExportMode.values()) {
            for (ExportType type : ExportType.values()) {
                if (!stock.hasTag(mode, type))
                    throw new IllegalStateException(String.format("Stock is expected to have cache for all export types. Please check and adjust dbunit xml. %sMode: '%s' (%d) %sType: '%s' (%d) %sStock: %s", LINE_SEPARATOR, mode.name(), mode.ordinal(), LINE_SEPARATOR, type.name(), type.ordinal(), LINE_SEPARATOR, stock));
            }
        }

        log.debug("Checking cache before export ...");
        final var cacheZipPath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.ZIP));
        final var cacheZipMd5 = testFunctions.md5ForFile(cacheZipPath);
        final var cacheDotCSVPath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD));
        final var cacheCommaCSVPath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD_C));
        final var cacheDotCSVMd5 = Objects.requireNonNull(testFunctions.md5ForFile(cacheDotCSVPath));
        final var cacheCommaCSVMd5 = Objects.requireNonNull(testFunctions.md5ForFile(cacheCommaCSVPath));
        log.trace("Looking good.");

        // ACT
        log.debug("Exporting ...");
        log.trace("Exporting zip ...");
        final var exportZipMd5 = testFunctions.md5(Objects.requireNonNull(api.requestStockAsZip(stock, superAdminToken).getBody()).getBytes(StandardCharsets.ISO_8859_1));
        final var newZipCachePath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.ZIP));
        final var newZipCacheMd5 = testFunctions.md5ForFile(newZipCachePath);

        log.trace("Exporting dot separated csv ...");
        final var exportDotCSVMdd5 = testFunctions.md5(Objects.requireNonNull(api.requestAsCsv(stock, DecimalSeparator.DOT, superAdminToken).getBody()).getBytes(StandardCharsets.UTF_8));
        final var newDotCSVCachePath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD));
        final var newDotCSVCacheMd5 = testFunctions.md5ForFile(newDotCSVCachePath);

        log.trace("Exporting comma separated csv ...");
        final var exportCommaCSVMdd5 = testFunctions.md5(Objects.requireNonNull(api.requestAsCsv(stock, DecimalSeparator.COMMA, superAdminToken).getBody()).getBytes(StandardCharsets.UTF_8));
        final var newCommaCSVCachePath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD_C));
        final var newCommaCSVCacheMd5 = testFunctions.md5ForFile(newCommaCSVCachePath);

        log.trace("Exports done.");

        // ASSERT
        if (ExportType.values().length != 3)
            throw new UnsupportedOperationException("This test is not currently not suited to test all relevant cases. Add or remove missing."); // I'm sorry

        log.trace("Compare checksums and cache paths ...");
        Assert.assertEquals("Checksums of cache and export must match. (ZIP)", cacheZipMd5, exportZipMd5);
        Assert.assertEquals("Checksums of old cache and new cache must match. (ZIP)", cacheZipMd5, newZipCacheMd5);
        Assert.assertEquals("Path of old cache should still be valid. (ZIP)", cacheZipPath, newZipCachePath);
        log.trace("Ok: ZIP export.");

        Assert.assertEquals("Checksums of cache and export must match. (Dot-separated CSV)", cacheDotCSVMd5, exportDotCSVMdd5);
        Assert.assertEquals("Checksums of old cache and new cache must match. (Dot-separated CSV)", cacheDotCSVMd5, newDotCSVCacheMd5);
        Assert.assertEquals("Path of old cache should still be valid. (Dot-separated CSV)", cacheDotCSVPath, newDotCSVCachePath);
        log.trace("OK: Dot-separated CSV export.");

        Assert.assertEquals("Checksums of cache and export must match. (Dot-separated CSV)", cacheCommaCSVMd5, exportCommaCSVMdd5);
        Assert.assertEquals("Checksums of old cache and new cache must match. (Dot-separated CSV)", cacheCommaCSVMd5, newCommaCSVCacheMd5);
        Assert.assertEquals("Path of old cache should still be valid. (Dot-separated CSV)", cacheCommaCSVPath, newCommaCSVCachePath);
        log.trace("OK: Comma-separated CSV export.");

        log.trace("All good.");
    }

    ////////////
    // TEST 6 //
    ////////////

    /**
     * Assert that modified stocks are being exported with the new state and update the cache.
     */
    @Test(priority = 6, dependsOnMethods = {"assert_stock_flagged_unmodified_when_UI_exported", "assert_stock_flagged_unmodified_when_API_exported"})
    public void assert_modified_data_stocks_are_exported_fresh_and_produce_cache() throws IOException {
        log.info("Asserting that modified stocks are exported fresh and produce cache ...");
        assert_modified_previously_cached_stock_is_exported_fresh_and_produce_cache(ROOT_MODIFIED_WITH_CACHE);
        assert_modified_previously_cached_stock_is_exported_fresh_and_produce_cache(LOGICAL_MODIFIED_WITH_CACHE);
        log.debug("All good: Modified stocks are not using cache!");
    }

    private void assert_modified_previously_cached_stock_is_exported_fresh_and_produce_cache(Stock stock) throws IOException {
        log.debug("Exporting ...");
        log.trace("Stock: {}", stock);

        // ARRANGE
        for (ExportMode mode : ExportMode.values()) {
            for (ExportType type : ExportType.values()) {
                if (!stock.hasTag(mode, type))
                    throw new IllegalStateException(String.format("Stock is expected to have cache for all export types. Please check and adjust dbunit xml. %sMode: '%s' (%d) %sType: '%s' (%d) %sStock: %s", LINE_SEPARATOR, mode.name(), mode.ordinal(), LINE_SEPARATOR, type.name(), type.ordinal(), LINE_SEPARATOR, stock));
            }
        }
        log.debug("Checking cache before export ...");
        final var cacheZipPath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.ZIP));
        final var cacheDotCSVPath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD));
        final var cacheCommaCSVPath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD_C));
        final var cacheZipMd5 = Objects.requireNonNull(testFunctions.md5ForFile(cacheZipPath));
        final var cacheDotCSVMd5 = Objects.requireNonNull(testFunctions.md5ForFile(cacheDotCSVPath));
        final var cacheCommaCSVMd5 = Objects.requireNonNull(testFunctions.md5ForFile(cacheCommaCSVPath));
        log.trace("Looking good.");

        // ACT
        log.debug("Exporting ...");
        log.trace("Exporting zip ...");
        final var exportZipMd5 = testFunctions.md5(Objects.requireNonNull(api.requestStockAsZip(stock, superAdminToken).getBody()).getBytes(StandardCharsets.ISO_8859_1));
        final var newZipCachePath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.ZIP));
        final var newZipCacheMd5 = testFunctions.md5ForFile(newZipCachePath);

        log.trace("Exporting dot separated csv ...");
        final var exportDotCSVMdd5 = testFunctions.md5(Objects.requireNonNull(api.requestAsCsv(stock, DecimalSeparator.DOT, superAdminToken).getBody()).getBytes(StandardCharsets.UTF_8));
        final var newDotCSVCachePath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD));
        final var newDotCSVCacheMd5 = testFunctions.md5ForFile(newDotCSVCachePath);

        log.trace("Exporting comma separated csv ...");
        final var exportCommaCSVMdd5 = testFunctions.md5(Objects.requireNonNull(api.requestAsCsv(stock, DecimalSeparator.COMMA, superAdminToken).getBody()).getBytes(StandardCharsets.UTF_8));
        final var newCommaCSVCachePath = Objects.requireNonNull(stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD_C));
        final var newCommaCSVCacheMd5 = testFunctions.md5ForFile(newCommaCSVCachePath);

        log.trace("Exports done.");

        // ASSERT
        if (ExportType.values().length != 3)
            throw new UnsupportedOperationException("This test is not currently not suited to test all relevant cases. Add or remove missing."); // I'm sorry

        log.trace("Compare checksums and cache paths ...");
        Assert.assertNotEquals("Checksums of cache and export must not match. (ZIP)", cacheZipMd5, exportZipMd5);
        Assert.assertNotEquals("Checksums of old cache and new cache must not match. (ZIP)", cacheZipMd5, newZipCacheMd5);
        Assert.assertNotEquals("Path of old cache should be updated. (ZIP)", cacheZipPath, newZipCachePath);
        log.trace("OK: ZIP export.");

        Assert.assertNotEquals("Checksums of cache and export must not match. (Dot-separated CSV)", cacheDotCSVMd5, exportDotCSVMdd5);
        Assert.assertNotEquals("Checksums of old cache and new cache must not match. (Dot-separated CSV)", cacheDotCSVMd5, newDotCSVCacheMd5);
        Assert.assertNotEquals("Path of old cache should be updated. (Dot-separated CSV)", cacheDotCSVPath, newDotCSVCachePath);
        log.trace("OK: Dot-separated CSV export.");

        Assert.assertNotEquals("Checksums of cache and export must not match. (Comma-separated CSV)", cacheCommaCSVMd5, exportCommaCSVMdd5);
        Assert.assertNotEquals("Checksums of old cache and new cache must not match. (Comma-separated CSV)", cacheCommaCSVMd5, newCommaCSVCacheMd5);
        Assert.assertNotEquals("Path of old cache should be updated. (Comma-separated CSV)", cacheCommaCSVPath, newCommaCSVCachePath);
        log.trace("OK: Comma-separated CSV export.");

        log.trace("All good.");
    }

    ////////////
    // TEST 7 //
    ////////////

    /**
     * Assert that stocks are being exported and produce cache even when they don't have any export tags
     * (= they've never been exported before).
     */
    @Test(priority = 7, dependsOnMethods = {"assert_stock_flagged_unmodified_when_UI_exported", "assert_stock_flagged_unmodified_when_API_exported"})
    public void assert_not_previously_exported_data_stocks_are_exported_and_produce_cache() throws IOException {
        log.info("Asserting that new stocks are exported fresh and produce cache ...");
        assert_not_previously_exported_stock_is_exported_and_produce_cache(ROOT_NO_CACHE);
        assert_not_previously_exported_stock_is_exported_and_produce_cache(LOGICAL_NO_CACHE);
        log.debug("All good: New stocks are exported fresh and update cache!");
    }

    private void assert_not_previously_exported_stock_is_exported_and_produce_cache(Stock stock) throws IOException {
        log.debug("Exporting ...");
        log.trace("Stock: {}", stock);

        // ARRANGE
        for (ExportMode mode : ExportMode.values()) {
            for (ExportType type : ExportType.values()) {
                if (stock.hasTag(mode, type))
                    throw new IllegalStateException(String.format("Stock is expected to not have cache for any export types. Please check and adjust dbunit xml. %sMode: '%s' (%d) %sType: '%s' (%d) %sStock: %s", LINE_SEPARATOR, mode.name(), mode.ordinal(), LINE_SEPARATOR, type.name(), type.ordinal(), LINE_SEPARATOR, stock));
            }
        }

        // ACT (+ some assertions)
        log.debug("Exporting ...");
        log.trace("Exporting zip ...");
        final var exportZipMd5 = testFunctions.md5(Objects.requireNonNull(api.requestStockAsZip(stock, superAdminToken).getBody()).getBytes(StandardCharsets.ISO_8859_1));
        final var zipCachePath = stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.ZIP);
        Assert.assertNotNull("Zip cache path should be present in db after export.", zipCachePath);
        Assert.assertTrue("Zip cache file should exist after export.",
                Files.exists(zipCachePath));
        final var zipCacheMd5 = testFunctions.md5ForFile(zipCachePath);

        log.trace("Exporting dot-separated csv ...");
        final var exportDotCSVMdd5 = testFunctions.md5(Objects.requireNonNull(api.requestAsCsv(stock, DecimalSeparator.DOT, superAdminToken).getBody()).getBytes(StandardCharsets.ISO_8859_1));
        final var dotCSVCachePath = stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD);
        Assert.assertNotNull("Dot-separated CSV cache path should be present in db after export.", dotCSVCachePath);
        Assert.assertTrue("Dot-separated CSV cache file should exist after export.",
                Files.exists(dotCSVCachePath));
        final var dotCSVCacheMd5 = testFunctions.md5ForFile(dotCSVCachePath);

        log.trace("Exporting comma-separated csv ...");
        final var exportCommaCSVMdd5 = testFunctions.md5(Objects.requireNonNull(api.requestAsCsv(stock, DecimalSeparator.COMMA, superAdminToken).getBody()).getBytes(StandardCharsets.ISO_8859_1));
        final var commaCSVCachePath = stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD_C);
        Assert.assertNotNull("Comma-separated CSV cache path should be present in db after export.", commaCSVCachePath);
        Assert.assertTrue("Comma-separated CSV cache file should exist after export.",
                Files.exists(commaCSVCachePath));
        final var commaCSVCacheMd5 = testFunctions.md5ForFile(commaCSVCachePath);

        log.trace("Exports done.");

        // ASSERT
        if (ExportType.values().length != 3)
            throw new UnsupportedOperationException("This test is not currently not suited to test all relevant cases. Add or remove missing."); // I'm sorry

        log.trace("Compare checksums and cache paths ...");
        Assert.assertEquals("Checksums of cache and export must match. (ZIP)", zipCacheMd5, exportZipMd5);
        log.trace("OK: ZIP export.");

        Assert.assertEquals("Checksums of cache and export must match. (Dot-separated CSV)", dotCSVCacheMd5, exportDotCSVMdd5);
        log.trace("OK: Dot-separated CSV export.");

        Assert.assertEquals("Checksums of cache and export must match. (Comma-separated CSV)", commaCSVCacheMd5, exportCommaCSVMdd5);
        log.trace("OK: Comma-separated CSV export.");

        log.trace("All good.");
    }

    ////////////
    // Test 8 //
    ////////////

    /**
     * Assert that previously exported stocks (=they have export tags) are exported and produce cache even when
     * they are pointing to a non-existing cache file or to none at all.
     */
    @Test(priority = 8, dependsOnMethods = {"assert_stock_flagged_unmodified_when_UI_exported", "assert_stock_flagged_unmodified_when_API_exported"})
    public void assert_data_stocks_with_missing_cache_file_are_exported_and_produce_cache() throws IOException {
        log.info("Asserting that stocks with missing cache file are exported fresh and produce cache ...");
        assert_stock_with_missing_cache_file_are_exported_and_produce_cache(ROOT_MISSING_CACHE_FILE);
        assert_stock_with_missing_cache_file_are_exported_and_produce_cache(LOGICAL_MISSING_CACHE_FILE);
        log.debug("All good: Stocks without cache file are exported fresh and produce cache.");
    }

    private void assert_stock_with_missing_cache_file_are_exported_and_produce_cache(Stock stock) throws IOException {
        log.debug("Exporting ...");
        log.trace("Stock: {}", stock);

        // ARRANGE
        for (ExportMode mode : ExportMode.values()) {
            for (ExportType type : ExportType.values()) {
                if (!stock.hasTag(mode, type))
                    throw new IllegalStateException(String.format("Stock is expected to have all export tags. Please check and adjust dbunit xml. %sMode: '%s' (%d) %sType: '%s' (%d) %sStock: %s", LINE_SEPARATOR, mode.name(), mode.ordinal(), LINE_SEPARATOR, type.name(), type.ordinal(), LINE_SEPARATOR, stock));
                final var tmpPath = stock.getCurrentExportCacheFilePath(mode, type);
                if (!(tmpPath == null || tmpPath.toString().isBlank() || INVALID_PATH.equals(tmpPath)))
                    throw new IllegalStateException(String.format("Stock is expected to lack a cache file. %sMode: '%s' (%d) %sType: '%s' (%d) %sStock: %s", LINE_SEPARATOR, mode.name(), mode.ordinal(), LINE_SEPARATOR, type.name(), type.ordinal(), LINE_SEPARATOR, stock));
            }
        }

        // ACT (+ some assertions)
        log.debug("Exporting ...");
        log.trace("Exporting zip ...");
        final var exportZipMd5 = testFunctions.md5(Objects.requireNonNull(api.requestStockAsZip(stock, superAdminToken).getBody()).getBytes(StandardCharsets.ISO_8859_1));
        final var zipCachePath = stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.ZIP);
        Assert.assertNotNull("Zip cache path should be present in db after export.", zipCachePath);
        Assert.assertTrue("Zip cache file should exist after export.",
                Files.exists(zipCachePath));
        final var zipCacheMd5 = testFunctions.md5ForFile(zipCachePath);

        log.trace("Exporting dot-separated csv ...");
        final var exportDotCSVMdd5 = testFunctions.md5(Objects.requireNonNull(api.requestAsCsv(stock, DecimalSeparator.DOT, superAdminToken).getBody()).getBytes(StandardCharsets.ISO_8859_1));
        final var dotCSVCachePath = stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD);
        Assert.assertNotNull("Dot-separated CSV cache path should be present in db after export.", dotCSVCachePath);
        Assert.assertTrue("Dot-separated CSV cache file should exist after export.",
                Files.exists(dotCSVCachePath));
        final var dotCSVCacheMd5 = testFunctions.md5ForFile(dotCSVCachePath);

        log.trace("Exporting comma-separated csv ...");
        final var exportCommaCSVMdd5 = testFunctions.md5(Objects.requireNonNull(api.requestAsCsv(stock, DecimalSeparator.COMMA, superAdminToken).getBody()).getBytes(StandardCharsets.ISO_8859_1));
        final var commaCSVCachePath = stock.getCurrentExportCacheFilePath(ExportMode.LATEST_ONLY, ExportType.CSV_EPD_C);
        Assert.assertNotNull("Comma-separated CSV cache path should be present in db after export.", commaCSVCachePath);
        Assert.assertTrue("Comma-separated CSV cache file should exist after export.",
                Files.exists(commaCSVCachePath));
        final var commaCSVCacheMd5 = testFunctions.md5ForFile(commaCSVCachePath);

        log.trace("Exports done.");

        // ASSERT
        if (ExportType.values().length != 3)
            throw new UnsupportedOperationException("This test is not currently not suited to test all relevant cases. Add or remove missing."); // I'm sorry

        log.trace("Compare checksums and cache paths ...");
        Assert.assertEquals("Checksums of cache and export must match. (ZIP)", zipCacheMd5, exportZipMd5);
        log.trace("OK: ZIP export.");

        Assert.assertEquals("Checksums of cache and export must match. (Dot-separated CSV)", dotCSVCacheMd5, exportDotCSVMdd5);
        log.trace("OK: Dot-separated CSV export.");

        Assert.assertEquals("Checksums of cache and export must match. (Comma-separated CSV)", commaCSVCacheMd5, exportCommaCSVMdd5);
        log.trace("OK: Comma-separated CSV export.");

        log.trace("All good.");
    }


    ////////////
    // Utils //
    ///////////

    private void pointExistingCacheToDummyFiles() {
        log.debug("DB update: Pointing existing cache to dummy files.");

        var q = String.format(
                "UPDATE `datastock_export_tag`" +
                        " SET `file` = '%s'" +
                        " WHERE `type` = %d " +
                        " AND `file` != ''" +
                        " AND `ID` > -1",
                DUMMY_ZIP.toAbsolutePath(), ExportType.ZIP.ordinal()
        );
        jdbcTemplate.update(q);
        log.trace("{} export tags in DB now all point to file '{}'", ExportType.ZIP.name(), DUMMY_ZIP.toAbsolutePath());

        q = String.format(
                "UPDATE `datastock_export_tag`" +
                        " SET `file` = '%s'" +
                        " WHERE `type` = %d" +
                        " AND `file` != ''" +
                        " AND `ID` > -1",
                DUMMY_CSV_DOT.toAbsolutePath(), ExportType.CSV_EPD.ordinal());
        jdbcTemplate.update(q);
        log.trace("{} export tags in DB now all point to file '{}'", ExportType.CSV_EPD.name(), DUMMY_CSV_DOT.toAbsolutePath());

        q = String.format(
                "UPDATE `datastock_export_tag`" +
                        " SET `file` = '%s'" +
                        " WHERE `type` = %d" +
                        " AND `file` != ''" +
                        " AND `ID` > -1",
                DUMMY_CSV_COMMA.toAbsolutePath(), ExportType.CSV_EPD_C.ordinal());
        jdbcTemplate.update(q);
        log.trace("{} export tags in DB now all point to file '{}'", ExportType.CSV_EPD_C.name(), DUMMY_CSV_COMMA.toAbsolutePath());

        q = "UPDATE `datastock_export_tag`" +
                " SET `file` = 'not/a/valid/path'" +
                " WHERE `datastock_id` = 12";
        jdbcTemplate.update(q);
        log.trace("{} export tags in DB now all point to file '{}'", ExportType.CSV_EPD_C.name(), DUMMY_CSV_COMMA.toAbsolutePath());
    }

}
