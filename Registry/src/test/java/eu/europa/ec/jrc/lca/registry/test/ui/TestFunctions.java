package eu.europa.ec.jrc.lca.registry.test.ui;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

/**
 * Functions, that are used in different testcases, like login
 *
 * @author mark.szabo
 */
public class TestFunctions {

    // Create an instance of HttpClient.
    public static HttpClient hclient = new HttpClient();

    /**
     * Checks if an element is on the site, and return true if it is, and false if isn't
     *
     * @param element
     * @return
     */
    public static boolean isElementNotPresent(By element) {
        try {
            Main.getInstance().getDriver().findElement(element);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Login on the side
     *
     * @param user        username
     * @param password    password
     * @param correct     should be true, if the user/pass is a valid logindata
     * @param staylogedin stay logged in or logout after checking the login
     * @param testadmin   Integer. 0 - no need to test the admin rights, 1 - test the admin rights and it should be admin, 2 -
     *                    test the admin rights and it should NOT be admin
     * @throws InterruptedException
     */
    public static void login(String user, String password, Boolean correct, Boolean staylogedin, Integer testadmin) throws InterruptedException
    // 'correct' should be true, if the user/pass is a valid logindata
    {
        WebDriver driver = Main.getInstance().getDriver();
        // open the main site
        driver.manage().deleteAllCookies();
        driver.get(Main.site);

        // click login
        findAndWaitOnElement(By.xpath(".//*[@id='leftFooter']/a[contains(@href, 'login.xhtml')]")).click();

        // login
        // wait for site to load
        (new WebDriverWait(driver, Duration.ofMillis(30))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='loginButton']")));
        // fill in the login form and click Login
        findAndWaitOnElement(By.xpath(".//input[@id='name']")).clear();
        findAndWaitOnElement(By.xpath(".//input[@id='name']")).sendKeys(user);
        Thread.sleep(Main.wait * 3);
        findAndWaitOnElement(By.xpath(".//input[@id='passw']")).clear();
        findAndWaitOnElement(By.xpath(".//input[@id='passw']")).sendKeys(password);
        findAndWaitOnElement(By.xpath(".//*[@id='loginButton']")).click();

        if (correct) {
            // test the admin rights, if need
            if (testadmin == 1)// the user is admin
            {
                // locate 'Admin area' button
                TestFunctions.findAndWaitOnElement(By.linkText(Main.lang.getProperty("admin.adminArea")));
            } else if (testadmin == 2) {
                if (TestFunctions.isElementNotPresent(By.linkText(Main.lang.getProperty("admin.adminArea"))))
                    org.testng.Assert.fail("The user should not be able to see the 'Admin area' link, but he sees");
            }

            // locate Logout link and logout .//*[@id='leftFooter']/a[2] and the next site .//*[@id='logoutButton']
            if (staylogedin) {
                // wait
                (new WebDriverWait(driver, Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='leftFooter']/a[2]")));
                findAndWaitOnElement(By.xpath(".//*[@id='leftFooter']/a[2]")); // Enough to locate the logout
                // button,
                // but don't click it
            } else // logout if staylogedin is false
            {
                // wait
                (new WebDriverWait(driver, Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='leftFooter']/a[2]")));
                findAndWaitOnElement(By.xpath(".//*[@id='leftFooter']/a[2]")).click();
                // wait
                (new WebDriverWait(driver, Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='logoutButton']")));
                findAndWaitOnElement(By.xpath(".//*[@id='logoutButton']")).click();
            }
        } else {
            // locate 'Incorrect user name/password' label .//*[@id='messages']/div
            // wait
            (new WebDriverWait(driver, Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='messages']/div")));
            findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));
        }
    }

    public static void login(String user, String password, Boolean correct) throws InterruptedException {
        login(user, password, correct, false, 0);
    }

    public static void login(String user, String password, Boolean correct, Boolean staylogedin) throws InterruptedException {
        login(user, password, correct, staylogedin, 0);
    }

    public static void loginReg(String user, String password, Boolean correct, Boolean staylogedin, Integer testadmin) throws InterruptedException {
        WebDriver driver = Main.getInstance().getDriver();
        // open the main site
        driver.manage().deleteAllCookies();
        driver.get(Main.siteReg);

        // fill in the login form and click Login
        findAndWaitOnElement(By.xpath(".//input[@id='login']")).clear();
        findAndWaitOnElement(By.xpath(".//input[@id='login']")).sendKeys(user);
        Thread.sleep(Main.wait);
        findAndWaitOnElement(By.xpath(".//input[@id='pass']")).clear();
        findAndWaitOnElement(By.xpath(".//input[@id='pass']")).sendKeys(password);
        findAndWaitOnElement(By.xpath(".//*[@id='loginButton']")).click();

        if (correct) {
            TestFunctions.checkNoMessage();
            //if ( !Main.getInstance().getDriver().getCurrentUrl().contains( "main.xhtml" ) )
            //	org.testng.Assert.fail( "After logging, it doesn't go to main.xhtml but goes to " + Main.getInstance().getDriver().getCurrentUrl() );
        } else {
            (new WebDriverWait(driver, Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='messages']/div")));
            findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));
        }
    }

    public static void gotoAdminArea() {

        TestFunctions.findAndWaitOnElement(By.linkText(Main.lang.getProperty("admin.adminArea"))).click();

        TestFunctions.waitOnAdminArea();
    }

    public static void waitOnAdminArea() {
        WebDriver driver = Main.getInstance().getDriver();

        (new WebDriverWait(driver, Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .linkText(Main.lang.getProperty("admin.globalConfig"))));
    }

    public static Properties propOpen(String language) {

        // language properties
        Properties lang = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            if (language == "en") // for English is the properties file in ISO8859-1
            {
                lang.load(loader.getResourceAsStream("lang_" + language + ".properties"));
            } else // for german it's in UTF-8
            {
                URL resource = loader.getResource("lang_" + language + ".properties");
                lang.load(new InputStreamReader(resource.openStream(), "UTF8"));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return lang;
    }

    public static Properties propRegOpen() {
        // message properties
        Properties msg = new Properties();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            msg.load(loader.getResourceAsStream("messages.properties"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return msg;
    }

    /**
     * Sometimes the UI too slow because of client side scripts. Then we have to wait for an element, before click on it
     *
     * @param b
     * @return
     */
    public static WebElement findAndWaitOnElement(By b) {
        // first wait
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(b));
        // then select
        return Main.getInstance().getDriver().findElement(b);
    }

    /**
     * Sometimes the correct behavior is, that no error message shows up. This function test the message and drop an
     * org.testng.Assert.fail if any message showed up
     */
    public static void checkNoMessage() {
        // check if there is no message
        try {
            Main.getInstance().getDriver().findElement(By.xpath(".//*[@id='messages']"));
        } catch (Exception e) {
            return;
        }

        // if there is a message, check if it's empty
        if (Main.getInstance().getDriver().findElement(By.xpath(".//*[@id='messages']")).getText().isEmpty())
            return;

        org.testng.Assert.fail("Message apperars: " + Main.getInstance().getDriver().findElement(By.xpath(".//*[@id='messages']")).getText());
    }

    /**
     * For testing the service API it's necessary to have a function which gives back the source of the requested url as
     * a string
     *
     * @param url     End of the url after Main.site + "resource/" OR the full url, depending on the fullurl variable
     * @param fullurl decide whether 'url' is a full url or a relative path
     *                if true
     * @return Returns the requested site's source
     * @throws Exception
     */
    public static String getUrl(String url, boolean fullurl) throws Exception {
        if (!fullurl) url = Main.site + "resource/" + url;
        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        String responseBody = null;

        try {
            // Execute the method.
            int statusCode = hclient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method '" + method.getURI() + "' failed: " + method.getStatusLine());
            }

            // Read the response body.

            // get respond length
            int length = method.getResponseBodyAsStream().available();
            if (length < 1) // if it's too long, it returns with a 0, in this case we have to manually attach it to 10
                // million
                length = 10000000;
            byte[] responseBodyByte = new byte[length];

            method.getResponseBodyAsStream().read(responseBodyByte, 0, length);

            responseBody = "";
            for (int i = 0; i < length && responseBodyByte[i] > 0; i++)
                responseBody += (char) responseBodyByte[i];

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return responseBody;
    }

    public static String getUrl(String urlend) throws Exception {
        return getUrl(urlend, false);
    }
}

