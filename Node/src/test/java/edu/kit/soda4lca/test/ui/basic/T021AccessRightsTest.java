package edu.kit.soda4lca.test.ui.basic;

import de.fzk.iai.ilcd.api.app.contact.ContactDataSet;
import de.fzk.iai.ilcd.api.app.flow.FlowDataSet;
import de.fzk.iai.ilcd.api.app.flowproperty.FlowPropertyDataSet;
import de.fzk.iai.ilcd.api.app.lciamethod.LCIAMethodDataSet;
import de.fzk.iai.ilcd.api.app.process.ProcessDataSet;
import de.fzk.iai.ilcd.api.app.source.SourceDataSet;
import de.fzk.iai.ilcd.api.app.unitgroup.UnitGroupDataSet;
import de.fzk.iai.ilcd.api.binding.helper.DatasetDAO;
import de.fzk.iai.ilcd.api.dataset.DataSet;
import de.fzk.iai.ilcd.service.client.FailedAuthenticationException;
import de.fzk.iai.ilcd.service.client.NotPermittedException;
import de.fzk.iai.ilcd.service.client.impl.ILCDClientResponse;
import de.fzk.iai.ilcd.service.client.impl.ILCDNetworkClient;
import edu.kit.soda4lca.test.ui.AbstractUITest;
import edu.kit.soda4lca.test.ui.main.TestContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class T021AccessRightsTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T021AccessRightsTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_post_T015ImportExportTest.xml"));
    }

    ;

    @Test(priority = 311)
    public void accessRights() throws Exception {
        // Copy from RootStock1 to RootStock2
        // incorrect user/passwd
        CopyBetweenDatastocks("User1", "s3cr3tt", ContactDataSet.class, "5bb337b0-9a1a-11da-a72b-0800200c9a70", "RootStock2", 1);
        // no export right on RS1
        CopyBetweenDatastocks("User1", "s3cr3t", ProcessDataSet.class, "0deb15f0-518a-11dd-ae16-0800200c9a66", "RootStock2", 2);
        // No read right on RS2
        CopyBetweenDatastocks("User2", "s3cr3t", LCIAMethodDataSet.class, "9e456c6b-2cb3-45cd-91ec-40681ab2b2e9", "RootStock2", 3);
        // No import or export right on RS2
        CopyBetweenDatastocks("User3", "s3cr3t", FlowDataSet.class, "0a0ba345-bba1-411f-84cc-e72b4b7d1027", "RootStock2", 4);
        // No export right on RS2
        CopyBetweenDatastocks("User4", "s3cr3t", FlowPropertyDataSet.class, "838aaa23-0117-11db-92e3-0800200c9a66", "RootStock2", 5);
        // CopyBetweenDatastocks( "User4", "s3cr3t", LCIAMethodDataSet.class, "9e456c6b-2cb3-45cd-91ec-40681ab2b2e9",
        // "RootStock2", 5 );
        // No import right on RS2
        CopyBetweenDatastocks("User5", "s3cr3t", UnitGroupDataSet.class, "93a60a57-a3c8-18da-a746-0800200c9a66", "RootStock2", 4);
        // every right presented
        CopyBetweenDatastocks("User6", "s3cr3t", SourceDataSet.class, "544205b8-bcc5-4850-a5b6-241434f23be6", "RootStock2", 0);
    }

    /**
     * @param user             Username
     * @param password         Password
     * @param dataset          dataset type class (e.g. ContactDataSet.class)
     * @param source           dataset UUID which will be copied
     * @param target           Name of the target Root Data Stock
     * @param expectedBehavior 0 -> correct behavior, user has all the rights
     *                         1 -> wrong password or user
     *                         2 -> no export right on RootStock1
     *                         3 -> no read right on RootStock2
     *                         4 -> no import right on RootStock2
     *                         5 -> no export right on RootStock2
     * @throws Exception
     */
    public <T extends DataSet> void CopyBetweenDatastocks(String user, String password, Class<T> dataset, String source, String target,
                                                          Integer expectedBehavior) throws Exception {

        // login
        ILCDNetworkClient ilcdnc = null;
        try {
            ilcdnc = new ILCDNetworkClient(TestContext.PRIMARY_SITE_URL + "resource/", user, password);
        } catch (FailedAuthenticationException fae) {
            //Check whether authentication should have failed
            if (expectedBehavior != 1) {
                org.testng.Assert.fail("The authentication data should be wrong, but the login still succeeds");
            }
            return;
        }

        // login test
        if (ilcdnc.getAuthenticationStatus().isAuthenticated() && expectedBehavior == 1)
            org.testng.Assert.fail("The authentication data should be wrong, but the login still succeeds");
        else if (!ilcdnc.getAuthenticationStatus().isAuthenticated() && !(expectedBehavior == 1))
            org.testng.Assert.fail("The authentication data should be correct, but it doesn't login");
        else if (expectedBehavior == 1) return;


        //export an existing dataset
        T ds = null;
        try {
            ds = ilcdnc.getDataSet(dataset, source);
        } catch (NotPermittedException e) {
            if (expectedBehavior == 4)
                return;
        } catch (Exception e) {
            if (expectedBehavior == 2 || expectedBehavior == 3)
                return;
            log.info("encountered exception " + e);
            throw new Exception(e);
        }
        if (expectedBehavior == 2)
            org.testng.Assert.fail("User should not have EXPORT right, but they seem to ");

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("ddHHmmss");

        String newUUID = "";
        String type = "";

        // for getting/setting the UUID every dataset-type needs different functions
        if (dataset.equals(ContactDataSet.class)) {
            type = "contacts";
            String origUUID = ((ContactDataSet) ds).getContactInformation().getDataSetInformation().getUUID();
            newUUID = sdf.format(cal.getTime()) + origUUID.substring(origUUID.indexOf("-"));
            ((ContactDataSet) ds).getContactInformation().getDataSetInformation().setUUID(newUUID);
        } else if (dataset.equals(SourceDataSet.class)) {
            type = "sources";
            String origUUID = ((SourceDataSet) ds).getSourceInformation().getDataSetInformation().getUUID();
            newUUID = sdf.format(cal.getTime()) + origUUID.substring(origUUID.indexOf("-"));
            ((SourceDataSet) ds).getSourceInformation().getDataSetInformation().setUUID(newUUID);
        } else if (dataset.equals(UnitGroupDataSet.class)) {
            type = "unitgroups";
            String origUUID = ((UnitGroupDataSet) ds).getUnitGroupInformation().getDataSetInformation().getUUID();
            newUUID = sdf.format(cal.getTime()) + origUUID.substring(origUUID.indexOf("-"));
            ((UnitGroupDataSet) ds).getUnitGroupInformation().getDataSetInformation().setUUID(newUUID);
        } else if (dataset.equals(FlowPropertyDataSet.class)) {
            type = "flowproperties";
            String origUUID = ((FlowPropertyDataSet) ds).getFlowPropertiesInformation().getDataSetInformation().getUUID();
            newUUID = sdf.format(cal.getTime()) + origUUID.substring(origUUID.indexOf("-"));
            ((FlowPropertyDataSet) ds).getFlowPropertiesInformation().getDataSetInformation().setUUID(newUUID);
        } else if (dataset.equals(FlowDataSet.class)) {
            type = "flows";
            String origUUID = ((FlowDataSet) ds).getFlowInformation().getDataSetInformation().getUUID();
            newUUID = sdf.format(cal.getTime()) + origUUID.substring(origUUID.indexOf("-"));
            ((FlowDataSet) ds).getFlowInformation().getDataSetInformation().setUUID(newUUID);
        } else if (dataset.equals(LCIAMethodDataSet.class)) {
            type = "lciamethods";
            String origUUID = ((LCIAMethodDataSet) ds).getLCIAMethodInformation().getDataSetInformation().getUUID();
            newUUID = sdf.format(cal.getTime()) + origUUID.substring(origUUID.indexOf("-"));
            ((LCIAMethodDataSet) ds).getLCIAMethodInformation().getDataSetInformation().setUUID(newUUID);
        } else if (dataset.equals(ProcessDataSet.class)) {
            type = "processes";
            String origUUID = ((ProcessDataSet) ds).getProcessInformation().getDataSetInformation().getUUID();
            newUUID = sdf.format(cal.getTime()) + origUUID.substring(origUUID.indexOf("-"));
            ((ProcessDataSet) ds).getProcessInformation().getDataSetInformation().setUUID(newUUID);
        }

        // save the ds to disk
        File tmpFile = File.createTempFile(newUUID, ".xml");
        tmpFile.deleteOnExit();
        String fileName = tmpFile.getAbsolutePath();

        DatasetDAO dao = new DatasetDAO();
        dao.saveDataset(ds, fileName);

        // set up an InputStream from that file
        InputStream fis = new FileInputStream(tmpFile);

        // getting the UUID of the target stock (needs read right)
        Pattern pattern = Pattern.compile("<sapi:uuid>([-a-zA-Z0-9]*?)</sapi:uuid><sapi:shortName>" + target + "</sapi:shortName>");
        testFunctions.hclient = new HttpClient();
        testFunctions.getUrl("authenticate/login?userName=" + user + "&password=" + password);
        Matcher matcher = pattern.matcher(testFunctions.getUrl("datastocks").replaceAll("[\\n\\t ]", ""));
        String targetuuid = null;
        if (matcher.find() && !(expectedBehavior == 3)) {
            targetuuid = matcher.group(1);
        } else if (!matcher.find() && (expectedBehavior == 3)) {
            fis.close();
            return;
        } else if (!matcher.find())
            org.testng.Assert.fail("The requested DataStock '" + target
                    + "' not found in Node/resource/datastocks. Maybe the user doesn't have READ rights on the stock");


        ILCDClientResponse response = ilcdnc.putDataSetAsStream(dataset, fis, targetuuid);

        int status = response.getStatus();
//		String message = response.getMessage();

        /*
         * try {
         * is = ilcdnc.getDataSetAsStream( dataset, newUUID );
         * assertTrue( is.available() != 0 );
         * }
         * catch ( DatasetNotFoundException e ) {
         * // No export right on RS2
         * if ( expectedBehavior == 4 )
         * return;
         * org.testng.Assert.fail( "User should have EXPORT right on RS2, but it doesn't " );
         * }
         * catch ( IOException e ) {
         * if ( expectedBehavior == 5 )
         * return;
         * org.testng.Assert.fail( "User should have IMPORT right on RS2, but it doesn't " );
         * }
         */

        String pageSource = testFunctions.getUrl(type + "/" + newUUID + "?format=xml&view=full");

        if (status == 403) // No import right on RS2
        {
            if (expectedBehavior == 4)
                return;
            org.testng.Assert.fail("User should have IMPORT right on RS2, but they don't ");
        }

        if (pageSource.contains("You are not permitted to export this data set"))// No export right on RS2
        {
            if (expectedBehavior == 5)
                return;
            org.testng.Assert.fail("User should have EXPORT right on RS2, but they don't ");
        }


        if (expectedBehavior == 5)
            org.testng.Assert.fail("User should not have EXPORT right on RS2, but they seem to ");
        if (expectedBehavior == 4)
            org.testng.Assert.fail("User should not have IMPORT right on RS2, but they seem to ");
        return;
    }
}
