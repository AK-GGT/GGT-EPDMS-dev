package eu.europa.ec.jrc.lca.registry.test.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;

public class T005Registry_DatasetsManagementTest {

    protected final static Logger log = LogManager.getLogger(T005Registry_DatasetsManagementTest.class);

    @Test(priority = 51, dependsOnMethods = {"eu.europa.ec.jrc.lca.registry.test.ui.T004SynchronizeAndRegistryDatasetsTest.registryDatasets"})
    public static void rejectRegistryDatasets() throws InterruptedException {
        log.debug("'Reject Registry Datasets' test started");

        Main.getInstance().getDriver().manage().deleteAllCookies();
        TestFunctions.loginReg("admin", "test", true, true, 0);

        rejectRegistryDataset("3", "", false);
        rejectRegistryDataset("3", "reason", true);

        log.info("'Reject Registry Datasets' test finished");
    }

    @Test(priority = 52, dependsOnMethods = {"rejectRegistryDatasets"})
    public static void acceptRegistryDatasets() throws InterruptedException {
        log.debug("'Accept Registry Datasets' test started");

        Main.getInstance().getDriver().manage().deleteAllCookies();
        TestFunctions.loginReg("admin", "test", true, true, 0);

        acceptRegistryDataset("2", true);

        log.info("'Accept Registry Datasets' test finished");
    }

    @Test(priority = 53, dependsOnMethods = {"acceptRegistryDatasets"})
    public static void deregisterRegistryDatasets() throws InterruptedException {
        log.debug("'Deregister Registry Datasets' test started");

        Main.getInstance().getDriver().manage().deleteAllCookies();
        TestFunctions.loginReg("admin", "test", true, true, 0);

        deregisterRegistryDataset("1", "", false);
        deregisterRegistryDataset("1", "test", true);

        log.info("'Deregister Registry Datasets' test finished");
    }

    public static void acceptRegistryDataset(String dsNr, boolean correct) throws InterruptedException {
        log.info("Accept registry dataset " + dsNr + " test started");

        TestFunctions.findAndWaitOnElement(By.linkText(Main.regMsg.getProperty("menu_datasetsToApprove"))
        ).click();

        TestFunctions.findAndWaitOnElement(By.xpath("//td/div/div[2]")).click();
        TestFunctions.findAndWaitOnElement(By.xpath("//tr[" + dsNr + "]/td/div/div[2]")).click();
        Thread.sleep(Main.wait);

        // Are you sure? - Click OK

        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.regMsg.getProperty(
                "listDatasets_acceptSelected") + "')]")).click();

        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.regMsg.getProperty(
                "button_yes"
        ) + "')]")).click();

        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(".//*[@id='messages']/div")));
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

        if (correct) {
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.regMsg.getProperty("datasetsHaveBeenAccepted")))
                org.testng.Assert.fail("Error.");
            else {
                log.info("Registry dataset " + dsNr + " accepted successfully");
            }
        } else {
            org.testng.Assert.fail(TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText());
        }

        log.info("Accept registry datasets test finished");
    }

    public static void rejectRegistryDataset(String dsNr, String reason, boolean correct) throws InterruptedException {
        log.info("Reject registry dataset nr" + dsNr + " test started");

        TestFunctions.findAndWaitOnElement(By.linkText(Main.regMsg.getProperty("menu_datasetsToApprove"))
        ).click();

        TestFunctions.findAndWaitOnElement(By.xpath("//tr[" + dsNr + "]/td/div/div[2]")).click();
        Thread.sleep(Main.wait);

        // Are you sure? - Click OK
        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.regMsg.getProperty(
                "listDatasets_rejectSelected") + "')]")).click();
        if (reason.length() > 0) {
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='rejectionForm:reason']")).clear();
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='rejectionForm:reason']")).sendKeys(reason);
            Thread.sleep(Main.wait);
        }
        TestFunctions.findAndWaitOnElement(By.xpath("//form[@id='rejectionForm']/button[span[contains(.,'" + Main.regMsg.getProperty(
                "button_yes"
        ) + "')]]")).click();
        Thread.sleep(Main.wait);

        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(".//*[@id='messages']/div")));
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

        if (correct) {
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.regMsg.getProperty("datasetsHaveBeenRejected").substring(0, 10)))
                org.testng.Assert.fail("Error.");
            else {
                log.info("Registry dataset " + dsNr + " rejected successfully");
            }
        } else {
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.regMsg.getProperty("nodeDetailsPage_rejectionreason_required").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        }

        log.info("Reject registry datasets test finished");
    }

    public static void deregisterRegistryDataset(String dsNr, String reason, boolean correct) throws InterruptedException {
        log.info("Deregister registry dataset " + dsNr + " test started");

        TestFunctions.findAndWaitOnElement(By.linkText(Main.regMsg.getProperty("menu_registeredDataSets"))
        ).click();
        Thread.sleep(Main.wait);

        TestFunctions.findAndWaitOnElement(By.xpath("//tr[" + dsNr + "]/td/div/div[2]")).click();
        Thread.sleep(Main.wait);

        // Are you sure? - Click OK
        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.regMsg.getProperty(
                "listDatasets_deregisterSelected") + "')]")).click();
        if (reason.length() > 0) {
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='deregistrationForm:deregistrationreason']")).clear();
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='deregistrationForm:deregistrationreason']")).sendKeys(reason);
            Thread.sleep(Main.wait);
        }
        TestFunctions.findAndWaitOnElement(By.xpath("//form[@id='deregistrationForm']/button[span[contains(.,'" + Main.regMsg.getProperty(
                "button_yes"
        ) + "')]]")).click();
        Thread.sleep(Main.wait);

        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(".//*[@id='messages']/div")));
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

        if (correct) {
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.regMsg.getProperty("datasetsHaveBeenDeregistered").substring(0, 10)))
                org.testng.Assert.fail("Error.");
            else {
                log.info("Registry dataset " + dsNr + " deregistered successfully");
            }
        } else {
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.regMsg.getProperty("listNodes_deregistrationreason_required").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        }

        log.info("Deregister registry datasets test finished");
    }

}