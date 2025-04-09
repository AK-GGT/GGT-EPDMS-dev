package eu.europa.ec.jrc.lca.registry.test.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;

public class T003Registry_NodesManagementTest {

    protected final static Logger log = LogManager.getLogger(T003Registry_NodesManagementTest.class);

    @Test(priority = 31, dependsOnMethods = {"eu.europa.ec.jrc.lca.registry.test.ui.T002RegistriesTest.registerRegistries"})
    public void rejectRegistryNodes() throws InterruptedException {
        log.debug("'Reject Registry Nodes' test started");

        Main.getInstance().getDriver().manage().deleteAllCookies();
        TestFunctions.loginReg("admin", "test", true, true, 0);

        rejectRegistryNode("node1", "", false);
        rejectRegistryNode("node1", "reason", true);

        log.info("'Reject Registry Nodes' test finished");
    }

    @Test(priority = 32, dependsOnMethods = {"rejectRegistryNodes"})
    public void registerRegistryForDeregister() throws InterruptedException {
        log.info("'Register registries' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        // click on Admin area
        TestFunctions.gotoAdminArea();

        register("registry1", "NODEID", "http://localhost:" + Main.getMavenUrlProperty("test.app.node.port") + "/Node", "admin", "default", true);

        log.info("'Register registries' test finished");

    }

    @Test(priority = 33, dependsOnMethods = {"registerRegistryForDeregister"})
    public void acceptRegistryNodes() throws InterruptedException {
        log.debug("'Accept Registry Nodes' test started");

        Main.getInstance().getDriver().manage().deleteAllCookies();
        TestFunctions.loginReg("admin", "test", true, true, 0);

        acceptRegistryNode("NODEID", true);

        log.info("'Accept Registry Nodes' test finished");
    }

    @Test(priority = 34, dependsOnMethods = {"acceptRegistryNodes"})
    public void deregisterRegistryNodes() throws InterruptedException {
        log.debug("'Deregister Registry Nodes' test started");

        Main.getInstance().getDriver().manage().deleteAllCookies();
        TestFunctions.loginReg("admin", "test", true, true, 0);

        deregisterRegistryNode("NODEID", "", false);
        deregisterRegistryNode("NODEID", "test", true);

        log.info("'Deregister Registry Nodes' test finished");
    }

    @Test(priority = 35, dependsOnMethods = {"deregisterRegistryNodes"})
    public void registerRegistryAfterDeregistration() throws InterruptedException {
        log.info("'Register registries' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        // click on Admin area
        TestFunctions.gotoAdminArea();

        register("registry1", "node1", "http://localhost:" + Main.getMavenUrlProperty("test.app.node.port") + "/Node", "admin", "default", true);

        log.info("'Register registries' test finished");
    }

    @Test(priority = 36, dependsOnMethods = {"registerRegistryAfterDeregistration"})
    public void acceptRegistryNodeAfterDeregistration() throws InterruptedException {
        log.debug("'Accept Registry Nodes' test started");

        Main.getInstance().getDriver().manage().deleteAllCookies();
        TestFunctions.loginReg("admin", "test", true, true, 0);

        acceptRegistryNode("node1", true);

        log.info("'Accept Registry Nodes' test finished");
    }

    public void acceptRegistryNode(String nodeId, boolean correct) throws InterruptedException {
        log.info("Accept registry node " + nodeId + " test started");

        TestFunctions.findAndWaitOnElement(By.linkText(Main.regMsg.getProperty("menu_nodesToApprove"))).click();

        TestFunctions.findAndWaitOnElement(By.xpath("//a[contains(text(),'" + nodeId + "')]")).click();
        Thread.sleep(Main.wait);

        // Are you sure? - Click OK
        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.regMsg.getProperty(
                "nodeDetailsPage_acceptRegistration") + "')]")).click();

        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.regMsg.getProperty(
                "button_yes"
        ) + "')]")).click();

        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(".//*[@id='messages']/div")));
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

        if (correct) {
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.regMsg.getProperty("registrationRequestHasBeenAccepted").substring(0, 10)))
                org.testng.Assert.fail("Error.");
            else {
                log.info("Registry node " + nodeId + " accepted successfully");
            }
        } else {
            (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='messages']/div")));
            TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

            org.testng.Assert.fail(TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText());
        }

        log.info("Accept registry nodes test finished");
    }

    public void rejectRegistryNode(String nodeId, String reason, boolean correct) throws InterruptedException {
        log.info("Reject registry node " + nodeId + " test started");

        TestFunctions.findAndWaitOnElement(By.linkText(Main.regMsg.getProperty("menu_nodesToApprove"))
        ).click();

        TestFunctions.findAndWaitOnElement(By.xpath("//a[contains(text(),'" + nodeId + "')]")).click();
        Thread.sleep(Main.wait);

        // Are you sure? - Click OK
        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.regMsg.getProperty(
                "nodeDetailsPage_rejectRegistration") + "')]")).click();
        if (reason.length() > 0) {
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='nodeDetailsRejectionForm:reason']")).clear();
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='nodeDetailsRejectionForm:reason']")).sendKeys(reason);
            Thread.sleep(Main.wait);
        }
        TestFunctions.findAndWaitOnElement(By.xpath("//form[@id='nodeDetailsRejectionForm']/button[span[contains(.,'" + Main.regMsg.getProperty(
                "button_yes") + "')]]")).click();
        Thread.sleep(Main.wait);

        if (correct) {
            TestFunctions.checkNoMessage();
            log.info("Registry node " + nodeId + " rejected successfully");
        } else {
            (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='messages']/div")));
            TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.regMsg.getProperty("nodeDetailsPage_rejectionreason_required").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        }

        log.info("Reject registry nodes test finished");
    }

    public void deregisterRegistryNode(String nodeId, String reason, boolean correct) throws InterruptedException {
        log.info("Deregister registry node " + nodeId + " test started");

        TestFunctions.findAndWaitOnElement(By.linkText(Main.regMsg.getProperty("menu_registeredNodes"))
        ).click();

        int i = 1;

        while (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='nodesList:nodestable_data']/tr[" + Integer.toString(i) + "]/td[1]/a")).getText()
                .contains(
                        nodeId))
            i++;

        Thread.sleep(Main.wait);

        TestFunctions.findAndWaitOnElement(
                By.xpath("(.//*[@id='nodesList:nodestable_data']/tr[" + Integer.toString(i) + "]/td[5]//a[contains(text(),'"
                        + Main.regMsg.getProperty("listNodes_deregister") + "')])")).click();

        Thread.sleep(Main.wait);

        // Are you sure? - Click OK
        if (reason.length() > 0) {
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='nodesList:deregistrationreason']")).clear();
            TestFunctions.findAndWaitOnElement(By.xpath("//textarea[@id='nodesList:deregistrationreason']")).sendKeys(reason);
            Thread.sleep(Main.wait);
        }
        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.regMsg.getProperty(
                "button_yes"
        ) + "')]")).click();
        Thread.sleep(Main.wait);

        if (correct) {
            TestFunctions.checkNoMessage();
            log.info("Registry node " + nodeId + " deregistered successfully");
        } else {
            (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='nodesList:messages']/div")));
            TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='nodesList:messages']/div"));

            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='nodesList:messages']")).getText().contains(
                    Main.regMsg.getProperty("listNodes_deregistrationreason_required").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        }

        log.info("Deregister registry nodes test finished");
    }

    public void register(String name, String nodeId, String nodeWww, String login, String password, boolean correct) throws InterruptedException {
        log.trace("Register registry " + name);

        Actions action = new Actions(Main.getInstance().getDriver());

        // registry
        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.network")))).build().perform();
        action.moveToElement(Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("common.adminMenu.registries")))).click()
                .build().perform();
        Thread.sleep(3 * Main.wait);
        // wait for the site to load
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(Main.lang
                .getProperty("public.user.logout"))));

        log.trace("Find registry to register");
        int i = 1;

        while (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[1]/a")).getText()
                .contains(name))
            i++;

        Thread.sleep(3 * Main.wait);

        TestFunctions.findAndWaitOnElement(
                By.xpath("(.//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[4]//a[contains(text(),'"
                        + Main.lang.getProperty("admin.listRegistries.actionRegister") + "')])")).click();

        Thread.sleep(3 * Main.wait);

        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='nodeId']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='nodeId']")).sendKeys(nodeId);
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='baseURL']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='baseURL']")).sendKeys(nodeWww);
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='accessAccount']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='accessAccount']")).sendKeys(login);
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='accessPassword']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='accessPassword']")).sendKeys(password);

        Thread.sleep(3 * Main.wait);
        TestFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + Main.lang.getProperty("admin.registerNode.registerButton") + "')]"))
                .click();
        // wait a little
        Thread.sleep(3 * Main.wait);

        // check the message
        if (correct) {
            TestFunctions.checkNoMessage();
            log.trace("Registry " + name + " registered successfully");
        } else {
            (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='messages']/div")));
            TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.lang.getProperty("restWSUnknownException_errorMessage").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        }

    }
}