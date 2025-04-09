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
 * Test the Organization menu. Add some Organization, then delete one, and change an other.
 *
 * @author mark.szabo
 */
@Listeners({ScreenShooter.class})
public class T011OrganisationsTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T011OrganisationsTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T011OrganisationsTest.xml"));
    }

    /**
     * Add new organizations
     *
     * @throws InterruptedException
     */
    @Test(priority = 111)
    public void newOrganisations() throws InterruptedException {
        // create a new organization
        log.info("'Creating new Organisations' test started");

        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        newOrganization("Organization1", "test", 3);
        newOrganization("Organization2", "test", 4);
        newOrganization("Organization3", "test", 2);
        newOrganization("Test Organization to delete", "delete", 3);

        // Click Save and Close
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.saveClose"))).click();

        Thread.sleep(TestContext.wait);

        // check if any warning message shows up
        testFunctions.assertNoMessageOtherThan("facesMsg.org.changeSuccess");

//		if ( !testFunctions.findAndWaitOnElement( By.xpath( ".//*[@id='messages']" ) ).getText().contains(
//				TestContext.lang.getProperty( "facesMsg.org.changeSuccess" ).substring( 0, 10 ) ) )
//			org.testng.Assert.fail( "Wrong message: " + testFunctions.findAndWaitOnElement( By.xpath( ".//*[@id='messages']" ) ).getText() );

        // check if the site is Manage Organizations now
        if (!driver.findElement(By.xpath((".//div[@id='admin_content']/h1"))).getText().equals(TestContext.lang.getProperty("admin.org.manageList")))
            org.testng.Assert.fail("After click on 'Save & Close' doesn't go to manageOrganizationList.xhtml instead goes to "
                    + driver.getCurrentUrl());

        log.info("'Creating new Organisations' test finished");
    }

    /**
     * Delete an organization, then change the sector of an other one.
     *
     * @throws InterruptedException
     */
    @Test(priority = 112, dependsOnMethods = {"newOrganisations"})
    public void manageOrganisations() throws InterruptedException {
        log.info("'Manage and delete Organisations' test started");

        driver.manage().deleteAllCookies();

        // login as admin
        testFunctions.login("admin", "default", true, true);
        // click on Admin area
        testFunctions.gotoAdminArea();

        log.trace("Click the menu");
        /*
         * TODO
         * Sometimes (maybe it depend on the design or some sort of weird race-condition) the test fails to find the
         * second submenu
         * In this case try to rerun the test, or restart the server with different design and rerun the test.
         * (Somehow it seems to me as it would fails if too many other programs running on the machine, which is kind a
         * strange)
         */
        // Create an action for mousemoves.Because more submenus, it needs some other methods
        Actions action = new Actions(driver);
        // Mouse over the menu 'Global configuration'
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.globalConfig")))).build().perform();
        // Mouse over the submenu 'Organizations'
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.orgs")))).build().perform();
        // Mouse over and click the sub-submenu 'Manage Organizations'
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.org.manageList")))).click().build()
                .perform();

        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath(".//*[@id='orgTable_paginator_bottom']")));

        // DELETE
        log.debug("Delete organisation");
        // find 'Test Organization to delete'
        log.trace("Find organisation to delete");
        int i = 1;
        while (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='orgTable_data']/tr[" + Integer.toString(i) + "]/td[2]/a")).getText().contains(
                "Test Organization to delete"))
            i++;
        // select it
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='orgTable_data']/tr[" + Integer.toString(i) + "]/td[1]/div/div[2]")).click();
        Thread.sleep(TestContext.wait);
        // Click 'Delete selected entries'
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='deleteBtn']")).click();
        // Are you sure? - Click OK
        testFunctions.findAndWaitOnElement(By.xpath("//button[contains(.,'" + TestContext.lang.getProperty("admin.ok") + "')]")).click();
        log.trace("Check if deleted succesfully");
        // Check the message
        if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText().contains(
                TestContext.lang.getProperty("facesMsg.removeSuccess").substring(0, 10)))
            org.testng.Assert.fail("Wrong message: " + testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='messages']")).getText());

        // CHANGE
        log.debug("Change an organisation (change the industrial sector)");
        // click 'Test Organization'
        testFunctions.findAndWaitOnElement(By.linkText("Organization1")).click();
        // choose an other Industrial sector from the list
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='orgTabs:industrSector_label']")).click();
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='orgTabs:industrSector_panel']/div/ul/li[5]")).click();
        // Click Save and Close
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.saveClose"))).click();

        log.info("'Manage and delete Organisations' test finished");

    }

    /**
     * Adding a new organization
     *
     * @param name   Name of the organization
     * @param unit   Organization unit (can be empty)
     * @param sector Sector id (integer from 2 to 10)
     * @throws InterruptedException
     */
    public void newOrganization(String name, String unit, Integer sector) throws InterruptedException {

        // wait for the site to load
        testFunctions.waitOnAdminArea();

        Thread.sleep(TestContext.wait);

        log.trace("Click the menu");
        // Create an action for mousemoves.Because more submenus, it needs some other methods
        Actions action = new Actions(driver);
        // Mouse over the menu 'Global configuration'
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.globalConfig")))).build().perform();
        Thread.sleep(TestContext.wait);
        // Mouse over the submenu 'Organizations'
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.orgs")))).build().perform();
        Thread.sleep(TestContext.wait);
        // Mouse over and click the sub-submenu 'New Organization'
        // testFunctions.findandwaitanElement( By.linkText( Main.lang.getProperty( "admin.org.new" ) ) );
        action.moveToElement(driver.findElement(By.linkText(TestContext.lang.getProperty("admin.org.new")))).click().build().perform();

        Thread.sleep(TestContext.wait);

        // wait for the site to load
        (new WebDriverWait(driver, Duration.ofSeconds(TestContext.timeout))).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(TestContext.lang
                .getProperty("admin.saveNew"))));
        log.trace("Fill in the form");
        // Fill in the form
        // name
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='orgTabs:name']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='orgTabs:name']")).sendKeys(name);

        // Organization unit:
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='orgTabs:orgUnit']")).clear();
        Thread.sleep(500);
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='orgTabs:orgUnit']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='orgTabs:orgUnit']")).sendKeys(unit);
        // choose Industrial sector from the list
        Thread.sleep(500);
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='orgTabs:industrSector_label']")).click();
        Thread.sleep(500);
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='orgTabs:industrSector_panel']/div/ul/li[" + sector + "]")).click();
        Thread.sleep(500);
        // Choose a country from the list
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='orgTabs:country_label']")).click();
        Thread.sleep(2000);
        testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='orgTabs:country_panel']/div/ul/li[99]")).click();
        // Zip code
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='orgTabs:zipCode']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='orgTabs:zipCode']")).sendKeys("1111");
        // City
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='orgTabs:city']")).clear();
        testFunctions.findAndWaitOnElement(By.xpath(".//input[@id='orgTabs:city']")).sendKeys("Budapest");
        // street address - textarea
        testFunctions.findAndWaitOnElement(By.id("orgTabs:streetAddr")).clear();
        testFunctions.findAndWaitOnElement(By.id("orgTabs:streetAddr")).sendKeys("Müegyetem rkp. 9-11.");
        log.trace("Click Save");
        // Click Save
        testFunctions.findAndWaitOnElement(By.linkText(TestContext.lang.getProperty("admin.save"))).click();
        // check if any warning message shows up
        Thread.sleep(TestContext.wait);
        testFunctions.assertNoMessageOtherThan("facesMsg.org.saveSuccess");

        // check the header/title
        if (!testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_content']/h1")).getText().contains(TestContext.lang.getProperty("admin.org.edit")))
            org.testng.Assert.fail("Wrong title/header: " + testFunctions.findAndWaitOnElement(By.xpath(".//*[@id='admin_content']/h1")).getText());

    }

}
