package edu.kit.soda4lca.test.ui.basic;

import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.PathSelectors;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * We've already imported stuff, so now go to the user interface and test if the imported datasets are available
 *
 * @author mark.szabo
 */
public class T020BrowseDataSetsTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T020BrowseDataSetsTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    @Test(priority = 201)
    public void testProcesses() throws Exception {
        testSomething("processes", true, "Process steam from natural gas; heat plant; consumption mix, at plant; MJ", "processsteam.xml");
    }

    @Test(priority = 202)
    public void testLCIAMethods() throws Exception {
        testSomething("lciaMethods", false, "ILCD2011; Eutrophication freshwater; endpoint; PDF; ReCiPe", "ilcd2011.xml");
    }

    @Test(priority = 203)
    public void testElementaryFlows() throws Exception {
        testSomething("elementaryFlows", true, "2,6-dichloro-p-cresol", "dichloro-p-cresol.xml");
    }

    @Test(priority = 204)
    public void testProductFlows() throws Exception {
        testSomething("productFlows", true, "Refractory", "refractory.xml");
    }

    @Test(priority = 205)
    public void testFlowProperties() throws Exception {
        testSomething("flowProperties", true, "Length", "length.xml");
    }

    @Test(priority = 206)
    public void testUnitGroups() throws Exception {
        testSomething("unitGroups", true, "Units of area", "unitsofarea.xml");
    }

    @Test(priority = 207)
    public void testSources() throws Exception {
        testSomething("sources", true, "ReCiPe methodology for Life Cycle Assessment Impact Assessment, v1.05", "recipemethodology.xml");
    }

    @Test(priority = 208)
    public void testContacts() throws Exception {
        testSomething("contacts", true, "EuLA", "eula.xml");
    }

    /**
     * Because every dataset is very similar, this method can test any of it. First find the element, check its name on
     * the complete dataset view, then download it as xml compare with the stored xml line by line
     *
     * @param menu                        Language property "common. +menu" constrains the linktext in the menu
     * @param table                       Doesn't need, should be deleted, on the list
     * @param completeDataSetsIsAvailable right now there is now complete dataset html view for LCIAMethods. IF it would be available in the
     *                                    future, just change it to true
     * @param name                        name of the dataset we are about to test
     * @param xml                         name of the stored xml of this dataset
     * @throws Exception
     */
    public void testSomething(String menu, boolean completeDataSetsIsAvailable, String name, String xml) throws Exception {
        log.info(menu + "test started. Looking for  " + name);
        driver.manage().deleteAllCookies();
        // login as admin
        testFunctions.login("Admin2", "s3cr3t", true, true);

        driver.get(TestContext.PRIMARY_SITE_URL);
        // wait
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(TestContext.lang
                .getProperty("common." + menu))));
        // find and click on the menu
        driver.findElement(By.linkText(TestContext.lang.getProperty("common." + menu))).click();
        // wait
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(PathSelectors.DATASTOCK_SELECTOR)));

        log.trace("Change DataStock");
        // change DataStock
        driver.findElement(By.xpath(PathSelectors.DATASTOCK_SELECTOR)).click();
        driver.findElement(By.xpath(".//*[@data-label='RootStock1']")).click();
        log.trace("Find the dataset and click on it");
        // wait
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(name)));
        // click on the item
        driver.findElement(By.linkText(name)).click();
        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(".//*[@id='datasetPage']/div[2]/table[2]/tbody/tr/td[1]")));
        // check if the full name the same is as was in the previous page
        if (menu == "elementaryFlows" || menu == "productFlows" || menu == "contacts") {
            if (!driver.findElement(By.xpath(".//*[@id='datasetPage']/div[2]/table[1]/tbody/tr/td[2]/span")).getText().contains(
                    name))
                org.testng.Assert.fail("The name on " + driver.findElement(By.xpath(".//*[@id='content']/h1")).getText()
                        + " page (url: " + driver.getCurrentUrl()
                        + ") isn't the same as in the previous page. Other possible reason: too long name. Original name was " + name
                        + " and the name here is "
                        + driver.findElement(By.xpath(".//*[@id='datasetPage']/div[2]/table[1]/tbody/tr/td[1]/span")).getText());
        } else if (!driver.findElement(By.xpath(".//*[@id='datasetPage']/div[2]/table[1]/tbody/tr/td[1]/span")).getText().contains(
                name))
            org.testng.Assert.fail("The name on " + driver.findElement(By.xpath(".//*[@id='content']/h1")).getText()
                    + " page (url: " + driver.getCurrentUrl()
                    + ") isn't the same as in the previous page. Other possible reason: too long name. Original name was " + name + " and the name here is "
                    + driver.findElement(By.xpath(".//*[@id='datasetPage']/div[2]/table[1]/tbody/tr/td[1]/span")).getText());

        // if HTML version of the complete data sets available, check it
        log.trace("if HTML version of the complete data sets available, check it");
        if (completeDataSetsIsAvailable) {
            // click on View entire data set if Process
            if (menu.equals("processes")) {
                String url = driver.findElement(By.linkText(TestContext.lang.getProperty("public.proc.viewDatasetDetail")))
                        .getAttribute("href");
                driver.navigate().to(url);
            }
            // otherwise click on View complete dataset
            else {
                String url = driver.findElement(By.linkText(TestContext.lang.getProperty("public.dataset.viewDataset"))).getAttribute(
                        "href");
                driver.navigate().to(url);
            }
            // wait
            // (new WebDriverWait( Main.getInstance().getDriver(), Main.timeout )).until(
            // ExpectedConditions.visibilityOfElementLocated( By.xpath( "html/body/table/tbody/tr[1]/td" ) ) );
            // wait
            Thread.sleep(TestContext.wait);
            // check the name again

            if (!driver.getPageSource().contains(name))
                org.testng.Assert.fail("The name on the 'Entire Data set' page (url: " + driver.getCurrentUrl()
                        + ") isn't the same as in the previous page. Other possible reason: too long name. Original name was " + name);
            driver.navigate().back();
        }
        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(TestContext.lang
                .getProperty("public.dataset.downloadDatasetXML"))));

        log.debug("test the xml file");
        /*
         * TEST THE XML FILE DOWNLOAD
         * it's important, because the serverside use the same Node/resource/ functions
         */
        // open the online xml file

        testFunctions.hclient = new HttpClient();
        testFunctions.getUrl("authenticate/login?userName=Admin2&password=s3cr3t");

        // URL onlinefile = new URL( Main.getInstance().getDriver().findElement( By.linkText( Main.lang.getProperty(
        // "public.dataset.downloadDatasetXML" ) ) ) .getAttribute( "href" ) );
        // log.trace( "Open online file " + onlinefile.getFile() );
        // BufferedReader on = new BufferedReader( new InputStreamReader( onlinefile.openStream() ) );
        Scanner on = new Scanner(testFunctions.getUrl(driver.findElement(
                By.linkText(TestContext.lang.getProperty("public.dataset.downloadDatasetXML"))).getAttribute("href"), true));

        // open the offline xml file
        // get project path
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        // open the file from the project/src/xmls
        log.trace("Open offline file reference-xmls\\" + xml);
        BufferedReader off = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("./reference-xmls/" + xml)));
        String line1 = "";
        String line2;
        // compare line by line the stored and the online file
        while (on.hasNextLine()) {
            line1 = on.nextLine();
            line2 = off.readLine();
            if (on.hasNextLine()) { // because someimes the last line of the string is broken, constrain only the half
                // of the actual string. So doesn't compare those
                if (line1.compareTo(line2) != 0)
                    org.testng.Assert.fail(TestContext.lang.getProperty("common." + menu)
                            + " download error. The stored and the downloaded xml file isn't the same. Difference: '" + line1 + "' vs. '" + line2 + "'");
            }
        }
        on.close();
        off.close();
        log.info(menu + "test finished.");
    }
}
