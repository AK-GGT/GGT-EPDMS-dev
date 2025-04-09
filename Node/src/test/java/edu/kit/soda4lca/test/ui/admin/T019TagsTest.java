package edu.kit.soda4lca.test.ui.admin;

import com.codeborne.selenide.testng.ScreenShooter;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

/**
 * Delete a dataset from every type and test if it gets really deleted
 */
@Listeners({ScreenShooter.class})
public class T019TagsTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T019TagsTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    @Test(priority = 191)
    public void manageTags() {
        log.info("'Manage tags' test started");

        // login
        testFunctions.login("admin", "default", true, true);

        // click on Admin area
        $(By.linkText(TestContext.lang.getProperty("admin.adminArea"))).click();

        $(By.linkText(TestContext.lang.getProperty("admin.globalConfig"))).hover();

        // Mouse over and click the submenu 'Tags'
        $(By.linkText(TestContext.lang.getProperty("admin.tags.edit"))).hover().click();

        sampleTags();
        cancelChanges();
        saveTags();
        deleteTags();
        saveEmptyRows();
        sampleTags();
        log.info("saving sample tags");
        $x(".//*[@id='tagsEditor:btnSave']").click();
        $x(".//*[@id='tagsEditor:btnSave']").shouldBe(disabled);
        cancelTagEditWithEscapeKey();
        cancelChangesWithExistingTags();
        editAndSaveTags();

        log.info("'Manage tags' test finished");
    }

    /**
     * Test display of tag after assigning it to a datastock
     */
    @Test(priority = 192, dependsOnMethods = {"manageTags"})
    public void assignTagsToDataStock() {
        log.info("'Assign tags to datastock' test started");

        // login
        testFunctions.login("admin", "default", true, true);

        // click on Admin area
        testFunctions.gotoAdminArea();

        log.trace("navigating to 'Manage Tags' view");
        $(By.linkText(TestContext.lang.getProperty("admin.globalConfig"))).hover();

        // Mouse over and click the submenu 'Tags'
        $(By.linkText(TestContext.lang.getProperty("admin.tags.edit"))).click();

        //gotoPageByAdminMenu
        // go to 'Manage Data Stocks' view
        log.trace("navigating to 'Manage Data Stocks' view");
        $(By.linkText(TestContext.lang.getProperty("common.stock"))).hover();
        testFunctions.waitUntilSiteLoaded();
        $(By.linkText(TestContext.lang.getProperty("admin.stock.manageList"))).click();
        testFunctions.waitUntilSiteLoaded();
        $(By.linkText("RootStock1")).click();

        $(By.linkText(TestContext.lang.getProperty("admin.stock.tags"))).should(exist);
        $(By.linkText(TestContext.lang.getProperty("admin.stock.tags"))).click();

        for (int i = 1; i >= 3; i++) {
            $x(".//*[@id='stockTabs:tagList']/div[1]/ul/li[" + i + "]").shouldHave(exactText("edited tag" + i));
        }

        log.debug("assigning tag 2 to RootStock1");
        $x(".//*[@id='stockTabs:tagList']/div[1]/ul/li[2]").click();
        $x("/html/body/div[2]/div/form/div[3]/div/div[5]/div/div/div/div[2]/div/div[2]/div/button[1]").click();

        $x(".//*[@id='stockTabs:tagList']/div[1]/ul/li[1]").shouldHave(exactText("edited tag1"));
        $x(".//*[@id='stockTabs:tagList']/div[1]/ul/li[2]").shouldHave(exactText("edited tag3"));
        $x(".//*[@id='stockTabs:tagList']/div[3]/ul/li").shouldHave(exactText("edited tag2"));

        log.trace("saving tag to data stock");
        testFunctions.waitUntilSiteLoaded();
        $x(".//*[@id='generalForm']/div[2]/a[1]").click();
        testFunctions.waitUntilSiteLoaded();

        $x(".//*[@id='stockTabs:tagList']/div[1]/ul/li[1]").shouldHave(exactText("edited tag1"));
        $x(".//*[@id='stockTabs:tagList']/div[1]/ul/li[2]").shouldHave(exactText("edited tag3"));
        $x(".//*[@id='stockTabs:tagList']/div[3]/ul/li").shouldHave(exactText("edited tag2"));

        $(By.linkText(TestContext.lang.getProperty("admin.dataset.manageList"))).hover();

        $(By.linkText(TestContext.lang.getProperty("admin.process.manageList"))).click();
        testFunctions.waitUntilSiteLoaded();

        // select RootStock1 as data stock
        $x(".//div[contains(@id, 'selectDataStock')]").click();
        $x(".//li[text()='RootStock1']").click();
        testFunctions.waitUntilSiteLoaded();

        // Check if tags are shown
        for (int i = 1; i <= 5; i++) {
            $x(".//*[@id='processTable:1:tagsColumnContentList:0:tag:name']").shouldHave(exactText("edited tag2"));
        }

        // Navigate to some dataset overview (first in list)
        for (int i = 0; i < TestContext.timeout && !$x(".//*[@id='datasetDetails:tagPanel_content']/span/span").exists(); i++) {
            if ($x(".//*[@id='processTable:0:nameColumnContentName']").exists())
                $x(".//*[@id='processTable:0:nameColumnContentName']").click();
        }

        $x(".//*[@id='datasetDetails:tagPanel_content']/span/span")
                .scrollTo().shouldHave(exactText("edited tag2")).hover();
        $x("/html/body/div[6]/div[1]").shouldBe(visible);

        $(By.linkText(TestContext.lang.getProperty("public.proc.viewDatasetDetail"))).scrollIntoView(true).click();

        switchTo().window(1);

        $x(".//*[@id='datasetdetailPanel_content']/table/tbody/tr/td[2]/span/span").shouldHave(exactText("edited tag2")).hover();

        $x("/html/body/div[3]/div[2]").shouldBe(visible);

        log.info("'Assign tags to datastock' test finished");
    }


    public void cancelChanges() {
        log.info("pressing the cancel button");
        $x(".//*[@id='tagsEditor:btnCancel']").click();
        if (!$x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr/td").text()
                .equals(TestContext.lang.getProperty("admin.tags.empty")))
            org.testng.Assert.fail("dataTable not empty after clicking Cancel");
    }

    public void saveEmptyRows() {
        log.trace("saving empty rows");
        newRowWithBtn();
        newRowWithContextMenu();
        $x(".//*[@id='tagsEditor:btnSave']").click();
        for (int i = 0; i > 2; i++) {
            if (!$x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[" + i + "]/td[1]/div/div[1]").text().equals(""))
                org.testng.Assert.fail("the tag name " + i + " should be empty after saving");
            if (!$x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[" + i + "]/td[2]/div/div[1]").text().equals(""))
                org.testng.Assert.fail("the tag description " + i + " should be empty after saving dataTable");
        }
        if (!$x(".//*[@id='messages']/div/ul/li/span").shouldBe(visible).text()
                .equals(TestContext.lang.getProperty("facesMsg.tags.saveFail.emptyRows")))
            org.testng.Assert.fail("faces message for saving failure with empty rows not displayed!");
        $x(".//*[@id='tagsEditor:btnCancel']").click();
    }
    
    /*public void saveEmptyRowsWithExistingTags() {
        sampleTags();
        $x(".//*[@id='tagsEditor:btnSave']").click();
        // check tag name and description for every row
        for (int i=1; i<=3; i++) {
            if ( !$x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[" + i + "]/td[1]/div/div[1]")
                    .text().equals("tag" + i))
                org.testng.Assert.fail( "the tag name " + i + " is not displayed after saving dataTable" );
            if ( !$x( ".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[" + i + "]/td[2]/div/div[1]")
                    .getText().equals("tag example description " + i))
                org.testng.Assert.fail( "the tag description " + i + " is not displayed after saving dataTable" );
        }
        if ($x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[4]/td[1]/div/div[1]").exists()) {
            org.testng.Assert.fail("number of rows after saving empty rows should be 3");
        }
    }*/

    public void saveTags() {
        sampleTags();
        $x(".//*[@id='tagsEditor:btnSave']").click();
        log.info("saved sample Tags");

        // check tag name and description for every row
        for (int i = 1; i <= 3; i++) {
            if (!$x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[" + i + "]/td[1]/div/div[1]")
                    .text().equals("tag" + i))
                org.testng.Assert.fail("the tag name " + i + " is not displayed after saving dataTable");
            if (!$x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[" + i + "]/td[2]/div/div[1]")
                    .getText().equals("tag example description " + i))
                org.testng.Assert.fail("the tag description " + i + " is not displayed after saving dataTable");
        }
    }

    public void deleteTags() {
        // delete a tag and save after every delete
        for (int i = 3; i > 0; i--) {
            deleteTag(i);
            $x(".//*[@id='tagsEditor:btnSave']").click();
            log.debug("deleted tag " + i);

            // check after every delete that the correct tag was deleted
            for (int j = 1; j < i; j++) {
                if ($x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[" + j + "]/td[1]/div/div[1]")
                        .text().equals("tag" + i))
                    org.testng.Assert.fail("the tag " + i + " was not deleted and still exists");
            }
        }
        if (!$x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr/td").text()
                .equals(TestContext.lang.getProperty("admin.tags.empty")))
            org.testng.Assert.fail("dataTable not empty after deleting rows and saving");
    }

    public void cancelTagEditWithEscapeKey() {
        log.info("editing tags and discard edits with escape key");
        String xPath = ".//*[@id='tagsEditor:tagTable_data']/tr[%d]/td[%d]";
        String xPathInput = xPath + "/div/div[2]/input";
        String xPathOutput = xPath + "/div/div[1]";
        for (int i = 3; i > 0; i--) {
            log.trace("editing tag name " + i + "and cancelling changes");
            $x(String.format(xPath, i, 1)).click();
            $x(String.format(xPathInput, i, 1)).val("edited tag" + i).pressEscape()
                    .shouldHave(value("tag" + i));
            $x(String.format(xPathOutput, i, 1)).shouldHave(exactText("tag" + i));

            log.trace("editing tag desc " + i + "and cancelling changes");
            $x(String.format(xPath, i, 2)).click();
            $x(String.format(xPathInput, i, 2)).val("this tag " + i + " is edited").pressEscape()
                    .shouldHave(value("tag example description " + i));
            $x(String.format(xPathOutput, i, 2)).shouldHave(exactText("tag example description " + i));
        }

    }

    public void cancelChangesWithExistingTags() {
        log.info("edit existing tags and cancelling changes");
        for (int i = 3; i > 0; i--) {
            editTag("edited tag" + i, "this tag" + i + " is edited", i);
            log.trace("edited tag " + i);
        }
        $x(".//*[@id='tagsEditor:btnCancel']").shouldBe(visible).click();
        log.trace("clicked cancel button");

        String xPath = ".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[%d]/td[%d]/div/div[1]";
        for (int i = 3; i > 0; i--) {
            if (!$x(String.format(xPath, i, 1)).text().equals("tag" + i))
                org.testng.Assert.fail("changes on tag name " + i + " were not discarded on cancel");
            if (!$x(String.format(xPath, i, 2)).text().equals("tag example description " + i))
                org.testng.Assert.fail("changes on tag description " + i + " were not discarded on cancel");
        }
    }

    public void editAndSaveTags() {
        log.info("edit existing tags and saving changes");
        for (int i = 3; i > 0; i--) {
            editTag("edited tag" + i, "this tag " + i + " is edited", i);
            log.trace("edited tag " + i);
        }
        $x(".//*[@id='tagsEditor:btnSave']").click();
        log.debug("saved edited tags");

        String xPath = ".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[%d]/td[%d]/div/div[1]";
        for (int i = 3; i > 0; i--) {
            if (!$x(String.format(xPath, i, 1)).text().equals("edited tag" + i))
                org.testng.Assert.fail("changes on tag name " + i + " were not saved");
            if (!$x(String.format(xPath, i, 2)).text().equals("this tag " + i + " is edited"))
                org.testng.Assert.fail("changes on tag description " + i + " were not saved");
        }
    }

    /**
     * fill dataTable with sample tags, twice with the add-button and once with the
     * context menu
     *
     * @throws InterruptedException
     */
    public void sampleTags() {
        newRowWithBtn();
        editTag("tag1", "tag example description 1", 1);
        newRowWithBtn();
        editTag("tag2", "tag example description 2", 2);
        newRowWithContextMenu();
        editTag("tag3", "tag example description 3", 3);
        log.debug("created sample tags");
    }

    public void newRowWithBtn() {
        $x(".//*[@id='tagsEditor:btnAdd']").click();
    }

    public void newRowWithContextMenu() {
        $x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[1]/td").contextClick();
        $x(".//*[@id='tagsEditor:addMenuBtn']").shouldBe(visible).click();
    }

    public void deleteTag(int index) {
        $x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[" + index + "]/td[1]/div/div[1]").contextClick();
        $x(".//*[@id='tagsEditor:tagTable']/div/table/tbody/tr[" + index + "]/td[1]/div/div[1]").contextClick();
        $x(".//*[@id='tagsEditor:delMenuBtn']").click();
    }

    public void editTag(String name, String description, Integer index) {
        String xPath = ".//*[@id='tagsEditor:tagTable_data']/tr[" + index + "]/td[%d]";
        String xPathCell = xPath + "/div/div[2]/input";

        testFunctions.findAndWaitOnElement(By.xpath(String.format(xPath, 1))).click();
        testFunctions.findAndWaitOnElement(By.xpath(String.format(xPathCell, 1))).sendKeys(name);
        $x(String.format(xPathCell, 1)).pressEnter();

        testFunctions.findAndWaitOnElement(By.xpath(String.format(xPath, 2))).click();
        testFunctions.findAndWaitOnElement(By.xpath(String.format(xPathCell, 2))).sendKeys(description);
        $x(String.format(xPathCell, 2)).pressEnter();

        testFunctions.findAndWaitOnElement(By.xpath(String.format(xPath, 3))).click();
        String xPathVisiblityCheckboxInputWrapper = "/html/body/div[2]/div/div[1]/div/form/div[2]/div/table/tbody/tr[" + index + "]/td[3]/div/div[2]/div";
        String xPathCheckboxClickable = xPathVisiblityCheckboxInputWrapper + "/div[2]/span";
        String xPathCheckboxHelper = xPathVisiblityCheckboxInputWrapper + "/div[1]/input";
        String isVisibleTagStringValue = testFunctions.findAndWaitOnElement(By.xpath(xPathCheckboxHelper)).getAttribute("aria-checked");
        Boolean isVisibleTag = StringUtils.isNotBlank(isVisibleTagStringValue) ? Boolean.valueOf(isVisibleTagStringValue) : Boolean.valueOf(false);
        if (!isVisibleTag)
            testFunctions.findAndWaitOnElement(By.xpath(xPathCheckboxClickable)).click();
        $x(".//*[@id='tagsEditor:tagTable']/div/table/thead/tr/th[3]").click();
    }

}
