package edu.kit.soda4lca.test.ui.admin;


import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.FileTestFunctions;
import edu.kit.soda4lca.test.ui.main.TestContext;
import edu.kit.soda4lca.test.ui.main.WorkbookTestFunctions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;


/**
 * Checks users export by counting the number of entries in download excel file and
 * comparing it with number of entries in user table.
 *
 * @author sarai
 */
@Listeners({ScreenShooter.class})
public class T050Export_UsersTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T050Export_UsersTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T013GroupsTest.xml"));
    }

    /**
     * Export users als xls file, then count number of entries and compare with number of shown entries on Manage Users table.
     *
     * @throws MalformedURLException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void exportUsers() throws MalformedURLException, URISyntaxException, IOException, InterruptedException {
        // export Database
        log.info("Users export test started");

        // first delete the donwloaded file, in case of previous test failed and the file is still there
        String userString = TestContext.lang.getProperty("admin.users");
        String fileName = userString + ".xls";
        FileTestFunctions.tryDeleteFile(fileName, this.getClass());

        driver.manage().deleteAllCookies();
        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        // wait for the site to load
        testFunctions.waitUntilSiteLoaded();

        testFunctions.selectMenuItem("admin.user", "admin.user.manageList");

        // Click on 'Export list as XLS file' button
        testFunctions.findAndWaitOnElement(By.id("exportUsers")).click();
        // the download will begin and the file is going to get saved in project folder /tmp
        log.debug("Download started");

        //detect number of users on current page
        int usersEntries = testFunctions.countTotalUsersEntries();
//		int usersEntries = 1;
        Thread.sleep(200);

        //Count the number of excel sheet entries and compare result with number of entries inn user table
        WorkbookTestFunctions.countAndCompareEntries(fileName, this.getClass(), usersEntries);
        log.info("Users export test finished");
    }
}
