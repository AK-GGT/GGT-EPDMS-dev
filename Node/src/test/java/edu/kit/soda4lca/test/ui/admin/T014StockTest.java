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

/**
 * Add new Root & simple Data Stocks
 * assign users to them
 *
 * @author mark.szabo
 */
@Listeners({ScreenShooter.class})
public class T014StockTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T014StockTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T014StockTest.xml"));
    }

    /**
     * Creat new Stocks
     *
     * @throws InterruptedException
     */
    @Test(priority = 141)
    public void newStocks() throws InterruptedException {
        log.info("'Create new stocks' test started");
        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        // add stocks
        newStock("RootStock1", "First test root stock", "Organization1", true);
        newStock("RootStock2", "Second test root stock", "Organization2", true);
        newStock("RootStock3", "Third test root stock", "Organization1", true);
        newStock("SimpleStock1", "First test not-root stock", "Organization1", false);
        newStock("SimpleStock2", "Second test not-root stock", "Organization1", false);
        log.info("'Create new stocks' test finished");
    }

    /**
     * Manage Stocks. Delete one, then Assign users to other ones and add them rights
     *
     * @throws InterruptedException
     */
    @Test(priority = 142, dependsOnMethods = {"newStocks"})
    public void manageStocks() throws InterruptedException {
        log.info("'Delete stock and assign users to stocks' test started");

        // delet all the cookies
        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        log.debug("Delete stock");

        // DELETE A STOCK

        // Click the menu
        Actions action = new Actions(driver);
        // Hover over the menu
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("common.stock")))).build()
                .perform();
        Thread.sleep(TestContext.wait);
        // Click New Stock
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.stock.manageList"))))
                .click().build().perform();
        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .linkText(TestContext.lang.getProperty("public.user.logout"))));

        int i = 1;
        // search for 'RootStock3'
        while (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTable_data']/tr[" + Integer.toString(i) + "]/td[3]/a")).getText().contains(
                "RootStock3"))
            i++;

        // select it
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTable_data']/tr[" + Integer.toString(i) + "]/td[1]/div/div[2]")).click();

        // wait for the clientside script
        Thread.sleep(TestContext.wait);

        // Click 'Delete selected entries'
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='deleteBtn']")).click();

        Thread.sleep(TestContext.wait);

        // Are you sure? - Click OK
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.ok") + "')]")).click();

        Thread.sleep(TestContext.wait);

        // Check the message
        if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                TestContext.lang.getProperty("facesMsg.removeSuccess").substring(0, 10)))
            org.testng.Assert.fail("Wrong message: " + testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText());
        log.debug("Stock deleted succesfull");

        log.debug("Assign users to stock");
        // ADD USERS TO STOCK
        String users[] = {"Admin1", "User1", "User2", "User3", "User4", "User5", "User6", "User7"};
        Integer rights[] = {0b111111111, 0b011010011, 0b000000000, 0b100000000, 0b101000000, 0b100100000, 0b101100000, 0b011010011, 0b00000000};
        addUserToStock("RootStock2", users, rights, true);

        String users2[] = {"Admin2", "User1", "User2", "User3", "User4", "User5", "User6"};
        Integer rights2[] = {0b111111111, 0b011010011, 0b100100000, 0b100100000, 0b100100000, 0b100100000, 0b100100000, 0b00000000};
        // Integer rights2[] = { 511, 211, 32, 32, 32, 32, 32, 0 };
        addUserToStock("RootStock1", users2, rights2, true);

        log.debug("Users assigned succesfull");
        log.info("'Delete stock and assign users to stocks' test finished");
    }

    /**
     * Add new stock
     *
     * @param name         Name of the stock
     * @param title        Title
     * @param organization Organization
     * @param root         root stock (true), or non-root stock (false)
     * @throws InterruptedException
     */
    public void newStock(String name, String title, String organization, boolean root) throws InterruptedException {
        log.trace("Creating new stock " + name);
        // Click the menu
        // Create an action for mouse-moves
        Actions action = new Actions(driver);

        testFunctions.waitOnAdminArea();

        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("common.stock")))).build().perform();
        if (root)
            action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.rootStock.new")))).click().build().perform();
        else
            action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.stock.new"))))
                    .click().build().perform();

        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .linkText(TestContext.lang.getProperty("public.user.logout"))));
        Thread.sleep(TestContext.wait);
        // Fill in the form
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='stockTabs:name']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='stockTabs:name']")).sendKeys(name);
        Thread.sleep(TestContext.wait);
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='stockTabs:title']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='stockTabs:title']")).sendKeys(title);
        Thread.sleep(TestContext.wait);
        // description is a textarea, so the function changes
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:mainInfoTab']/div/div/div[5]/div[2]/textarea")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:mainInfoTab']/div/div/div[5]/div[2]/textarea")).sendKeys(
                "It's the description of the test root stock.");
        // choose Organization from the list
        // click on the list
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:org_label']")).click();
        Thread.sleep(TestContext.wait);
        // search for the specified element
        int i = 1;
        while (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:org_panel']/div/ul/li[" + i + "]")).getText().contains(organization))
            i++;
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:org_panel']/div/ul/li[" + i + "]")).click();

        // Click Save
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.save"))).click();
        // wait a little
        Thread.sleep(3 * TestContext.wait);
        // check the title
        if (root) {
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_content']/h1")).getText().contains(
                    TestContext.lang.getProperty("admin.rootStock.edit")))
                org.testng.Assert.fail("Wrong title after adding a new root stock: "
                        + testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_content']/h1")).getText() + " Error message: "
                        + testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText());
        } else {
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_content']/h1")).getText().contains(
                    TestContext.lang.getProperty("admin.stock.edit")))
                org.testng.Assert.fail("Wrong title after adding a new root stock: "
                        + testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_content']/h1")).getText() + " Error message: "
                        + testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText());
        }
        log.trace("Stock " + name + " created succesfull");
    }

    /**
     * Assign users to a stock
     *
     * @param stock  Name of the stock
     * @param users  Name of users to add - array of strings
     * @param rights Rights for the users - array of integers. In the same order as the user + at the end rights for the
     *               anonymous.
     *               Rights are read as a binary number, for example: 416=0b110100000 => the user has the 1st, 2nd and 4th
     *               rights (Read, Write and Export)
     * @param add    boolean - add (true) or delete (false) users from the specified stock
     * @throws InterruptedException
     */
    public void addUserToStock(String stock, String[] users, Integer[] rights, boolean add) throws InterruptedException {
        log.trace("Assig users to the stock " + stock);

        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .linkText(TestContext.lang.getProperty("admin.globalConfig"))));

        // click the menu
        Actions action = new Actions(driver);
        // Mouse over the menu
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("common.stock")))).build()
                .perform();
        // Mouse over and click the submenu
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.stock.manageList"))))
                .click().build().perform();

        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .linkText(TestContext.lang.getProperty("admin.globalConfig"))));

        // click the stock
        testFunctions.findAndWaitOnElement(By.linkText(stock)).click();

        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .linkText(TestContext.lang.getProperty("public.user.logout"))));

        // click user acces rights
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.stock.accessRights.user"))).click();

        // click Assign...
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.assign") + " ')]")).click();

        // select users one by one
        int addOrDelButtonPosition = 3;
        String addOrDelListXPath = ".//*[@id='stockTabs:assignUserDialog']//*[preceding-sibling::div[text()='"
                + TestContext.lang.getProperty("admin.picklist.targetUsers") + "']]";
        if (add) {
            addOrDelButtonPosition = 1;
            addOrDelListXPath = ".//*[@id='stockTabs:assignUserDialog']//*[preceding-sibling::div[text()='"
                    + TestContext.lang.getProperty("admin.picklist.sourceUsers")
                    + "']]";
        }

        // wait for users to appear
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(addOrDelListXPath + "/li[1]")));
        // add users
        for (String user : users) {
            int i = 1;
            while (!testFunctions.findAndWaitOnElement(By.xpath(addOrDelListXPath + "/li[" + i + "]")).getText().equals(user))
                i++;
            testFunctions.findAndWaitOnElement(By.xpath(addOrDelListXPath + "/li[" + i + "]")).click();
            testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:assignUserDialog']//button[" + addOrDelButtonPosition + "]")).click();
            Thread.sleep(2 * TestContext.wait);
        }
        Thread.sleep(2 * TestContext.wait);

        // Click 'Assign users'
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.assignUsers") + "')]")).click();

        // wait for users to added
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(".//*[@id='stockTabs:accessRightsUserTable_data']/tr[" + users.length + "]/td[1]")));

        // add them rights
        if (add && rights.length - 1 == users.length) {
            int k = 0;
            for (String user : users) {
                int i = 1;
                // find the user
                while (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:accessRightsUserTable_data']/tr[" + i + "]/td[1]")).getText()
                        .equals(user))
                    i++;
                // click on the rights
                for (int j = 0; j < 9; j++) // right by right
                {
                    if (rights[k] / Math.pow(2, 8 - j) >= 1) {
                        testFunctions.findAndWaitOnElement(
                                By.xpath(".//*[@id='stockTabs:accessRightsUserTable_data']/tr[" + i + "]/td[" + (j + 2) + "]/div/div[2]/span")).click();
                        rights[k] = (int) (rights[k] - Math.pow(2, 8 - j));
                    }
                }

                Thread.sleep(3 * TestContext.wait);
                k++;
            }

            // add rights to Anonymous
            if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='stockTabs:accessRightsUserTable_data']/tr[1]/td[1]")).getText().equals(
                    "Anonymous"))
                org.testng.Assert.fail("Anonymous not found when assigning rights to a stock");
            for (int j = 0; j < 9; j++) // right by right
            {
                if (rights[rights.length - 1] / Math.pow(2, 8 - j) >= 1) {
                    testFunctions.findAndWaitOnElement(
                            By.xpath(".//*[@id='stockTabs:accessRightsUserTable_data']/tr[1]/td[" + (j + 2) + "]/div/div[1]/input")).click();
                    rights[rights.length - 1] = (int) (rights[k] - Math.pow(2, 8 - j));
                }
            }

        }
        // save it
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.save"))).click();

        // check the message
        if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                TestContext.lang.getProperty("facesMsg.stock.changeSuccess")))
            org.testng.Assert.fail("Wrong message: " + testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText());

        log.trace("Assig users to the stock " + stock + " finished succesfull");
    }
}
