package eu.europa.ec.jrc.lca.registry.test.ui;

import org.junit.After;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterTest;

import java.io.File;
import java.util.Properties;

public class Main {

    // variables for the program
    public static String site;

    public static String siteReg;

    public static String language = "en"; // en for english, de for deutsch

    public static int timeout = 10; // maximum seconds to wait for a site to
    // load

    public static int wait = 300; // millis to wait between entering 2 text

    public static boolean fast = false;

    // open language properties
    public static Properties lang = TestFunctions.propOpen(language);

    public static Properties regMsg = TestFunctions.propRegOpen();
    private static Main _instance = null;
    // start firefox
    private FirefoxDriver ff = null;

    public Main() {

        Main.site = "http://localhost:" + getMavenProperty("test.app.node.port") + "/Node/";
        Main.siteReg = "http://localhost:" + getMavenProperty("test.app.registry.port") + "/Registry/";

        FirefoxOptions options = new FirefoxOptions();
        // set the language
        options.addPreference("intl.accept_languages", language);
        // for export disable download dialog
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.manager.showWhenStarting", false);
        options.addPreference("browser.download.panel.shown", false);
        // get the current project folder
        String path = getClass().getClassLoader().getResource(".").getPath() + "tmp/";
        // if we are on a windows maschine, the first / before the C:/ should be
        // removed
        if (File.separator.equals("\\"))
            path = path.substring(1);
        path = path.replace("/", java.io.File.separator);
        options.addPreference("browser.download.dir", path);
        options.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/zip");
        this.ff = new FirefoxDriver(options);
    }

    public static Main getInstance() {
        if (_instance == null)
            _instance = new Main();
        return _instance;
    }

    public static String getMavenUrlProperty(String propertyName) {
        String retval = "8080";
        try {
            Properties p = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            p.load(loader.getResourceAsStream("maven.properties"));
            retval = p.getProperty(propertyName);
        } catch (Exception e) {
//			e.printStackTrace();
        }
        return retval;
    }

    private String getMavenProperty(String propertyName) {
        String retval = "8080";
        try {
            Properties p = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            p.load(loader.getResourceAsStream("maven.properties"));
            retval = p.getProperty(propertyName);
        } catch (Exception e) {
//			e.printStackTrace();
        }
        return retval;
    }

    public FirefoxDriver getDriver() {
        return this.ff;
    }

    @AfterTest
    public void CloseFirefox() {
        Main.getInstance().getDriver().close();
        System.out.println("AfterTest");
    }

    @After
    public void CloseFirefoxJUnit() {
        Main.getInstance().getDriver().close();
        System.out.println("Simply After");
    }
}
