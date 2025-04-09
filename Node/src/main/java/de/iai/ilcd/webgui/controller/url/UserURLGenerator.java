package de.iai.ilcd.webgui.controller.url;

import org.apache.commons.lang.StringUtils;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

public class UserURLGenerator extends AbstractURLGenerator {

    public UserURLGenerator(URLGeneratorBean urlBean) {
        super(urlBean);
    }

    public String getProfile() {
        return this.getProfile(this.getCurrentStockName());
    }

    public String getProfile(String stockName) {
        return "/profile.xhtml" + super.getStockURLParam(stockName, true);
    }

    public String getPassword() {
        return this.getPassword(this.getCurrentStockName());
    }

    public String getPassword(String stockName) {
        return "/changePassword.xhtml" + super.getStockURLParam(stockName, true);
    }

    public String getLogout() {
        return this.getLogout(this.getCurrentStockName());
    }

    public String getLogin() {
        return this.getLogin(this.getCurrentStockName());
    }

    public String getLogin(String stockName) {
        final ExternalContext econtext = FacesContext.getCurrentInstance().getExternalContext();
        String queryString = ((HttpServletRequest) econtext.getRequest()).getQueryString();
        if (!StringUtils.isBlank(queryString)) {
            queryString = "?" + queryString;
        } else {
            queryString = "";
        }
        return buildPathWithParams("/login.xhtml", super.getStockURLParam(stockName, false), "src=" + econtext.getRequestServletPath() + queryString);
    }

    public String getLogout(String stockName) {
        return "/login.xhtml" + super.getStockURLParam(stockName, true);
    }

    public String getRegistration() {
        return this.getRegistration(this.getCurrentStockName());
    }

    public String getRegistration(String stockName) {
        return "/registration.xhtml" + this.getStockURLParam(stockName, true);
    }

    /**
     * Get the URL to create new user (on current stock name)
     *
     * @return generated URL
     */
    public String getNew() {
        return this.getNew(this.getCurrentStockName());

    }

    /**
     * Get the URL to create new user
     *
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getNew(String stockName) {
        return "/admin/newUser.xhtml" + super.getStockURLParam(stockName, true);
    }

    /**
     * Get the URL to edit user
     *
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getEdit(String stockName) {
        return this.getEdit(null, stockName);
    }

    /**
     * Get the URL to edit user (on current stock name)
     *
     * @return generated URL
     */
    public String getEdit() {
        return this.getEdit(null, this.getCurrentStockName());
    }

    /**
     * Get the URL to edit user
     *
     * @param uid       ID of the user to edit
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getEdit(Long uid, String stockName) {
        return buildPathWithParams("/admin/showUser.xhtml", (uid != null ? "userId=" + uid.toString() + "&" : ""), super.getStockURLParam(stockName));
    }

    /**
     * Get the URL to edit user (on current stock name)
     *
     * @param uid ID of the user to edit
     * @return generated URL
     */
    public String getEdit(Long uid) {
        return this.getEdit(uid, this.getCurrentStockName());
    }

    /**
     * Get the URL to show user list (on current stock name)
     *
     * @return generated URL
     */
    public String getShowList() {
        return this.getShowList(this.getCurrentStockName());

    }

    /**
     * Get the URL to show user list
     *
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getShowList(String stockName) {
        return "/admin/manageUserList.xhtml" + super.getStockURLParam(stockName, true);
    }

    /**
     * Get the URL to show recovery Access
     *
     * @return generated URL
     */
    public String getAccessRecovery() {
        return this.getAccessRecovery(this.getCurrentStockName());
    }

    /**
     * Get the URL to show recovery Access
     *
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getAccessRecovery(String stockName) {
        return "/accessRecovery.xhtml" + super.getStockURLParam(stockName, true);
    }

    /**
     * Get the URL to show recovery Login
     *
     * @return generated URL
     */
    public String getLoginRecovery() {
        return this.getLoginRecovery(this.getCurrentStockName());
    }

    /**
     * Get the URL to show recovery Login
     *
     * @param stockName name of the stock
     * @return generated URL
     */
    public String getLoginRecovery(String stockName) {
        return "/loginRecovery.xhtml" + super.getStockURLParam(stockName, true);
    }

}
