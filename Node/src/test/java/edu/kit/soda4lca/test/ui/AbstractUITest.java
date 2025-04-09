package edu.kit.soda4lca.test.ui;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import edu.kit.soda4lca.test.ui.main.TestContext;
import edu.kit.soda4lca.test.ui.main.TestFunctions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.IOperationListener;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSet;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ExcludeTableFilter;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.dbunit.operation.DatabaseOperation;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

/**
 * In order to supply your own database connection credentials when running
 * this test locally, set the following system properties:
 * <p>
 * mvn.mysql.url			for the db url
 * mvn.mysql.username		for the username
 * mvn.mysql.password		for the password
 * mvn.mysql.database		for the default schema
 * mvn.mysql2.url			for the 2nd db url
 * mvn.mysql2.username		for the 2nd username
 * mvn.mysql2.password		for the 2nd password
 * mvn.mysql2.database		for the 2nd default schema
 *
 * @author oliver.kusche
 */
public abstract class AbstractUITest {

    protected final static Logger log = LogManager.getLogger(AbstractUITest.class);
    final String dbDriverClass = "com.mysql.cj.jdbc.Driver";
    final List<String> defaultDatabaseUrl = Arrays.asList("jdbc:mysql://localhost/soda_test?useUnicode=yes&characterEncoding=UTF-8", "jdbc:mysql://localhost/root2?useUnicode=yes&characterEncoding=UTF-8");
    final List<String> defaultUsername = Arrays.asList("root", "root");
    final List<String> defaultPassword = Arrays.asList("root", "root");
    final List<String> defaultSchema = Arrays.asList("root", "root2");
    List<IDatabaseTester> databaseTester = new ArrayList<IDatabaseTester>();
    IDataSet dataSet;
    private ComboPooledDataSource dataSource;
    private List<String> databaseUrl = new ArrayList<String>();
    private List<String> username = new ArrayList<String>();
    private List<String> password = new ArrayList<String>();
    private List<String> schema = new ArrayList<String>();
    private double timeTaken;
    protected RemoteWebDriver driver = null;
    protected ChromeDriverService driverService = null;
    private WebDriverManager wdm = null;
    protected TestFunctions testFunctions;

    /**
     * Get a list of to be imported data sets.
     * Please note: order does matter here, so set the file name of data set that shall be included into the other one as first
     * element of list.
     * Here the first data set may only contain non-null table entries (otherwise only the empty tables will be set into test database)
     *
     * @return A list of data set names that shall be imported into test
     */
    protected List<List<String>> getDBDataSetFileName() {
        return null;
    }

    public void setupWebDriver() {
        log.info("setting up Webdriver");
        wdm = WebDriverManager.chromedriver();
        wdm.setup();
        String chromedriverPath = wdm.getDownloadedDriverPath();
        System.setProperty("webdriver.chrome.driver", chromedriverPath);
    }

    @BeforeClass
    public void setup() throws Exception {
        log.debug("setup");

        setupWebDriver();
        ChromeOptions options = TestContext.getInstance().getChromeOptions();
        this.driver = new ChromeDriver(options);
        this.driverService = new ChromeDriverService.Builder().usingDriverExecutable(new File(wdm.getDownloadedDriverPath())).build();

        this.testFunctions = new TestFunctions();
        this.testFunctions.setDriver(this.driver);

        if (TestContext.exportPre)
            exportDB(this.getClass().getSimpleName(), "pre");

        if (getDBDataSetFileName() != null) {
            List<IDatabaseConnection> connectionList = getConnections();
            if (log.isTraceEnabled())
                log.trace("number of got connections: " + connectionList.size());
            int i = 0;
            for (IDatabaseConnection connection : connectionList) {
                if (log.isTraceEnabled())
                    log.trace("current index is: " + i);
                databaseTester.add(new DefaultDatabaseTester(connection));
                log.trace("number of database tester turned to: " + databaseTester.size());
                int last = databaseTester.size() - 1;
                databaseTester.get(last).setOperationListener(IOperationListener.NO_OP_OPERATION_LISTENER);
                databaseTester.get(last).setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
                databaseTester.get(last).setTearDownOperation(DatabaseOperation.CLOSE_CONNECTION(DatabaseOperation.TRUNCATE_TABLE));
                log.trace("got all properties for database tester not. " + i);

                setUp(getDBDataSetFileName(), i);

                log.trace("set up with files");

                databaseTester.get(last).onSetup();

                log.trace("set up database tester.");

                //connection.close();
                i++;
                log.trace("end of setup iteration");
            }
        }
    }

