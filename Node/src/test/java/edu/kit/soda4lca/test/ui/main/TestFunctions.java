package edu.kit.soda4lca.test.ui.main;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import de.fzk.iai.ilcd.service.client.impl.vo.dataset.DataSetList;
import de.fzk.iai.ilcd.service.client.impl.vo.dataset.DataSetVO;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.security.jwt.JWTController;
import de.iai.ilcd.xml.read.JAXBContextResolver;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

/**
 * Functions, that are used in different testcases, like login
 *
 * @author mark.szabo
 */
public class TestFunctions {

    public final Predicate<String> STRING_NOT_BLANK = s -> (s != null && !s.trim().isEmpty());
    protected final static Logger log = LogManager.getLogger(TestFunctions.class);
    // Create an instance of HttpClient.
    public HttpClient hclient = new HttpClient();
    RestTemplate restTemplate;
    private Unmarshaller dataSetListUnmarshaller = null;

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    private WebDriver driver;


    /**
     * Checks if an element is on the site, and return true if it is, and false if isn't
     *
     * @param element
     * @return
     */
    public boolean isElementNotPresent(By element) {
        try {
            driver.findElement(element);
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
     * @param testadmin   Integer. 0 - no need to test the admin rights, 1 - test the admin rights, and it should be admin, 2 -
     *                    test the admin rights, and it should NOT be admin
     */
    public void login(String user, String password, Boolean correct, Boolean staylogedin, Integer testadmin, Boolean remembered) {
        // 'correct' should be true, if the user/pass is a valid logindata
        login(user, password, correct, staylogedin, testadmin, remembered, TestContext.PRIMARY_SITE_URL, null);
    }

    public void loginWithExpiredPassword(String user, String password, String newPassword, Boolean correct, Boolean staylogedin, Integer testadmin, Boolean remembered) {
        login(user, password, correct, staylogedin, testadmin, remembered, TestContext.PRIMARY_SITE_URL, newPassword);
    }

    /**
     * Login on the side
     *
     * @param user        username
     * @param password    password
     * @param correct     should be true, if the user/pass is a valid logindata
     * @param staylogedin stay logged in or logout after checking the login
     * @param testadmin   Integer. 0 - no need to test the admin rights, 1 - test the admin rights, and it should be admin, 2 -
     *                    test the admin rights, and it should NOT be admin
     * @param site        The URL string of site to connect to
     */
    public void login(String user, String password, Boolean correct, Boolean staylogedin, Integer testadmin, Boolean remembered, String site, String newPassword)
    // 'correct' should be true, if the user/pass is a valid logindata
    {
        WebDriverRunner.setWebDriver(driver);
        // open the main site
        WebDriverRunner.clearBrowserCache();
        open(site);

        // click login
        $x(".//*[@id='leftFooter']/a[contains(@href, 'login.xhtml')]").click();

        loginOnLoginSite(user, password, remembered);

        if (newPassword != null) {
            changeExpiredPasswordOnSite(newPassword);
        }

        if (correct) {
            // test the admin rights, if needed
            if (testadmin == 1)// the user is admin
            {
                // locate 'Admin area' button
                $(By.linkText(TestContext.lang.getProperty("admin.adminArea"))).shouldBe(visible);
            } else if (testadmin == 2) {
                if ($(By.linkText(TestContext.lang.getProperty("admin.adminArea"))).exists())
                    org.testng.Assert.fail("The user should not be able to see the 'Admin area' link, but he sees");
            }
            if ($x(".//*[text()='" + TestContext.lang.getProperty("common.privacyPolicyTitle") + "']").exists()) {
                $(By.id("acceptPrivacyPolicy")).click();
                clickButtonWithI18nLabel("admin.continue");
            }
            // locate Logout link and logout .//*[@id='leftFooter']/a[2] and the next site .//*[@id='logoutButton']
            if (staylogedin) {
                // wait
                $x(".//*[@id='leftFooter']/a[contains(@href, 'login.xhtml')]").shouldBe(visible);
            } else // logout if staylogedin is false
            {
                logout();
            }
        } else {
            // locate 'Incorrect user name/password' label .//*[@id='messages']/div
            // wait
            findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("public.user.logout"))).click();
        }

    }

    /**
     * Logs directly in with given parameters without any tests about correctness of user/password combination.
     *
     * @param user       the username
     * @param password   the password
     * @param remembered Should be true if user has clicked remember me check box
     */
    public void loginOnLoginSite(String user, String password, Boolean remembered) {
        // login
        // wait for site to load
        $x(".//*[@id='loginButton']").shouldBe();
        // fill in the login form and click Login
        $x(".//input[@id='name']").clear();
        $x(".//input[@id='name']").setValue(user);
        $x(".//input[@id='passw']").clear();
        $x(".//input[@id='passw']").setValue(password);
        if (remembered) {
            $x(".//*[@id='rememberme']").click();
        }
        $x(".//*[@id='loginButton']").click();
    }

