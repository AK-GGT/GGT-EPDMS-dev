package eu.europa.ec.jrc.lca.registry.test.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Properties;

public class T002RegistriesTest {

    protected final static Logger log = LogManager.getLogger(T002RegistriesTest.class);

    public void newRegistry(String name, String uuid, String www, String description, boolean correct, boolean exist)
            throws InterruptedException {
        log.trace("Creating new registry " + name);

        // Click the menu
        // Create an action for mouse-moves
        Actions action = new Actions(Main.getInstance().getDriver());
        Thread.sleep(Main.wait);
        action.moveToElement(
                        Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.network")))).build()
                .perform();
        action.moveToElement(Main.getInstance().getDriver()
                        .findElement(By.linkText(Main.lang.getProperty("common.adminMenu.registries")))).click().build()
                .perform();
        Thread.sleep(3 * Main.wait);
        // wait for the site to load
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions
                .visibilityOfElementLocated(By.linkText(Main.lang.getProperty("public.user.logout"))));
        Thread.sleep(Main.wait);
        // Fill in the form
        TestFunctions
                .findAndWaitOnElement(By.xpath(
                        "//button[contains(.,'" + Main.lang.getProperty("admin.listRegistries.addRegistry") + "')]"))
                .click();
        Thread.sleep(3 * Main.wait);

        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryNameE']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryNameE']")).sendKeys(name);
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryUUIDE']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryUUIDE']")).sendKeys(uuid);
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryBaseUrlE']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryBaseUrlE']")).sendKeys(www);
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryDescriptionE']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryDescriptionE']")).sendKeys(description);
        Thread.sleep(3 * Main.wait);
        // Click Save
        TestFunctions
                .findAndWaitOnElement(
                        By.xpath("//button[contains(.,'" + Main.lang.getProperty("admin.registryDetails.save") + "')]"))
                .click();
        // wait a little
        Thread.sleep(3 * Main.wait);

        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='messages']/div")));
        TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

        // check the message
        if (correct) {
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText()
                    .contains(Main.lang.getProperty("admin.registryDetails.registryHasBeenSaved").substring(0, 10)))
                org.testng.Assert.fail("Error.");
            else {
                log.trace("Registry " + name + " created succesfull");
            }
        } else if (exist) {
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText()
                    .contains(Main.lang.getProperty("registryWithSameUrlAlreadyExists").substring(0, 10))
                    && !TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText()
                    .contains(Main.lang.getProperty("registryWithSameUUIDAlreadyExists").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        } else {
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains("required")
                    && !TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    Main.lang.getProperty("admin.registryDetails.baseUrlValidationMessage").substring(0, 10))) {
                org.testng.Assert.fail("Error.");
            }
        }
    }

    public void editRegistry(String name, String description, boolean correct) throws InterruptedException {
        log.trace("Edit registry " + name);

        // EDIT
        log.trace("Find registry to edit");
        int i = 1;

        while (!TestFunctions
                .findAndWaitOnElement(
                        By.xpath(".//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[1]/a"))
                .getText().contains(name))
            i++;

        Thread.sleep(3 * Main.wait);

        TestFunctions.findAndWaitOnElement(By.xpath("(.//*[@id='registriesTable_data']/tr[" + Integer.toString(i)
                        + "]/td[4]//a[contains(text(),'" + Main.lang.getProperty("admin.listRegistries.editRegistry") + "')])"))
                .click();

        Thread.sleep(3 * Main.wait);

        // CHANGE
        log.debug("Change a description");
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryDescriptionE']")).clear();
        TestFunctions.findAndWaitOnElement(By.xpath("//input[@id='registryDescriptionE']")).sendKeys(description);
        Thread.sleep(3 * Main.wait);

        // Click Save
        TestFunctions
                .findAndWaitOnElement(
                        By.xpath("//button[contains(.,'" + Main.lang.getProperty("admin.registryDetails.save") + "')]"))
                .click();
        // wait a little
        Thread.sleep(3 * Main.wait);
    }

    public void deleteRegistry(String name, boolean correct) throws InterruptedException {
        log.trace("Deleting registry " + name);

        // DELETE
        log.debug("Delete registry");
        log.trace("Find registry to delete");
        int i = 1;

        while (!TestFunctions
                .findAndWaitOnElement(
                        By.xpath(".//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[1]/a"))
                .getText().contains(name))
            i++;

        Thread.sleep(3 * Main.wait);

        // Click 'Delete selected entries'
        TestFunctions.findAndWaitOnElement(
                        By.xpath("(.//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[4]//a[contains(text(),'"
                                + Main.lang.getProperty("admin.listRegistries.deleteRegistry") + "')])"))
                .click();

        // Are you sure? - Click OK
        TestFunctions
                .findAndWaitOnElement(
                        By.xpath(".//button[contains(.,'" + Main.lang.getProperty("common.button.yes") + "')]"))
                .click();
        log.trace("Check if deleted succesfully");

        // Check the message
        if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText()
                .contains(Main.lang.getProperty("admin.deleteRegistry.registryHasBeenRemoved").substring(0, 10)))
            org.testng.Assert.fail(
                    "Wrong message: " + TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText());

    }

    public void register(String name, String nodeId, String nodeWww, String login, String password, boolean correct)
            throws InterruptedException {
        log.trace("Register registry " + name);

        Actions action = new Actions(Main.getInstance().getDriver());
        Thread.sleep(Main.wait);
        // registry
        action.moveToElement(
                        Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.network")))).build()
                .perform();
        action.moveToElement(Main.getInstance().getDriver()
                        .findElement(By.linkText(Main.lang.getProperty("common.adminMenu.registries")))).click().build()
                .perform();
        Thread.sleep(3 * Main.wait);
        // wait for the site to load
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions
                .visibilityOfElementLocated(By.linkText(Main.lang.getProperty("public.user.logout"))));
        Thread.sleep(3 * Main.wait);
        log.trace("Find registry to register");
        int i = 1;

        while (!TestFunctions
                .findAndWaitOnElement(
                        By.xpath(".//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[1]/a"))
                .getText().contains(name))
            i++;

        Thread.sleep(3 * Main.wait);

        // Click 'Delete selected entries'
        TestFunctions.findAndWaitOnElement(
                        By.xpath("(.//*[@id='registriesTable_data']/tr[" + Integer.toString(i) + "]/td[4]//a[contains(text(),'"
                                + Main.lang.getProperty("admin.listRegistries.actionRegister") + "')])"))
                .click();

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
        // Click Save
        TestFunctions
                .findAndWaitOnElement(By.xpath(
                        "//button[contains(.,'" + Main.lang.getProperty("admin.registerNode.registerButton") + "')]"))
                .click();
        // wait a little
        Thread.sleep(3 * Main.wait);

        // check the message
        if (correct) {
            TestFunctions.checkNoMessage();
            log.trace("Registry " + name + " registered successfully");
        } else {
            (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout)))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[@id='messages']/div")));
            TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));
            if (!TestFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText()
                    .contains(Main.lang.getProperty("restWSUnknownException_errorMessage").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        }

    }

    @Test(priority = 21, dependsOnMethods = {"eu.europa.ec.jrc.lca.registry.test.ui.T001Registry_LoginTest.login"})
    public void newRegistries() throws InterruptedException {
        log.info("'Create new registries' test started");

        WebDriver ff = Main.getInstance().getDriver();
        String site = Main.site;
        Properties lang = Main.lang;
        // delete previous session
        ff.manage().deleteAllCookies();

        // open the main site
        ff.get(site);

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        // click on Admin area
        TestFunctions.gotoAdminArea();

        // add registries
        newRegistry("registry1", "uuid_registry1",
                "http://localhost:" + Main.getMavenUrlProperty("test.app.registry.port") + "/Registry/",
                "registry description", true, false);
        newRegistry("registry2", "uuid_registry2", "http://localhost:8083/Registry/", "", true, false);
        if (!Main.fast) {
            newRegistry("registry3", "uuid_registry3", "http://localhost:8082/Registry/", "", true, false);
            newRegistry("", "", "", "", false, false);
            newRegistry("registry4", "", "", "", false, false);
            newRegistry("", "uuid_registry4", "", "", false, false);
            newRegistry("", "", "http://localhost:8083/Registry/", "", false, false);
            newRegistry("registry4", "uuid_registry3", "http://localhost:8083/Registry/", "", false, true);
            newRegistry("registry4", "uuid_registry4", "http://localhost:8082/Registry/", "", false, true);
            newRegistry("registry4", "uuid_registry4", "http://localhost:8083/Registry", "", false, false);
            newRegistry("registry4", "uuid_registry4", "http://localhost:8084/Registry/", "", true, false);
        }
        log.info("'Create new registries' test finished");
    }

    @Test(priority = 22, dependsOnMethods = {"newRegistries"})
    public void editRegistries() throws InterruptedException {
        if (Main.fast)
            return;

        log.info("'Edit registries' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        // click on Admin area
        TestFunctions.gotoAdminArea();

        Actions action = new Actions(Main.getInstance().getDriver());

        action.moveToElement(
                        Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.network")))).build()
                .perform();
        action.moveToElement(Main.getInstance().getDriver()
                        .findElement(By.linkText(Main.lang.getProperty("common.adminMenu.registries")))).click().build()
                .perform();
        // wait for the site to load
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions
                .visibilityOfElementLocated(By.linkText(Main.lang.getProperty("public.user.logout"))));

        // edit registry
        editRegistry("registry3", "desc reg3", true);
        editRegistry("registry4", "desc reg4", true);

        log.info("'Edit registries' test finished");
    }

    @Test(priority = 23, dependsOnMethods = {"editRegistries"})
    public void deleteRegistries() throws InterruptedException {
        if (Main.fast)
            return;

        log.info("'Delete registries' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        // click on Admin area
        TestFunctions.gotoAdminArea();

        Actions action = new Actions(Main.getInstance().getDriver());

        action.moveToElement(
                        Main.getInstance().getDriver().findElement(By.linkText(Main.lang.getProperty("admin.network")))).build()
                .perform();
        action.moveToElement(Main.getInstance().getDriver()
                        .findElement(By.linkText(Main.lang.getProperty("common.adminMenu.registries")))).click().build()
                .perform();
        // wait for the site to load
        (new WebDriverWait(Main.getInstance().getDriver(), Duration.ofMillis(Main.timeout))).until(ExpectedConditions
                .visibilityOfElementLocated(By.linkText(Main.lang.getProperty("public.user.logout"))));

        // remove registries
        deleteRegistry("registry4", true);
        deleteRegistry("registry3", true);

        log.info("'Delete registries' test finished");
    }

    @Test(priority = 24, dependsOnMethods = {"deleteRegistries"})
    public void registerRegistries() throws InterruptedException {
        log.info("'Register registries' test started");
        Main.getInstance().getDriver().manage().deleteAllCookies();

        // login as admin
        TestFunctions.login("admin", "default", true, true);
        Thread.sleep(Main.wait);
        // click on Admin area
        TestFunctions.gotoAdminArea();

        register("registry2", "node1", "http://localhost:8082/Node", "admin", "default", false);
        register("registry1", "node1", "http://localhost:" + Main.getMavenUrlProperty("test.app.node.port") + "/Node",
                "admin", "default", true);

        log.info("'Register registries' test finished");

    }
}