    @AfterMethod
    public void getRunTime(ITestResult tr) {
        timeTaken += tr.getEndMillis() - tr.getStartMillis();
    }

    @AfterClass
    public void tearDown() throws Exception {

        Logger logSubClass = LogManager.getLogger(this.getClass().getName());
        logSubClass.info("time taken: " + timeTaken / 1000 + " s");

        log.debug("tearDown");
        log.debug("closing webdriver");

        if (this.driverService != null)
            this.driverService.stop();
        if (this.driver != null)
            this.driver.quit();

        if (TestContext.exportPost)
            exportDB(this.getClass().getSimpleName(), "post");

        if (databaseTester != null) {
            for (IDatabaseTester tester : databaseTester) {
                log.trace("before tearDown - conn is closed: " + tester.getConnection().getConnection().isClosed());
                if (!tester.getConnection().getConnection().isClosed())
                    tester.onTearDown();
                log.trace("after tearDown - conn is closed: " + tester.getConnection().getConnection().isClosed());
            }
        }
    }

    @AfterSuite
    public void closeConnectionPool() {
        log.trace("closing datasource");
        try {
            if (this.dataSource != null)
                this.dataSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void exportDB(String name, String prefix) {
        if (log.isDebugEnabled())
            log.debug("exporting to " + name);

        try {
            IDatabaseConnection connection = getConnection();

            String[] filterTables = new String[]{"schema_version", "geographicalarea", "languages",
                    "industrialsector", "configuration"};

            ITableFilter filter = new ExcludeTableFilter(filterTables);

            IDataSet dataset = new DatabaseDataSet(connection, true, filter);

            String fileName = TestContext.DBUNIT_DATA_PATH + "/yDB_" + prefix + "_" + name + ".xml";
            FlatXmlDataSet.write(dataset, new FileOutputStream(fileName));

            connection.close();

            log.debug("export finished");

        } catch (Exception e) {
            log.error("Failed to export DB", e);
        }

    }

    private IDatabaseConnection getConnection() throws Exception {
        return getConnections().get(0);
    }

    protected JdbcTemplate newJDBCTemplate() {
        log.trace("Initialised JDBC template for source '{}'", this.dataSource.getJdbcUrl());
        return new JdbcTemplate(this.dataSource);
    }

    private List<IDatabaseConnection> getConnections() throws Exception {
        String url0 = System.getProperty("mvn.mysql.url", defaultDatabaseUrl.get(0));
        log.trace("setting db settings #0: url  " + url0);
        databaseUrl.add(url0);
        String user0 = System.getProperty("mvn.mysql.username", defaultUsername.get(0));
        log.trace("setting db settings #0: user " + user0);
        username.add(user0);
        String pass0 = System.getProperty("mvn.mysql.password", defaultPassword.get(0));
        log.trace("setting db settings #0: pass " + pass0);
        password.add(pass0);
        String db_schema0 = System.getProperty("mvn.mysql.database", defaultSchema.get(0));
        log.trace("setting db settings #0: schema " + db_schema0);
        schema.add(db_schema0);

        List<IDatabaseConnection> connectionList = new ArrayList<IDatabaseConnection>();

        for (int i = 1; i < getNumberOfConnections(); i++) {
            String url = System.getProperty("mvn.mysql" + (i + 1) + ".url", defaultDatabaseUrl.get(i));
            databaseUrl.add(url);
            log.trace("setting additional db settings #1: url  " + url);
            String user = System.getProperty("mvn.mysql" + (i + 1) + ".username", defaultUsername.get(i));
            log.trace("setting additional db settings #1: user " + user);
            username.add(user);
            String pass = System.getProperty("mvn.mysql" + (i + 1) + ".password", defaultPassword.get(i));
            log.trace("setting additional db settings #1: pass " + pass);
            password.add(pass);
            String db_schema = System.getProperty("mvn.mysql" + (i + 1) + ".database", defaultSchema.get(i));
            log.trace("setting additional db settings #1: schema " + pass);
            schema.add(db_schema);
        }

        for (int i = 0; i < getNumberOfConnections(); i++) {
            Connection jdbcConnection = null;

            try {
                this.dataSource = new ComboPooledDataSource();
                this.dataSource.setDriverClass(dbDriverClass); // shouldn't be necessary
                this.dataSource.setJdbcUrl(databaseUrl.get(i));
                this.dataSource.setUser(username.get(i));
                this.dataSource.setPassword(password.get(i));
                jdbcConnection = this.dataSource.getConnection();

                //we're disabling foreign key checks since dbunit can't deal with those properly
                Statement stmt = jdbcConnection.createStatement();
                stmt.execute("SET FOREIGN_KEY_CHECKS=0");
                stmt.close();

                log.trace("database connection pool initalized successfully");
            } catch (Exception e) {
                log.error("Error initializing database connection pool", e);
            }

            connectionList.add(new DatabaseConnection(jdbcConnection, schema.get(i)));

            DatabaseConfig databaseConfig = connectionList.get(i).getConfig();

            databaseConfig.setProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, new PrimaryKeyFilter());

            databaseConfig.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                    new org.dbunit.ext.mysql.MySqlDataTypeFactory());

            databaseConfig.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, new MySqlMetadataHandler());

            databaseConfig.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);

//			databaseConfig.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);

