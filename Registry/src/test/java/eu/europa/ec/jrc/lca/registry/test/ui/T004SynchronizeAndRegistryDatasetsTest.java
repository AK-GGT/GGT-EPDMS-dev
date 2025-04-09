package eu.europa.ec.jrc.lca.registry.test.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;

public class T004SynchronizeAndRegistryDatasetsTest {

    protected final static Logger log = LogManager.getLogger(T004SynchronizeAndRegistryDatasetsTest.class);

    public void synchronizeData(String name, boolean correct) throws InterruptedException {
        log.trace("Synchronize data for registry " + name);

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        Actions action = new Actions(Main.getInstance().getDriver());
        // click on Admin area
        TestFunctions.gotoAdminArea();

        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.network")))).build().perform();
        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("common.adminMenu.registries")))).click()
                .build().perform();
        Thread.sleep(Main.wait);
        // SYNCHRONIZE
        log.trace("Find registry to synchronize");
        int i = 1;

        while (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[1]/a")).getText()
                .contains(name))
            i++;

        Thread.sleep(Main.wait);

        log.debug("Synchronize data");
        TestFunctions.findAndWaitOnElement(By.linkText(name)).click();
        Thread.sleep(Main.wait);

        // Click Synchronize
        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.lang.getProperty("admin.registryDetails.synchronize") + "')]"))
                .click();
        // wait a little
        Thread.sleep(3 * Main.wait);

        // check the message
        if (correct) {
            (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='messages']/div")));
            TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.lang.getProperty("admin.registryDetails.successfull_synchronization").substring(0, 10)))
                org.testng.Assert.fail("Error.");
            else {
                log.trace("Registry " + name + " synchronized successfully");
            }
        } else {
            if (!Main.getInstance().getDriver().getTitle().contains("Error information page"))
                org.testng.Assert.fail("Error.");
        }

    }

    public void registryDataset(String regName, boolean correct) throws InterruptedException {
        Actions action = new Actions(Main.getInstance().getDriver());
        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.dataset.manageList")))).build()
                .perform();
        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.process.manageList")))).click()
                .build().perform();
        Thread.sleep(Main.wait);
        //if tests are running after Node tests, if not - change data stock on 'default'
        log.trace("Change datastock");
        // Choose data stock RootStock1
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_header']/div[2]//table/tbody/tr[1]/td[2]/div/div[3]/span")).click();
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@data-label='RootStock1']")).click();
        // wait
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='processTable_data']/tr[1]/td[1]/div/div[2]/span")));

        // select items
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='processTable_data']/tr[1]/td/div/div[2]/span")).click();
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='processTable_data']/tr[2]/td/div/div[2]/span")).click();
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='processTable_data']/tr[3]/td/div/div[2]/span")).click();

        // wait for Register button to enabled
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.elementToBeClickable(By
                .xpath(".//*[@id='btnRegisterSelected']")));

        // click Register
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='btnRegisterSelected']")).click();
        Thread.sleep(Main.wait);
        // select registry
        new Select(TestFunctions.findAndWaitOnElement(By.xpath("//select[@id='reg']"))).selectByVisibleText(regName);
        Thread.sleep(Main.wait);
        // click Register
        TestFunctions.findAndWaitOnElement(By.xpath("//button[@id='btnRegister']")).click();

        Thread.sleep(Main.wait);

        // check the message
        if (correct) {
            TestFunctions.checkNoMessage();
            if (!Main.getInstance().getDriver().getCurrentUrl().contains("datasetRegistrationSummary.xhtml"))
                org.testng.Assert.fail("After registering datasets, it doesn't go to datasetRegistrationSummary.xhtml but goes to "
                        + Main.getInstance().getDriver().getCurrentUrl());
        } else {
            if (!Main.getInstance().getDriver().getTitle().contains("Error information page"))
                org.testng.Assert.fail("Error.");
        }

    }

    @Test(priority = 41, dependsOnMethods = {"eu.europa.ec.jrc.lca.registry.test.ui.T003Registry_NodesManagementTest.acceptRegistryNodeAfterDeregistration"})
    public void synchronizeData() throws InterruptedException {
        log.info("'Synchronize data' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        Thread.sleep(Main.wait);
        synchronizeData("registry2", false);
        synchronizeData("registry1", true);

        log.info("'Synchronize data' test finished");
    }

    @Test(priority = 42, dependsOnMethods = {"synchronizeData"})
    public void registryDatasets() throws InterruptedException {
        log.info("'Registry datasets' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        Thread.sleep(Main.wait);
        // click on Admin area
        TestFunctions.gotoAdminArea();

        registryDataset("registry1", true);

        log.info("'Registry datasets' test finished");
    }

}