package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Just looking around on the admin site, if every button is available
 *
 * @author mark.szabo
 */
@Listeners({ScreenShooter.class})
public class T010Basic_AdminTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T010Basic_AdminTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T011OrganisationsTest.xml"));
    }

    ;

    /**
     * Just looking around on the admin site, if every button is available
     *
     * @throws InterruptedException
     */
    @Test(priority = 101)
    public void adminBasic() throws InterruptedException {
        log.info("admin basic test started");
        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.adminArea"))).click();
        // check the admin site
        log.debug("looking for the menu and menu items");
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='adminMenuForm:adminMenu']"));
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_content']"));
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.dataImport")));
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("common.stock")));
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.dataset.manageList")));
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.user")));
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.network")));
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.globalConfig")));
        log.info("admin basic test finished");
    }

}
