package edu.kit.soda4lca.test.ui.basic;

import edu.kit.soda4lca.test.ui.AbstractUITest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Try to log in with the users created in T012UsersTest
 *
 * @author mark.szabo
 */
public class T022Advanced_LoginTest extends AbstractUITest {

    // initializing the log
    protected final static Logger log = LogManager.getLogger(T022Advanced_LoginTest.class);

    @Override
    protected List<List<String>> getDBDataSetFileName() {
        return Arrays.asList(Arrays.asList("DB_pre_T022AdvancedLoginTest.xml"));
    }

    /**
     * Try to log in with the users created in T012UsersTest
     *
     * @throws Exception
     */
    @Test(priority = 221)
    public void Advanced_LoginTest() throws Exception {
        log.debug("Advanced Login test started - Try to log in with different credentials");
        testFunctions.login("admin", "default", true, false, 1);
        testFunctions.login("User1", "s3cr3t", true, false, 2);
        testFunctions.login("User2", "s3cr3t", true, false, 2);
        testFunctions.login("User3", "s3cr3t", true, false, 2);
        testFunctions.login("User4", "s3cr3t", true, false, 2);
        testFunctions.login("User5", "s3cr3t", true, false, 2);
        testFunctions.login("User6", "s3cr3t", true, false, 2);
        testFunctions.login("User7", "s3cr3t", true, false, 2);
        testFunctions.login("Admin1", "s3cr3t", true, false, 1);
        testFunctions.login("Admin2", "s3cr3t", true, false, 1);
        testFunctions.login("Admin3", "s3cr3t", true, false, 1);
        log.debug("Advanced Login test finished");
    }

}
