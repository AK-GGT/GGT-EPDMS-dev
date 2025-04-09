package de.iai.ilcd.security;

import de.iai.ilcd.model.security.UserOIDC;
import de.iai.ilcd.security.sql.IlcdUsernamePasswordToken;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.faces.context.FacesContext;
import java.io.IOException;

/**
 * @author clemens.duepmeier
 */
@Deprecated //TODO: remove this and do necessary changes
public class UserLoginActions {

    public void login(String userName, String password) {
        login(userName, password, false);
    }

    public void login(String userName, String password, boolean remembered) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated())
            return;
        IlcdUsernamePasswordToken token = new IlcdUsernamePasswordToken(userName, password);
        if (remembered) {
            token.setRememberMe(true);
        }
        currentUser.login(token);
        token.clear();
    }

    public void logout() {
        Subject currentUser = SecurityUtils.getSubject();
        // IlcdSecurityRealm realm= new IlcdSecurityRealm();
        // realm.clearAuthorizationInfo(currentUser.getPrincipals());

        if (SecurityUtil.getCurrentUser() instanceof UserOIDC) {
            FacesContext context = FacesContext.getCurrentInstance();
            try {
                context.getExternalContext().redirect("/logout"); // trigger logout filter
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (currentUser.isAuthenticated() || currentUser.isRemembered()) {
            currentUser.logout();
        }
    }

    public boolean isLoggedIn() {
        Subject currentUser = SecurityUtils.getSubject();

        if (currentUser.isAuthenticated() || currentUser.isRemembered()) {
            return true;
        } else {
            return false;
        }
    }
}