    /**
     * Changes an expired password
     *
     * @param password the new password to be set
     */
    public void changeExpiredPasswordOnSite(String password) {
        $x(".//*[@id='changePasswordButton']").shouldBe(visible);
        $x(".//input[@id='passw']").clear();
        $x(".//input[@id='passw']").setValue(password);
        $x(".//input[@id='repeatPassw']").clear();
        $x(".//input[@id='repeatPassw']").setValue(password);
        $x(".//*[@id='changePasswordButton']").click();
    }

    public void login(String user, String password, Boolean correct) {
        login(user, password, correct, false, 0);
    }

    public void login(String user, String password, Boolean correct, Boolean staylogedin) {
        login(user, password, correct, staylogedin, 0);
    }

    public void login(String user, String password, Boolean correct, Boolean staylogedin, Integer testadmin) {
        login(user, password, correct, staylogedin, testadmin, false);
    }

    public void loginWithExpiredPassword(String user, String password, String newPassword, Boolean correct, Boolean staylogedin) {
        loginWithExpiredPassword(user, password, newPassword, correct, staylogedin, 0, false);

    }

    /**
     * Logs user out.
     */
    public void logout() {
        findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("public.user.logout"))).click();
        $x(".//*[@id='logoutButton']").click();
    }

    public void gotoAdminArea() {
        findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.adminArea"))).click();

        waitOnAdminArea();
    }

    public void waitOnAdminArea() {
        $(By.linkText(TestContext.lang.getProperty("admin.globalConfig"))).shouldBe(enabled);
    }

    public Properties propOpen(String language) {

        // language properties
        Properties lang = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            if (language == "en") // for English is the properties file in ISO8859-1
            {
                lang.load(loader.getResourceAsStream("resources/lang_" + language + ".properties"));
            } else // for german it's in UTF-8
            {
                URL resource = loader.getResource("resources/lang_" + language + ".properties");
                lang.load(new InputStreamReader(resource.openStream(), "UTF8"));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return lang;
    }

    /**
     * Sometimes the UI too slow because of client side scripts. Then we have to wait for an element, before click on it
     *
     * @param b
     * @return
     */
    public WebElement findAndWaitOnElement(By b) {
        return findAndWaitOnElement(b, TestContext.timeout);
    }

    public WebElement findAndWaitOnElement(WebElement b) {
        return findAndWaitOnElement(b, TestContext.timeout);
    }

    /**
     * Sometimes the UI too slow because of client side scripts. Then we have to wait for an element, before click on it
     *
     * @param b
     * @param timeout
     * @return
     */
    public WebElement findAndWaitOnElement(By b, int timeout) {
        for (int i = 0; i < timeout * 2; i++) {
            if ($(b).isDisplayed())
                break;
        }

        // then select
        return $(b);
    }

    public WebElement findAndWaitOnElement(WebElement b, int timeout) {
        for (int i = 0; i < timeout * 2; i++) {
            if (b.isDisplayed())
                break;
        }

        // then select
        return b;
    }

    /**
     * Sometimes the UI too slow because of client side scripts. Then we have to wait for an element, before click on it
     *
     * @param b
     * @return
     */
    public WebElement findAndWaitOnConsoleElement(By b) {
        // first wait
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(b));

        WebElement e = driver.findElement(b);

        // we need to scroll there first because Selenium doesn't
        int elementPosition = e.getLocation().getY();
        String js = String.format("window.scroll(0, %s)", elementPosition);
        ((JavascriptExecutor) driver).executeScript(js);

        e = driver.findElement(b);

        e = driver.findElement(b);

        // then select
        return e;
    }

    /**
     * Find a checkbox (PrimeFaces implementation specific)
     *
     * @param label
     * @return
     */
    public WebElement findAndWaitOnCheckBox(String label) {
        return $x(("//span[@class='ui-chkbox-label'][text()='" + label + "']"));
    }

    /**
     * find a button in a dialog with a given title (PrimeFaces implementation specific)
     *
     * @return
     */
    public WebElement findDialogButton(String dialogTitle, String buttonCaption) {
        return driver.findElement(By.xpath(".//button[preceding::div[child::span[text()='" + dialogTitle + "']] and child::span[text()='" + buttonCaption + "']]"));
    }

    /**
     * Sometimes the correct behavior is, that no error message shows up. This function test the message and drop an
     * org.testng.Assert.fail if any message showed up
     */
    public void assertNoMessageOtherThan(String... i18nKeyAllowed) {
        String msg = obtainMessageText();
        // Assert
        if (msg == null)
            return; // No message
        if (i18nKeyAllowed == null || i18nKeyAllowed.length < 1)
            Assert.fail(String.format("Found message when expecting to find none! Message: '%s'", msg));
        else {
            final String LINE_BREAK = System.getProperty("line.separator");
            final Set<String> allowList = Arrays.stream(i18nKeyAllowed).map(key -> TestContext.lang.getProperty(key)).collect(Collectors.toSet());
            if (!allowList.contains(msg))
                org.testng.Assert.fail(String.format("Unexpected message: '%s'%sAllowed messages would have been: %s%s", msg, LINE_BREAK, LINE_BREAK, allowList));
        }
    }

    public void assertNotMessage(@Nonnull String... i18nKeyBlocked) {
        if (i18nKeyBlocked.length < 1)
            throw new IllegalArgumentException("We need at least one specific i18n key whose corresponding msg should be blocked.");
        final String msg = obtainMessageText();
        // Assert
        if (msg != null) {
            final Set<String> blocklist = Arrays.stream(i18nKeyBlocked)
                    .map(key -> TestContext.lang.getProperty(key))
                    .collect(Collectors.toSet());
            if (blocklist.contains(msg))
                org.testng.Assert.fail(String.format("Unwanted message appears: '%s'", msg));
        }
    }

    private String obtainMessageText() {
        final String msg;
        try {
            msg = $x(".//*[@id='messages']").getText();
            if (msg.isBlank()) {
                String warning = "Found a message without text. Probably maybe there's a lang resource missing (or some error)";
                if (log.isDebugEnabled())
                    log.warn(warning, new IllegalStateException("Message with blank text in UI."));
                else
                    log.warn(warning);
                return null;
            }
        } catch (NoSuchElementException | NullPointerException e) {
            return null;
        }
        return msg;
    }

    private WebElement getXPathSafely(String xpath) {
        try {
            return $x(xpath);
        } catch (NoSuchElementException e) {
            return null;
        }
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
    public String getUrl(String url, boolean fullurl) throws Exception {
        if (!fullurl)
            url = TestContext.PRIMARY_SITE_URL + "resource/" + url;

        System.out.println("GETting " + url);
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

    public String getUrl(String urlend) throws Exception {
        return getUrl(urlend, false);
    }

    /**
     * Selects the given data stock.
     *
     * @param dataStock The data stock which shall be selected
     */
    public void selectDataStock(String dataStock) {
        findAndWaitOnElement(By.xpath(".//div[contains(@id, 'selectDataStock')]")).click();
        findAndWaitOnElement(By.xpath(".//li[text()='" + dataStock + "']")).click();
    }

    /**
     * Waits until site is loaded
     */
    public void waitUntilSiteLoaded() {
        Selenide.sleep(TestContext.wait * 2);
        new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout)).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Hovers over given menu item to then click on given menu selection in menu item.
     */
    public void selectMenuItem(String menuItem, String menuSelection) {
        // Hover over the menu
        $(By.linkText(TestContext.lang.getProperty(menuItem))).hover();
        // Click on selection
        $(By.linkText(TestContext.lang.getProperty(menuSelection))).click();
    }

    /**
     * Selects given data set.
     *
     * @param dataSet The data set which shall be selected.
     */
    public void selectDataSet(String dataSet) {
        // Hover over the menu
        waitUntilSiteLoaded();
        $(By.linkText(TestContext.lang.getProperty("admin.dataset.manageList"))).hover();
        // Click on data set
        String option = "";
        if (dataSet.endsWith("es")) {
            if (dataSet.endsWith("ies")) {
                option = dataSet.replace("ies", "y");
            } else if (dataSet == "sources") {
                option = "source";
            } else {
                option = dataSet.substring(0, dataSet.length() - 2);
            }

        } else if (dataSet.endsWith("s")) {
            option = dataSet.substring(0, dataSet.length() - 1);
        }
        $(By.linkText(TestContext.lang.getProperty("admin." + option + ".manageList"))).click();
    }

    /**
     * Counts the total number of users
     */
    public int countTotalUsersEntries() {
        return countTotalTableEntries(null, false);
    }

    /**
     * Counts the total number entries in data table.
     *
     * @param type       The data set type, if needed
     * @param assignView The flag indicating whether the table is in Assign Dataset View
     * @return The number of total table entries
     */
    public int countTotalTableEntries(String type, boolean assignView) {
        String paginatorXPath = ".//div[contains(@id, '_paginator_top')]";
        if (assignView) {
            paginatorXPath = ".//div[contains(@id, '" + type + "')]/div[contains(@id, '_paginator_top')]";
        }
        String currentElementText = findAndWaitOnElement(By.xpath(paginatorXPath)).getText();
        String tempString = StringUtils.substringAfter(currentElementText, " (");
        String numberTempString = StringUtils.substringBefore(tempString, " " + TestContext.lang.getProperty("common.list.total"));
        int usersEntries = Integer.valueOf(numberTempString);
        return usersEntries;
    }

    /**
     * Counts the total number of entries in a data set.
     *
     * @return the total amount of entries in a data set.
     */
    public int countTotalDatasetEntries() {
        String currentElementText = findAndWaitOnElement(By.className("ui-paginator-current")).getText();
        String tempString = StringUtils.substringBefore(currentElementText, " (");
        String numberTempString = StringUtils.substringAfter(tempString, ": ");
        int datasetEntries = Integer.valueOf(numberTempString);
        return datasetEntries;
    }

    /**
     * Counts all data sets which are visible on the current page of a data set.
     *
     * @return The number of data sets visible on current page of data set.
     */
    public int countDatasetEntriesOnPage() {
        Select select = new Select(findAndWaitOnElement(By.xpath(".//div[contains(@id,'_paginator_top')]/select[1]")));
        WebElement selectedOption = select.getFirstSelectedOption();
        String pageNumberString = selectedOption.getText();
        int pageEntries = Integer.valueOf(pageNumberString);
        return pageEntries;
    }

    /**
     * Fills in inputText (given by its ID) with given String. Uses the findAndWaitOnElement without Selenide for stability reasons.
     *
     * @param inputTextId The ID of to be filled in intputText
     * @param content     The content for inputText
     */
    public void fillInputText(String inputTextId, String content) {
        $x(".//input[@id='" + inputTextId + "']").doubleClick();
        $x(".//input[@id='" + inputTextId + "']").clear();
        if (content != null)
            $x(".//input[@id='" + inputTextId + "']").sendKeys(content);
    }

    /**
     * Clicks button with given label as i18n string. Uses findAndWaitOnConsoleElement without Selenide for stability reasons.
     *
     * @param name The i18n key for button label
     */
    public void clickButtonWithI18nLabel(String name) {
        findAndWaitOnConsoleElement(By.xpath(".//button/span[text()='" + TestContext.lang.getProperty(name) + "']/..")).click();
    }

    /**
     * Clicks on a button with css visibility=hidden.
     * <p>
     * Used to test deprecated features in the UI
     *
     * @param cssSelector
     * @see JWTController#generateTokenOLD(String, String)
     */
    public void clickButtonHidden(String cssSelector) {
        ((JavascriptExecutor) driver).executeScript(String.format("document.getElementsByClassName(\"%s\")[0].click();", cssSelector));
    }

    /**
     * Clicks the button with given name as label
     *
     * @param name The label name of button
     */
    public void clickButtonWithLabel(String name) {
        findAndWaitOnElement(By.xpath(".//button/span[text()='" + name + "']/..")).click();
    }

    /**
     * Clicks button that contains given i18n String als label.
     */
    public void clickButtonContainingI18nText(String name) {
        findAndWaitOnElement(By.xpath(".//button/span[contains(.,'" + TestContext.lang.getProperty(name) + "')]/..")).click();
    }

    /**
     * Selects a given item of a oneSelectMenu given by its ID. Uses the findAndWaitOnElement without Selenide for stability reasons.
     *
     * @param selectBoxID The ID of oneSelectMenu
     * @param item        The to be selected item
     */
    public void selectItemInSelectBox(String selectBoxID, String item) {
        $(By.id(selectBoxID)).click();
        $x(".//li[text()='" + item + "']").shouldBe(visible, Duration.ofMillis(TestContext.wait * 2)).click();
    }

    /**
     * Selects all current visible entries of table with given ID.
     *
     * @param tableID The jsf ID of table
     */
    public void selectAllTableEntries(String tableID) {
        findAndWaitOnElement(By.xpath(".//thead[@id='" + tableID + "_head']/tr[1]/th[1]/div[1]/div[2]")).click();
    }

    /**
     * Goes to certain page via hovering over the admin menu.
     *
     * @param menuList The list of menu items Selenium shall hover over; last item will be clicked
     */
    public void goToPageByAdminMenu(String... menuList) {
        for (int i = 0; i < menuList.length - 1; i++) {
            for (int j = 0; j < TestContext.timeout && !$(By.linkText(TestContext.lang.getProperty(menuList[i + 1]))).isDisplayed(); j++) {
                $(By.linkText(TestContext.lang.getProperty(menuList[i]))).hover();
                waitUntilSiteLoaded();
            }
        }
        for (int i = 0; i < TestContext.timeout && $(By.linkText(TestContext.lang.getProperty(menuList[menuList.length - 1]))).exists(); i++) {
            $(By.linkText(TestContext.lang.getProperty(menuList[menuList.length - 1]))).click();
            waitUntilSiteLoaded();
        }
    }

    /**
     * Checks if table with given name in target instance has given number of data set entries.
     *
     * @param tableName name of table
     * @param number    The expected number of daa set entries
     */
    public void targetTableContainsNumberDataSets(String tableName, Long number) {
        final var txt = findAndWaitOnElement(By.xpath(".//*[@id='" + tableName + "_paginator_top']/span")).getText();
        if (!(txt.contains("(" + number))) {
            Assert.fail("Incorrect count of datasets were transferred to target instance (Table name : '" + tableName + "') -- Expected to find: '(" + number + "', In text: '" + txt + "'");
        }
    }

    /**
     * Checks if table with given name is empty.
     *
     * @param tableName The name of to be checked table
     * @return True if table contains no entries
     */
    public boolean isTableEmpty(String tableName) {
        return findAndWaitOnElement(By.xpath(".//*[@id='" + tableName + "_data']/tr[1]/td[1]")).getText()
                .equals(TestContext.lang.getProperty("common.list.noneFound"));
    }

    /**
     * Checks if message with given language key is shown.
     *
     * @param i18nKey The language key
     * @return True if message with given language key is shown
     */
    public boolean isMessageShown(String i18nKey) {
        for (int i = 0; i < 500; i++) {
            if (isElementNotPresent(By.xpath(".//*[text()='" + TestContext.lang.getProperty(i18nKey) + "']")))
                return true;
        }
        return false;
    }

    public String getMessage() {
        return findAndWaitOnElement(By.xpath(".//div[@id='messages']/div")).getText();
    }

    /**
     * Checks if given text is shown.
     *
     * @param text The text that should be present
     * @return True if given text is present
     */
    public boolean isTextShown(String text) {
        return isElementNotPresent(By.xpath(".//*[text()='" + text + "']"));
    }

    /**
     * Checks if last entry of table with given name at given column equals the given expected text
     *
     * @param tableName    The name of table
     * @param column       The wanted column
     * @param expectedText The text the entry at given column is expected
     * @return True if given text equals the content of given column in last table row
     */
    public boolean isLastTableEntryAtColumnEqual(String tableName, Long column, String expectedText) {
        String textFound = findAndWaitOnElement(By.xpath(".//*[@id='" + tableName + "_data']/tr[last()]/td[" + column + "]")).getText();
        boolean expecationMet = textFound.equals(expectedText);
        if (!expecationMet)
            log.info("Expected to find text '" +
                    expectedText +
                    "' in last row in column " +
                    column +
                    " at table with name '" +
                    tableName +
                    "'. However, actually found: '" +
                    textFound +
                    "'");
        return expecationMet;
    }

    /**
     * Gets entry of given column in last table row with given table name.
     *
     * @param tableName The name of table
     * @param column    The wanted column
     * @return A String containing the column  content of last table row
     */
    public String getLastTableEntryAtColumn(String tableName, Long column) {
        return findAndWaitOnElement(By.xpath(".//*[@id='" + tableName + "_data']/tr[last()]/td[" + column + "]")).getText();
    }

    /**
     * Gets entry of given column in first row with given table name.
     *
     * @param tableName The name of table
     * @param column    The wanted column
     * @return A String containing the column  content of last table row
     */
    public String getFirstTableEntryAtColumn(String tableName, Long column) {
        return findAndWaitOnElement(By.xpath(".//*[@id='" + tableName + "_data']/tr[1]/td[" + column + "]")).getText();
    }

    /**
     * Gets entry of given column in second row with given table name.
     *
     * @param tableName The name of table
     * @param column    The wanted column
     * @return A String containing the column  content of last table row
     */
    public String getSecondTableEntryAtColumn(String tableName, Long column) {
        return findAndWaitOnElement(By.xpath(".//*[@id='" + tableName + "_data']/tr[2]/td[" + column + "]")).getText();
    }

    /**
     * Selects a table entry with given entry name in given column of given Table ID.
     *
     * @param tableID      The jsf ID of given table
     * @param name         The id/name of table entry
     * @param columnNumber The column number the name is expected to be.
     */
    public void selectTableEntry(String tableID, String name, int columnNumber) {
        String xpath = ".//*[@id='" + tableID + "_data']/tr/td[" + columnNumber + "]//*[text()='" + name + "']/../td[1]/div[1]/div[2]";
        if (isElementNotPresent(By.xpath(xpath)))
            findAndWaitOnElement(By.xpath(xpath)).click();
        else
            findAndWaitOnElement(By.xpath(".//*[@id='" + tableID + "_data']/tr/td[" + columnNumber + "]//*[text()='" + name + "']/../../td[1]/div[1]/div[2]")).click();

    }

    /**
     * Selects a table entry with given row number in table with given table ID.
     *
     * @param tableID   The jsf ID of given table
     * @param rowNumber The wanted row number of to selected entry
     */
    public void selectTableEntryByNumber(String tableID, int rowNumber) {
        By by = By.xpath(".//*[@id='" + tableID + "_data']/tr[" + rowNumber + "]/td[1]/div[1]/div[2]");
        findAndWaitOnElement(by).click();

    }

    /**
     * Selets a table entry with given expected content in given column of table with given table ID.
     *
     * @param tableID      The jsf table ID
     * @param content      The expected content
     * @param columnNumber The column number the content sis xpected to be
     */
    public void selectTableEntryByContent(String tableID, String content, int columnNumber) {
        int i = 1;
        String xpath = ".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[" + columnNumber + "]";
        if (findAndWaitOnElement(By.xpath(xpath)).getText() == null)
            xpath = ".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[" + columnNumber + "]//*";
        while (!findAndWaitOnElement(By.xpath(xpath)).getText().equals(
                content)) {
            i++;
            xpath = ".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[" + columnNumber + "]";
            if (findAndWaitOnElement(By.xpath(xpath)).getText() == null)
                xpath = ".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[" + columnNumber + "]//*";

        }
        By by = By.xpath(".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[1]/div[1]/div[2]");
        findAndWaitOnElement(by).click();

    }

    /**
     * Selets a table entry with given expected content in given column of table with given table ID.
     *
     * @param tableID            The jsf table ID
     * @param content            The expected content
     * @param columnNumber       The column number the content is expected to be
     * @param targetColumnNumber The column number of which content is wanted
     * @param columnContent      the wanted column content
     */
    public By getColumnTableEntryByContent(String tableID, String content, int columnNumber, int targetColumnNumber, String columnContent) {
        int i = 1;
        String xpath = ".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[" + columnNumber + "]";
        if (findAndWaitOnElement(By.xpath(xpath)).getText() == null)
            xpath = ".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[" + columnNumber + "]//*";
        while (!findAndWaitOnElement(By.xpath(xpath)).getText().equals(
                content)) {
            i++;
            xpath = ".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[" + columnNumber + "]";
            if (findAndWaitOnElement(By.xpath(xpath)).getText() == null)
                xpath = ".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[" + columnNumber + "]//*";

        }
        return By.xpath(".//*[@id='" + tableID + "_data']/tr[" + i + "]/td[" + targetColumnNumber + "]/*[" + columnContent + "]");

    }

    public void selectDataSetFromDataTable(String uuid) {
        findAndWaitOnElement(By.xpath(".//*[contains(text(),'" + uuid + "')]")).click();
//		/../../td[1]/div[1]
    }

    public Boolean selectOptionInDataSetDialogue(int option) {
        if (option < 1 || option > 4) {
            return false;
        } else {
            $x(".//*[@id='generalForm']/div[last()]/div[2]/fieldset[1]/legend[1]").click();
            $x(".//*[@id='generalForm']/div[last()]/div[2]/fieldset[1]/div[1]/table[1]/tbody[1]/tr[2]/td[1]/table[1]/tbody[1]/tr[" + option + "]/td[1]/label[1]").click();
            $x(".//*[@id='generalForm']/div[last()]//button[contains(.," + "'" + TestContext.lang.getProperty("admin.ok") + "'" + ")]").click();
            return true;
        }
    }

    /**
     * Extracts password from given mail.
     *
     * @param message The message the mail contains
     * @return The password the mail contains
     * @throws IOException
     * @throws MessagingException
     */
    public String extractPasswordFomMail(MimeMessage message) throws IOException, MessagingException {
        String content = String.valueOf(message.getContent());
        String[] contentSplits2 = content.split("b>Password</b>: ");
        String passw = contentSplits2[1].split("\r")[0];
        return passw;
    }

    /**
     * Extracts username from given mail.
     *
     * @param message The message the mail contains
     * @return The username the mail contains
     * @throws IOException
     * @throws MessagingException
     */
    public String extractUserFomMail(MimeMessage message) throws IOException, MessagingException {
        String content = String.valueOf(message.getContent());
        String[] contentSplits2 = content.split("b>Login</b>: ");
        String user = contentSplits2[1].split("<br/>")[0];
        return user;
    }

    /**
     * Extracts link from given mail.
     *
     * @param message The message which is contained in mail
     * @return The link the mail contains
     * @throws IOException
     * @throws MessagingException
     */
    public String extractLinkFomMail(MimeMessage message) throws IOException, MessagingException {
        String content = String.valueOf(message.getContent());
        String[] contentSplits2 = content.split("href=\"");
        String link = contentSplits2[1].split("\">")[0];
        link = link.replace("&amp;", "&");
        return link;
    }

    /**
     * Creates a Client.
     *
     * @return A created client
     */
    public Client createClient() {
        Client client;
        ClientConfig cc = new ClientConfig();
        cc.register(JAXBContextResolver.class);
        cc.property(ClientProperties.FOLLOW_REDIRECTS, true);
        cc.register(JacksonFeature.class);
        client = ClientBuilder.newClient(cc);
        return client;
    }

    /**
     * Generates an API key via GET request to REST service API.
     *
     * @param userName       The name of user that shall generate an API key
     * @param password       The password for given user
     * @param expectedStatus The expected response status code
     * @return A String containing either the API key or an error message
     */
    public String generateAPIKeyGET(String userName, String password, int expectedStatus) {
        String APIKey = null;
        int status = -1;

        if (restTemplate == null)
            restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(TestContext.PRIMARY_SITE_URL
                    + "resource/authenticate/getTokenOLD?userName=" + userName + "&password=" + password, String.class);

            status = response.getStatusCodeValue();
            APIKey = response.getBody();
        } catch (HttpClientErrorException e) {
            status = e.getRawStatusCode();
        }

        if (status != expectedStatus) {
            org.testng.Assert.fail("User with name " + userName + " should get response code " + expectedStatus
                    + " after requesting API key but gets response code " + status + ".");
        }

        return APIKey;
    }

    public String generateAPIKeyGET(String userName, String password) {
        return generateAPIKeyGET(userName, password, 200);
    }

    /**
     * Checks with API key whether wanted dataset occurs in dataset list of given
     * type in given data stock.
     *
     * @param APIKey     The API key for authentication
     * @param stock_id   The UUID of data stock to check
     * @param dataset_id The UUID of datast to check if it exists
     * @param type       The type of dataset to check
     * @param expected   The flag indicating whether dataset should occur or not.
     */
    public void checkDatasetExisting(String APIKey, String stock_id, String dataset_id, String type, boolean expected,
                                     String header_name, Client client) {
        boolean datasetVisible = false;
        Response response;

        String url = TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + stock_id + "/" + type;

        log.debug("calling URL " + url);

        if (APIKey != null) {
            response = client.target(url).request().header(header_name, "Bearer " + APIKey).get(Response.class);
        } else {
            response = client.target(url).request().get(Response.class);
        }

        int status = response.getStatus();

        log.debug("response is HTTP " + status);

        if (status == 200) {


            GenericType<JAXBElement<DataSetList>> genericType = new GenericType<JAXBElement<DataSetList>>() {
            };
            JAXBElement<DataSetList> e = response.readEntity(genericType);

            DataSetList list = e.getValue();

            List<DataSetVO> datasetList = list.getDataSet();

            log.debug("result set has " + datasetList.size() + " entries");

            // Going through stock list and check whether stock UUID equals UUID of
            // restricted stock
            for (DataSetVO datasetVO : datasetList) {

                if (datasetVO.getUuidAsString().equals(dataset_id)) {
                    datasetVisible = true;
                    continue;
                }

            }

        }

        if (expected != datasetVisible) {
            String vf[] = {" not", ""};
            /**
             * case datasetVisible == True:
             * 	message:  "The " + type + " dataset " + dataset_id + " should" + " not" + " be visible in data stock " + stock_id + " but seems" + "" + " to be."
             *
             * case datasetVisible == False:
             * 	message:  "The " + type + " dataset " + dataset_id + " should" + "" + " be visible in data stock " + stock_id + " but seems" + " not" + " to be."
             */
            String message = "The " + type + " dataset " + dataset_id + " should" + vf[datasetVisible ? 0 : 1] + " be visible in data stock "
                    + stock_id + " but seems" + vf[!datasetVisible ? 0 : 1] + " to be.";
            log.error(message);
            org.testng.Assert.fail(message);
        }

    }

    /**
     * Calls the getter repeatedly as long as it fails to provide a non-null
     * String value or until 30 seconds hav passed before returning the (last received) String value.<br/>
     * <i>NOTE: The test fails if no non-null value is received withing 30 seconds.</i>
     *
     * @param getter               supplies a string (e.g. from UI)
     * @param reattemptWithRefresh refresh page between getter calls
     * @param goalDescription      describes the expected outcome for the logs in case a valid string couldn't be obtained.
     * @return the first non-blank String received
     */
    public String getValidStringWaiting(Supplier<String> getter, boolean reattemptWithRefresh,
                                        String goalDescription) throws InterruptedException {
        Predicate<String> nonNull = Objects::nonNull; // default predicate: every non-null String is fine
        return getValidStringWaiting(getter, nonNull, reattemptWithRefresh, true, goalDescription); // default: failing early
    }

    /**
     * Calls the getter repeatedly as long as it fails to provide a non-valid
     * String value or until the timeout is reached before returning the (last received) String value.
     *
     * @param getter               gets some string (e.g. from UI)
     * @param stringValidation     the predicate applied to the received String values. If it returns false -> wait + retry
     * @param reattemptWithRefresh refresh page between getter calls
     * @param failTestIfInvalid    if the last received String value does not meet the requirements of the predicate,
     *                             the method may fail the test
     * @param goalDescription      describes the expected outcome for the logs in case a valid string couldn't be obtained.
     * @return the first non-blank String received or the last blank String received
     */
    public String getValidStringWaiting(Supplier<String> getter, Predicate<String> stringValidation,
                                        boolean reattemptWithRefresh, boolean failTestIfInvalid,
                                        String goalDescription) throws InterruptedException {
        Duration timeOutAfter = Duration.of(30, ChronoUnit.SECONDS);// default: timeout after 30 seconds
        return getValidStringWaiting(getter, stringValidation, timeOutAfter,
                reattemptWithRefresh, failTestIfInvalid, goalDescription);
    }

    /**
     * Calls the getter repeatedly as long as it fails to provide a valid
     * String value or until the timeout is reached before returning the (last received) String value.
     *
     * @param getter               gets some String value (e.g. from UI)
     * @param stringValidation     the predicate applied to the recieved Strings. If it returns false -> wait + retry
     * @param timeOut              if the getter fails to provide a valid String value, we hand over whatever was provided to the
     *                             initializer after the timeout duration is exceeded
     * @param reattemptWithRefresh refresh page between getter calls
     * @param failTestIfInvalid    if the last received String value does not meet the requirements of the predicate,
     *                             the method may fail the test
     * @param goalDescription      describes the expected outcome for the logs in case a valid string couldn't be obtained.
     * @return the first valid String value received or the last invalid String value received
     */
    public String getValidStringWaiting(Supplier<String> getter, Predicate<String> stringValidation,
                                        Duration timeOut, boolean reattemptWithRefresh, boolean failTestIfInvalid,
                                        String goalDescription) throws InterruptedException {
        String s = getter.get();
        boolean isValid = stringValidation.test(s);
        boolean firstSleepPassed = false; // Let's try the first time without refresh

        // wait on valid String
        Instant start = Instant.now();
        while (!isValid && Duration.between(start, Instant.now()).compareTo(timeOut) < 0) {

            if (Thread.currentThread().isInterrupted())
                throw new InterruptedException();

            if (reattemptWithRefresh && firstSleepPassed) { // after first sleep, we will refresh the page if requested
                if (log.isTraceEnabled())
                    log.trace("Attempting refresh while waiting on suitable String value.");
                driver.navigate().refresh();
                waitUntilSiteLoaded();
            }


            try {
                Thread.sleep(!firstSleepPassed ? 3 * 1000 : 6 * 1000); // TODO: can't we remove the thread sleep?
                if (!firstSleepPassed)
                    firstSleepPassed = true;

            } catch (InterruptedException e) {
                e.printStackTrace();

            }

            s = getter.get();
            isValid = stringValidation.test(s);
        }

        if (log.isTraceEnabled() || failTestIfInvalid) {
            String message = "Waited " +
                    Duration.between(start, Instant.now()).getSeconds() +
                    " seconds for value '" +
                    s +
                    "'. This is considered " +
                    (isValid ? "valid." : "invalid.") +
                    (goalDescription != null ? (" Goal was: " + goalDescription) : "");

            if (failTestIfInvalid) {
                if (!isValid)
                    Assert.fail(message);

            } else { // log.isTraceEnabled()
                log.trace(message);
            }
        }

        return s;
    }

    @SuppressWarnings("all")
    public boolean isDataSetAmongAssignedDataSetsOfDataStock(@Nonnull final UUID dsUuid,
                                                             @Nonnull final DataSetVersion dsVersion,
                                                             @Nonnull final DataSetType type,
                                                             @Nonnull final UUID stockUuid,
                                                             @Nonnull final Client client,
                                                             @Nonnull final String APIKey) throws RuntimeException {
        final int expectedResponseStatus = 200;
        final String LINE_SEPARATOR = System.getProperty("line.separator");
        try {
            var url = new URL(TestContext.PRIMARY_SITE_URL + "resource/datastocks/" + stockUuid + "/" + type.getStandardFolderName());
            log.debug("Verifying data set (type='{}', uuid='{}', version='{}') is among assigned data sets of data stock (uuid='{}') using 'GET {}'", type.getValue(), dsUuid, dsVersion.getVersionString(), stockUuid, url);
            Response response = client.target(url.toString()).request()
                    .header("Authorization", "Bearer " + APIKey)
                    .get(Response.class);

            String stringEntity = response.readEntity(String.class);
            log.debug("Response status: {}" + LINE_SEPARATOR + "Response entity (as String):" + LINE_SEPARATOR + "{}", response.getStatus(), stringEntity);
            if (response.getStatus() == expectedResponseStatus) {
                JAXBElement<DataSetList> jaxbList = (JAXBElement<DataSetList>) getDataSetListUnmarshaller().unmarshal(new StringReader(stringEntity));
                List<DataSetVO> assignedDataSets = jaxbList.getValue().getDataSet();
                return assignedDataSets.stream().anyMatch(ds -> dsUuid.toString().equals(ds.getUuidAsString()) && dsVersion.getVersionString().equals(ds.getDataSetVersion()));
            } else {
                throw new IllegalStateException(String.format("Unexpected HTTP response status, when requesting 'GET %s'%sExpected: %s%sActual: %s", url, LINE_SEPARATOR, expectedResponseStatus, LINE_SEPARATOR, response.getStatus()));
            }

        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    private Unmarshaller getDataSetListUnmarshaller() {
        if (dataSetListUnmarshaller == null) {
            try {
                dataSetListUnmarshaller = JAXBContext.newInstance(DataSetList.class).createUnmarshaller();
            } catch (JAXBException e) {
                throw new RuntimeException("Error while trying to instantiate unmarshaller for de.fzk.iai.ilcd.service.client.impl.vo.dataset.DataSetList", e);
            }
        }
        return dataSetListUnmarshaller;
    }

    public void waitUntilTrue(Supplier<Boolean> booleanSupplier, int timeoutInSeconds) {
        final int superTimeout = 120; // 2 minutes.
        final Instant start = Instant.now();
        Supplier<Long> checkSeconds = () -> Duration.between(start, Instant.now()).getSeconds();
        long secondsWaited = 0;
        while (secondsWaited <= timeoutInSeconds
                && secondsWaited <= superTimeout
                && !Thread.interrupted()) {

            if (booleanSupplier.get())
                break;

            secondsWaited = checkSeconds.get();
        }
    }

    public String md5(@Nonnull final byte[] s) {
        return DigestUtils.md5DigestAsHex(s);
    }

    public String md5ForFile(@Nonnull final Path path) throws IOException {
        return DigestUtils.md5DigestAsHex(new BufferedInputStream(new FileInputStream(path.toFile())));
    }
}
