package de.iai.ilcd.service;

import de.iai.ilcd.model.common.exception.ExpiredPasswordNotChangedException;
import de.iai.ilcd.model.common.exception.PrivacyPolicyNotAcceptedException;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Service;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Service class for persisting/ merging user when privacy policy was accepted or an expired password was changed.
 *
 * @author sarai
 */
@Service("userService")
public class UserService {

    private static Logger logger = LogManager.getLogger(UserService.class);

    private UserDao userDao = new UserDao();

    /**
     * Persists user when user accepted privacy policy.
     *
     * @param user The user that should accept privacy policy
     * @throws PrivacyPolicyNotAcceptedException
     * @throws MergeException
     * @throws IOException
     */
    public void privacyPolicyAccepted(User user) throws PrivacyPolicyNotAcceptedException, MergeException {
        if (!user.isPrivacyPolicyAccepted()) {
            throw new PrivacyPolicyNotAcceptedException();
        }

        userDao.merge(user);
    }

    /**
     * Persists user when user changed expired password
     *
     * @param user The user with expired password
     * @throws ExpiredPasswordNotChangedException
     * @throws MergeException
     */
    public void expiredPasswordChanged(User user) throws ExpiredPasswordNotChangedException, MergeException {
        if (user.isPasswordExpired()) {
            throw new ExpiredPasswordNotChangedException();
        }

        userDao.merge(user);
    }

    /**
     * Redirects user to requested page after login
     *
     * @param conf           The configuration bean
     * @param source         the requested admin source
     * @param indexXhtmlPath The index html path to which user should be to redirected if they haave not requested any admin page
     * @throws IOException
     */
    public void gotToRequestedPage(ConfigurationBean conf, String source, String indexXhtmlPath) throws IOException {
        SavedRequest savedRequest = WebUtils.getSavedRequest((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());

        if (logger.isDebugEnabled()) {
            logger.debug("savedRequest != null: " + (savedRequest != null));
            if (savedRequest != null)
                logger.debug(savedRequest.getRequestUrl());
        }

        if (savedRequest != null && !savedRequest.getRequestUrl().contains("loginRecovery.xhtml")) {
            FacesContext.getCurrentInstance().getExternalContext().redirect(savedRequest.getRequestUrl());
        } else {
            String redirectPath = conf.getContextPath() + (!StringUtils.isBlank(source) && !source.equals("/login.xhtml") ? source : indexXhtmlPath);
            if (logger.isDebugEnabled()) {
                logger.debug("forwarding to " + redirectPath);
            }
            FacesContext.getCurrentInstance().getExternalContext().redirect(redirectPath);
        }
    }

}
