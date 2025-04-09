package eu.europa.ec.jrc.lca.registry.test.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

public class T001Registry_LoginTest {

    protected final static Logger log = LogManager.getLogger(T001Registry_LoginTest.class);

    @Test(priority = 10)
    public static void login() throws InterruptedException {
        log.debug("Login to application Registry test started - Try to log in with different credentials");

        Main.getInstance().getDriver().manage().deleteAllCookies();

        if (!Main.fast) {
            TestFunctions.loginReg("", "", false, true, 0);
            TestFunctions.loginReg("admin", "", false, true, 0);
            TestFunctions.loginReg("", "test", false, true, 0);
            TestFunctions.loginReg("admin", "default", false, true, 0);
        }
        TestFunctions.loginReg("admin", "test", true, true, 0);
        log.info("Login to application Registry test finished");
    }

}