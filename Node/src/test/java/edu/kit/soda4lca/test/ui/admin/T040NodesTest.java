package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Listeners({ScreenShooter.class})
public class T040NodesTest extends AbstractUITest {

    protected final static Logger log = LogManager.getLogger(T040NodesTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    @Test(priority = 401)
    public void newNodes() throws InterruptedException {
        log.info("'Create new nodes' test started");
        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);

        // click on Admin area
        testFunctions.gotoAdminArea();

        // add nodes
        newNode("http://localhost:8080/", "", "", false, false);
        newNode("http://localhost:8080/Node2", "admin", "test", true, false);
        newNode("http://localhost:8080/Registry", "", "", false, false);
        newNode("http://localhost:8080/Node2", "admin", "def", false, true);

        log.info("'Create new nodes' test finished");
    }

    @Test(priority = 402, dependsOnMethods = {"newNodes"})
    public void manageNodes() throws InterruptedException {
        log.info("'Manage nodes' test started");
        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);

        // click on Admin area
        testFunctions.gotoAdminArea();

        Actions action = new Actions(driver);
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.network")))).build().perform();
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.node.manageList")))).click().build()
                .perform();
        Thread.sleep(3 * TestContext.wait);
        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(TestContext.lang
                .getProperty("public.user.logout"))));

        // edit node
        editNode("ACME2", "def", true);
        Thread.sleep(3 * TestContext.wait);

        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.network")))).build().perform();
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.node.manageList")))).click().build()
                .perform();
        Thread.sleep(3 * TestContext.wait);

        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(TestContext.lang
                .getProperty("public.user.logout"))));

        // delete node
        deleteNode("ACME2", true);

        log.info("'Manage nodes' test finished");

    }

    public void newNode(String www, String login, String password, boolean correct, boolean exist) throws InterruptedException {
        log.trace("Creating new node " + www);

        // Create an action for mouse-moves
        Actions action = new Actions(driver);
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.network")))).build().perform();
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.node.new")))).click().build()
                .perform();
        Thread.sleep(3 * TestContext.wait);
        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(TestContext.lang
                .getProperty("public.user.logout"))));
        // Fill in the form
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='baseUrl']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='baseUrl']")).sendKeys(www);
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='login']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='login']")).sendKeys(login);
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='passw']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='passw']")).sendKeys(password);
        Thread.sleep(3 * TestContext.wait);

        // Click Save
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.node.new") + "')]")).click();
        // wait a little
        Thread.sleep(3 * TestContext.wait);
        // check the message
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(".//*[@id='messages']/div")));
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

        if (correct) {
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    TestContext.lang.getProperty("facesMsg.node.addSuccess").substring(0, 10)))
                org.testng.Assert.fail("Error.");
            else {
                log.trace("Node " + www + " created succesfull");
            }
        } else if (exist) {
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    TestContext.lang.getProperty("facesMsg.node.alreadyExists").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        } else {
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    TestContext.lang.getProperty("facesMsg.node.infoError").substring(0, 10))
                    && !testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    TestContext.lang.getProperty("facesMsg.node.noID").substring(0, 10)))
                org.testng.Assert.fail("Error.");
        }
    }

    public void editNode(String nodeId, String password, boolean correct) throws InterruptedException {
        log.trace("Edit node " + nodeId);

        // EDIT
        log.trace("Find node to edit");
        int i = 1;

        while (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='nodeTable_data']/tr[" + Integer.toString(i) + "]/td[2]/a")).getText().contains(
                nodeId))
            i++;

        Thread.sleep(3 * TestContext.wait);

        // CHANGE
        log.debug("Change a password");

        testFunctions.findAndWaitOnElement(By.linkText(nodeId)).click();
        testFunctions.findAndWaitOnElement(By.xpath("//input[@id='passw']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath("//input[@id='passw']")).sendKeys(password);
        Thread.sleep(3 * TestContext.wait);

        // Click Save
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.node.updateNode") + "')]")).click();
        // wait a little
        Thread.sleep(3 * TestContext.wait);

        if (correct) {
            (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='messages']/div")));
            testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    TestContext.lang.getProperty("facesMsg.node.saveSuccess").substring(0, 10)))
                org.testng.Assert.fail("Error.");
            else {
                log.trace("Node " + nodeId + " updated succesfull");
            }
        }
    }

    public void deleteNode(String nodeId, boolean correct) throws InterruptedException {
        log.trace("Delete node " + nodeId);

        // DELETE
        log.trace("Find node to delete");
        int i = 1;

        while (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='nodeTable_data']/tr[" + Integer.toString(i) + "]/td[2]/a")).getText().contains(
                nodeId))
            i++;

        Thread.sleep(3 * TestContext.wait);

        log.debug("Delete node");
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='nodeTable_data']/tr[" + Integer.toString(i) + "]/td[1]/div/div[2]")).click();
        Thread.sleep(3 * TestContext.wait);
        // Click 'Delete selected entries'
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='deleteBtn']")).click();
        // Are you sure? - Click OK
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.ok") + "')]")).click();
        log.trace("Check if deleted succesfully");

        // wait a little
        Thread.sleep(3 * TestContext.wait);

        if (correct) {
            (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                    .xpath(".//*[@id='messages']/div")));
            testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']/div"));

            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                    TestContext.lang.getProperty("facesMsg.removeSuccess").substring(0, 10)))
                org.testng.Assert.fail("Error.");
            else {
                log.trace("Node " + nodeId + " deleted successfully");
            }
        }
    }
}
