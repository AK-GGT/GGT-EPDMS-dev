package edu.kit.soda4lca.test.ui.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestContext {

    public static final String PRIMARY_SITE_URL = "http://localhost:8080/Node/";
    public static final String SECONDARY_SITE_URL = "http://localhost:8080/Node2/";
    public static final String REGISTRY_URL = "http://localhost:8080/Registry/";
    public static final String DRIVER_TYPE = "mvn.selenium.driver";
    public static final String HEADLESS = "mvn.selenium.headless";
    public static final String CHROME_DRIVER = "Chrome";
    public static final String FIREFOX_DRIVER = "Firefox";
    protected final static Logger log = LogManager.getLogger(TestContext.class);
    /**
     * If set to true, we will generate dbunit xml snapshots of the db state <i>after</i> each integration test
     */
    public static boolean exportPost = false;
    /**
     * If set to true, we will generate dbunit xml snapshots of the db state <i>before</i> each integration test
     */
    public static boolean exportPre = false;
    public static String DBUNIT_DATA_PATH = "src/test/resources/dbunit";
    public static String datafilepath = "/sample_data_med.zip";
    public static String language = "en"; // en for english, de for deutsch
    public static int timeout = 60; // maximum seconds to wait for a site to load
    public static int wait = 100; // millis to wait between entering 2 text
    // open language properties (i18n)
    public static Properties lang = new TestFunctions().propOpen(language);
    private static TestContext _instance = null;
    private RemoteWebDriver driver = null;
    private String tmpFolder = null;

    private boolean headless = false;

    private ChromeDriverService driverService = null;

    public TestContext() {
        this.tmpFolder = setupTmpFolder();
        this.headless = "true".equalsIgnoreCase(System.getProperty(HEADLESS, "false"));
//        setupDriver(System.getProperty(DRIVER_TYPE, CHROME_DRIVER));
    }

    public static TestContext getInstance() {
        if (_instance == null) {
            log.trace("_instance is null, instantiating");
            _instance = new TestContext();
        }
        return _instance;
    }

    private void setupDriver(String driverType) {

        log.trace("setting up driver");

        if (log.isDebugEnabled())
            log.debug("headless: " + this.headless);

        if (CHROME_DRIVER.equalsIgnoreCase(driverType)) {
            ChromeOptions options = getChromeOptions();
            this.driver = new ChromeDriver(options);
            this.driverService = new ChromeDriverService.Builder().usingDriverExecutable(new File(WebDriverManager.getInstance().getDownloadedDriverPath())).build();

            if (this.headless) {
                prepareChromeHeadlessDownload();
            }

        } else if (FIREFOX_DRIVER.equalsIgnoreCase(driverType)) {

            WebDriverManager.firefoxdriver().setup();

            FirefoxOptions options = getFirefoxOptions();
            this.driver = new FirefoxDriver(options);

        } else
            throw new IllegalArgumentException("Invalid driver type " + driverType);
    }

    public ChromeOptions getChromeOptions() {

        ChromeOptions options = new ChromeOptions();

        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("intl.accept_languages", "en");

        prefs.put("download.default_directory", this.tmpFolder);
        prefs.put("download.prompt_for_download", "false");
        prefs.put("download.directory_upgrade", "false");

        options.setExperimentalOption("prefs", prefs);

        options.addArguments("--test-type");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-dev-shm-usage");

        if (this.headless) {
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        } else {
            options.addArguments("--window-size=1920,1080");
        }

        return options;
    }

    private FirefoxOptions getFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();

        // set the language
        options.addPreference("intl.accept_languages", language);
        // for export disable download dialog
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.manager.showWhenStarting", false);
        options.addPreference("browser.download.panel.shown", false);
        options.addPreference("log", "{level: warn}");
        options.addPreference("browser.download.dir", this.tmpFolder);
        options.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/zip");

//        options.setHeadless(this.headless);

        return options;
    }

    private String setupTmpFolder() {
        String path = new File(getClass().getClassLoader().getResource(".").getFile()).getParent() + "/tmp";
        // if we are on a windows machine, the first / before the C:/ should be removed
        if (File.separator.equals("\\") && (path.startsWith("/") || path.startsWith("\\")))
            path = path.substring(1);
        path = path.replace("/", java.io.File.separator);
        if (log.isDebugEnabled())
            log.debug("setting temp folder to " + path);
        return path;
    }

    private void prepareChromeHeadlessDownload() {
        Map<String, Object> commandParams = new HashMap<>();
        commandParams.put("cmd", "Page.setDownloadBehavior");
        Map<String, String> params = new HashMap<>();
        params.put("behavior", "allow");
        params.put("downloadPath", this.tmpFolder);
        commandParams.put("params", params);
        ObjectMapper objectMapper = new ObjectMapper();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String command = null;
        try {
            command = objectMapper.writeValueAsString(commandParams);
        } catch (IOException e) {
            log.warn("preparation for download in headless mode could not be completed!");
            e.printStackTrace();
        }
        String u = this.driverService.getUrl().toString() + "/session/" + this.driver.getSessionId() + "/chromium/send_command";
        HttpPost request = new HttpPost(u);
        request.addHeader("content-type", "application/json");
        try {
            request.setEntity(new StringEntity(command));
        } catch (UnsupportedEncodingException e) {
            log.warn("preparation for download in headless mode could not be completed!");
            e.printStackTrace();
        }
        try {
            httpClient.execute(request);
        } catch (IOException e) {
            log.warn("preparation for download in headless mode could not be completed!");
            e.printStackTrace();
        }
    }

    public RemoteWebDriver getDriver() {
        return this.driver;
    }

    @AfterClass
    public void tearDown() {
        log.trace("AfterTest");
        if (this.driverService != null)
            this.driverService.stop();
        if (this.driver != null)
            this.driver.quit();
    }

    public String getTmpFolder() {
        return tmpFolder;
    }

    public boolean isHeadless() {
        return headless;
    }
}