            databaseConfig.setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, true);
        }
        return connectionList;
    }

    /**
     * Pre-processes the data set (e.g. replaces certain Objects in data base by a given other object).
     *
     * @param dataset The data set that shall be pre-processed
     * @return The pre-processed data set
     */
    protected IDataSet preProcessDataSet(IDataSet dataset) {
        return dataset;
    }

    protected int getNumberOfConnections() {
        return 1;
    }

    private void setUp(List<List<String>> fileNameList, int k) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("number of connections: " + getNumberOfConnections());
            log.trace("number of database tester: " + databaseTester.size());
        }
        List<String> fileNames = fileNameList.get(k);
        IDataSet[] dataSets = new IDataSet[fileNames.size()];
        for (int i = 0; i < fileNames.size(); i++) {
            FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
            builder.setColumnSensing(true);
            dataSets[i] = builder.build(new File(TestContext.DBUNIT_DATA_PATH + "/" + fileNames.get(i)));
        }
        IDataSet mergedDataSet = new CompositeDataSet(dataSets, true);

        IDataSet preProcessedDataSet = preProcessDataSet(mergedDataSet);
        databaseTester.get(k).setDataSet(preProcessedDataSet);
    }

    private class PrimaryKeyFilter implements IColumnFilter {

        protected Logger log = LogManager.getLogger(PrimaryKeyFilter.class);

        Map<String, List<String>> tablePrimaryKeyMap = new HashMap<String, List<String>>();

        {
            tablePrimaryKeyMap.put("usergroup_user", Arrays.asList(new String[]{"users_ID", "groups_ID"}));
            tablePrimaryKeyMap.put("unitgroup_unit", Arrays.asList(new String[]{"UnitGroup_ID", "units_ID"}));
            tablePrimaryKeyMap.put("unitgroup_languages",
                    Arrays.asList(new String[]{"UnitGroup_ID", "supportedLanguages_ID"}));
            tablePrimaryKeyMap.put("UnitGroup_DESCRIPTION", Arrays.asList(new String[]{"unitgroup_id"}));
            tablePrimaryKeyMap.put("unitgroup_description", Arrays.asList(new String[]{"unitgroup_id"}));
            tablePrimaryKeyMap.put("unitgroup_classifications",
                    Arrays.asList(new String[]{"UnitGroup_ID", "classifications_ID"}));
            tablePrimaryKeyMap.put("unit_description", Arrays.asList(new String[]{"unit_id"}));
            tablePrimaryKeyMap.put("source_shortName", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("Source_NAME", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("source_name", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("source_shortname", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("source_languages",
                    Arrays.asList(new String[]{"Source_ID", "supportedLanguages_ID"}));
            tablePrimaryKeyMap.put("source_globalreference",
                    Arrays.asList(new String[]{"Source_ID", "contacts_ID"}));
            tablePrimaryKeyMap.put("Source_DESCRIPTION", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("source_description", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("source_classifications",
                    Arrays.asList(new String[]{"Source_ID", "classifications_ID"}));
            tablePrimaryKeyMap.put("source_citation", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("review_scopeofreview", Arrays.asList(new String[]{"Review_ID", "scopes_ID"}));
            tablePrimaryKeyMap.put("review_reviewdetails", Arrays.asList(new String[]{"review_id"}));
            tablePrimaryKeyMap.put("review_otherreviewdetails", Arrays.asList(new String[]{"review_id"}));
            tablePrimaryKeyMap.put("review_methods", Arrays.asList(new String[]{"scopeofreview_id"}));
            tablePrimaryKeyMap.put("review_globalreference",
                    Arrays.asList(new String[]{"Review_ID", "referencesToReviewers_ID"}));
            tablePrimaryKeyMap.put("review_dataqualityindicator",
                    Arrays.asList(new String[]{"Review_ID", "qualityIndicators_ID"}));
            tablePrimaryKeyMap.put("processname_unit", Arrays.asList(new String[]{"process_id"}));
            tablePrimaryKeyMap.put("processname_route", Arrays.asList(new String[]{"process_id"}));
            tablePrimaryKeyMap.put("processname_location", Arrays.asList(new String[]{"process_id"}));
            tablePrimaryKeyMap.put("processname_base", Arrays.asList(new String[]{"process_id"}));
            tablePrimaryKeyMap.put("process_userestrictions",
                    Arrays.asList(new String[]{"process_accessinformation_id"}));
            tablePrimaryKeyMap.put("process_useadvice", Arrays.asList(new String[]{"process_id"}));
            tablePrimaryKeyMap.put("process_timedescription",
                    Arrays.asList(new String[]{"process_timeinformation_id"}));
            tablePrimaryKeyMap.put("process_technicalpurpose", Arrays.asList(new String[]{"process_id"}));
            tablePrimaryKeyMap.put("process_synonyms", Arrays.asList(new String[]{"process_id"}));
            tablePrimaryKeyMap.put("process_review", Arrays.asList(new String[]{"Process_ID", "reviews_ID"}));
            tablePrimaryKeyMap.put("process_quantref_referenceids",
                    Arrays.asList(new String[]{"internalquantitativereference_id"}));
            tablePrimaryKeyMap.put("process_locationrestriction",
                    Arrays.asList(new String[]{"process_geography_id"}));
            tablePrimaryKeyMap.put("process_lcimethodapproaches", Arrays.asList(new String[]{"processId"}));
            tablePrimaryKeyMap.put("process_languages",
                    Arrays.asList(new String[]{"supportedLanguages_ID", "Process_ID"}));
            tablePrimaryKeyMap.put("process_exchange", Arrays.asList(new String[]{"Process_ID", "exchanges_ID"}));
            tablePrimaryKeyMap.put("process_compliancesystem",
                    Arrays.asList(new String[]{"Process_ID", "complianceSystems_ID"}));
            tablePrimaryKeyMap.put("process_classifications",
                    Arrays.asList(new String[]{"Process_ID", "classifications_ID"}));
            tablePrimaryKeyMap.put("lciamethod_ti_referenceyeardescription",
                    Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("lciamethod_ti_durationdescription",
                    Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("LCIAMethod_NAME", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("lciamethod_name", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("lciamethod_methodology", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("lciamethod_lciamethodcharacterisationfactor",
                    Arrays.asList(new String[]{"LCIAMethod_ID", "characterisationFactors_ID"}));
            tablePrimaryKeyMap.put("lciamethod_languages",
                    Arrays.asList(new String[]{"LCIAMethod_ID", "supportedLanguages_ID"}));
            tablePrimaryKeyMap.put("lciamethod_impactcategory", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("LCIAMethod_DESCRIPTION", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("lciamethod_description", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("lciamethod_classifications",
                    Arrays.asList(new String[]{"LCIAMethod_ID", "classifications_ID"}));
            tablePrimaryKeyMap.put("lciamethod_areaofprotection", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("globalreference_shortdescription",
                    Arrays.asList(new String[]{"globalreference_id"}));
            tablePrimaryKeyMap.put("flow_synonyms", Arrays.asList(new String[]{"flow_id"}));
            tablePrimaryKeyMap.put("flowproperty_synonyms", Arrays.asList(new String[]{"flowproperty_id"}));
            tablePrimaryKeyMap.put("flow_propertydescriptions",
                    Arrays.asList(new String[]{"Flow_ID", "propertyDescriptions_ID"}));
            tablePrimaryKeyMap.put("flowproperty_languages",
                    Arrays.asList(new String[]{"FlowProperty_ID", "supportedLanguages_ID"}));
            tablePrimaryKeyMap.put("flow_common_languages",
                    Arrays.asList(new String[]{"Flow_ID", "supportedLanguages_ID"}));
            tablePrimaryKeyMap.put("flowproperty_classifications",
                    Arrays.asList(new String[]{"FlowProperty_ID", "classifications_ID"}));
            tablePrimaryKeyMap.put("flow_common_classification",
                    Arrays.asList(new String[]{"Flow_ID", "classifications_ID"}));
            tablePrimaryKeyMap.put("datastock_longtitle", Arrays.asList(new String[]{"datastock_id"}));
            tablePrimaryKeyMap.put("datastock_description", Arrays.asList(new String[]{"datastock_id"}));
            tablePrimaryKeyMap.put("contact_shortname", Arrays.asList(new String[]{"contact_id"}));
            tablePrimaryKeyMap.put("Contact_NAME", Arrays.asList(new String[]{"contact_id"}));
            tablePrimaryKeyMap.put("contact_name", Arrays.asList(new String[]{"contact_id"}));
            tablePrimaryKeyMap.put("contact_languages",
                    Arrays.asList(new String[]{"Contact_ID", "supportedLanguages_ID"}));
            tablePrimaryKeyMap.put("Contact_DESCRIPTION", Arrays.asList(new String[]{"contact_id"}));
            tablePrimaryKeyMap.put("contact_description", Arrays.asList(new String[]{"contact_id"}));
            tablePrimaryKeyMap.put("contact_classifications",
                    Arrays.asList(new String[]{"Contact_ID", "classifications_ID"}));
            tablePrimaryKeyMap.put("configuration", Arrays.asList(new String[]{"default_datastock_id"}));
            tablePrimaryKeyMap.put("classification_clclass",
                    Arrays.asList(new String[]{"Classification_ID", "classes_ID"}));
            tablePrimaryKeyMap.put("UnitGroup_NAME", Arrays.asList(new String[]{"unitgroup_id"}));
            tablePrimaryKeyMap.put("unitgroup_name", Arrays.asList(new String[]{"unitgroup_id"}));
            tablePrimaryKeyMap.put("UnitGroup_DESCRIPTION", Arrays.asList(new String[]{"unitgroup_id"}));
            tablePrimaryKeyMap.put("unitgroup_description", Arrays.asList(new String[]{"unitgroup_id"}));
            tablePrimaryKeyMap.put("Source_NAME", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("source_name", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("Source_DESCRIPTION", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("source_description", Arrays.asList(new String[]{"source_id"}));
            tablePrimaryKeyMap.put("source_classifications",
                    Arrays.asList(new String[]{"Source_ID", "classifications_ID"}));
            tablePrimaryKeyMap.put("Process_DESCRIPTION", Arrays.asList(new String[]{"process_id"}));
            tablePrimaryKeyMap.put("process_description", Arrays.asList(new String[]{"process_id"}));
            tablePrimaryKeyMap.put("LCIAMethod_NAME", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("lciamethod_name", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("LCIAMethod_DESCRIPTION", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("lciamethod_description", Arrays.asList(new String[]{"lciamethod_id"}));
            tablePrimaryKeyMap.put("Flow_NAME", Arrays.asList(new String[]{"Flow_ID"}));
            tablePrimaryKeyMap.put("flow_name", Arrays.asList(new String[]{"Flow_ID"}));
            tablePrimaryKeyMap.put("Flow_DESCRIPTION", Arrays.asList(new String[]{"Flow_ID"}));
            tablePrimaryKeyMap.put("flow_description", Arrays.asList(new String[]{"Flow_ID"}));
            tablePrimaryKeyMap.put("FlowProperty_NAME", Arrays.asList(new String[]{"flowproperty_id"}));
            tablePrimaryKeyMap.put("flowproperty_name", Arrays.asList(new String[]{"flowproperty_id"}));
            tablePrimaryKeyMap.put("FlowProperty_DESCRIPTION", Arrays.asList(new String[]{"flowproperty_id"}));
            tablePrimaryKeyMap.put("flowproperty_description", Arrays.asList(new String[]{"flowproperty_id"}));
        }

        @Override
        public boolean accept(String tableName, Column column) {
            if (tablePrimaryKeyMap.containsKey(tableName)) {
                if (log.isTraceEnabled())
                    log.trace("accepting " + tableName + "." + column.getColumnName());
                return tablePrimaryKeyMap.get(tableName).contains(column.getColumnName());
            } else if ("ID".equalsIgnoreCase(column.getColumnName())) {
                if (log.isTraceEnabled())
                    log.trace("accepting " + tableName + "." + column.getColumnName());
                return true;
            }
            if (log.isTraceEnabled())
                log.trace("rejecting " + tableName + "." + column.getColumnName());
            return false;

        }

    }
}
