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

public class T006RegistriesEndTest {

    protected final static Logger log = LogManager.getLogger(T006RegistriesEndTest.class);

    public void deregisterRegistry(String name, String account, String password, boolean correct, boolean dsExist) throws InterruptedException {
        log.trace("Deregister registry " + name);

        Actions action = new Actions(Main.getInstance().getDriver());
        Thread.sleep(Main.wait);
        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.network")))).build().perform();
        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("common.adminMenu.registries")))).click()
                .build().perform();
        Thread.sleep(Main.wait);
        // wait for the site to load
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(Main.lang
                .getProperty("public.user.logout"))));

        log.trace("Find registry to deregister");
        int i = 1;

        while (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[1]/a")).getText()
                .contains(name))
            i++;

        Thread.sleep(Main.wait);

        TestFunctions.findAndWaitOnElement(
                By.xpath("(.//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[4]//a[contains(text(),'"
                        + Main.lang.getProperty("admin.listRegistries.actionDeregister") + "')])")).click();

        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='accessAccount']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='accessAccount']")).sendKeys(account);
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='accessPassword']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='accessPassword']")).sendKeys(password);

        Thread.sleep(Main.wait);
        // Click Save
        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.lang.getProperty("admin.deregisterNode.deregisterButton") + "')]"))
                .click();

        Thread.sleep(Main.wait);

        // Sure? Click OK
        TestFunctions.findAndWaitOnElement(By.xpath(".//button[contains(.,'" + Main.lang.getProperty("common.button.yes") + "')]")).click();

        // TODO - check if no message is correct
        if (correct) {
            TestFunctions.checkNoMessage();
            if (Main.getInstance().getDriver().getTitle().contains("Error information page"))
                org.testng.Assert.fail("Error.");
            else {
                log.trace("Registry " + name + " deregistered successfully");
            }
        } else {
            (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='messages']/div")));
            TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));
            if (dsExist) {
                if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                        Main.lang.getProperty("admin.deregisterNode.nodeDegistrationException").substring(0, 10)))
                    org.testng.Assert.fail("Error.");
            } else {
                if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                        Main.lang.getProperty("authenticationException_errorMessage").substring(0, 10)))
                    org.testng.Assert.fail("Error.");
            }
        }
    }

    public void deregisterDataset(String regName, String reason, boolean correct) throws InterruptedException {
        log.trace("Deregister dataset " + regName);

        Actions action = new Actions(Main.getInstance().getDriver());
        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.dataset.manageList")))).build()
                .perform();
        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.process.manageList")))).click()
                .build().perform();
        //if tests are running after Node tests, if not - change data stock on 'default'
        log.trace("Change datastock");
        // Choose data stock RootStock1
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_header']/div[2]//table/tbody/tr[1]/td[2]/div/div[3]/span")).click();
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@data-label='RootStock1']")).click();
        // wait
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='processTable_data']/tr[1]/td[1]/div/div[2]/span")));

        // wait for the site to load
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(Main.lang
                .getProperty("public.user.logout"))));

        new Select(TestFunctions.findAndWaitOnElement(By.xpath("//select[@id='reg']"))).selectByVisibleText(regName);
        Thread.sleep(Main.wait);

        TestFunctions.findAndWaitOnElement(By.xpath("//div[2]/table/tbody/tr/td/div/div[2]/span")).click();
        TestFunctions.findAndWaitOnElement(By.xpath("//button[@id='btnDeregisterSelected']")).click();

        if (reason.length() > 0) {
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='deregistrationreason']")).clear();
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='deregistrationreason']")).sendKeys(reason);
            Thread.sleep(Main.wait);
        }

        // Sure? Click OK
        TestFunctions.findAndWaitOnElement(By.xpath(".//button[contains(.,'" + Main.lang.getProperty("common.button.yes") + "')]")).click();

        // check the message
        if (correct) {
            TestFunctions.checkNoMessage();
            if (!Main.getInstance().getDriver().getCurrentUrl().contains("manageProcessList.xhtml"))
                org.testng.Assert.fail("After registering datasets, it doesn't go to manageProcessList.xhtml but goes to "
                        + Main.getInstance().getDriver().getCurrentUrl());
        } else {
            (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='messages']/div")));
            TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));
            if (!Main.getInstance().getDriver().getTitle().contains("Error information page"))
                org.testng.Assert.fail("Error.");
        }
    }

    @Test(priority = 61, dependsOnMethods = {"eu.europa.ec.jrc.lca.registry.test.ui.T005Registry_DatasetsManagementTest.deregisterRegistryDatasets"})
    public void deregisterRegistries() throws InterruptedException {
        log.info("'Deregister registries' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        Thread.sleep(Main.wait);
        // click on Admin area
        TestFunctions.gotoAdminArea();

        deregisterRegistry("registry1", "admin", "test", false, true); // exist ds -> deregister ds

        log.info("'Deregister registries' test finished");
    }

    @Test(priority = 62, dependsOnMethods = {"deregisterRegistries"})
    public void deregisterDatasets() throws InterruptedException {
        log.info("'Deregister registries' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        // login as admin
        TestFunctions.login("admin", "default", true, true);

        // click on Admin area
        TestFunctions.gotoAdminArea();

        deregisterDataset("registry1", "reason", true);

        log.info("'Deregister registries' test finished");
    }

    @Test(priority = 63, dependsOnMethods = {"deregisterDatasets"})
    public void deregisterRegistry() throws InterruptedException {
        log.info("'Deregister registries' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        // click on Admin area
        TestFunctions.gotoAdminArea();

        deregisterRegistry("registry1", "admin", "a", false, false);
        deregisterRegistry("registry1", "admin", "default", true, false);

        log.info("'Deregister registries' test finished");
    }

}