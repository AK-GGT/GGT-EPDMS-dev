package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.service.client.impl.vo.AuthenticationInfo;
import de.fzk.iai.ilcd.service.client.impl.vo.DataStockVO;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.UserAccessBean;
import de.iai.ilcd.security.jwt.JWTController;
import de.iai.ilcd.security.sql.IlcdSecurityRealm;
import de.iai.ilcd.security.sql.IlcdUsernamePasswordToken;
import de.iai.ilcd.webgui.controller.admin.RandomPassword;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * REST Web Service
 *
 * @author clemens.duepmeier
 */

@Component
@Path("authenticate")
public class AuthenticateResource { // TODO: might need to go through the JWTVerifying filter (shiro.ini)

    private static final Logger logger = LogManager.getLogger(AuthenticateResource.class);

    @Context
    private UriInfo context;

    @Autowired
    private JWTController jwtController;

    /**
     * Creates a new instance of AuthenticateResource
     */
    public AuthenticateResource() {
    }

    @GET
    @Path("status")
    @Produces("application/xml")
    public AuthenticationInfo status() {
        logger.debug("authenticate/status");

        UserAccessBean user = new UserAccessBean();

        AuthenticationInfo authInfo = new AuthenticationInfo();

        boolean authenticated = user.isLoggedIn();

        authInfo.setAuthenticated(authenticated);
        logger.debug("user authenticated: " + authenticated);

        if (!authenticated) {
            return authInfo;
        }

        authInfo.setUserName(SecurityUtil.getPrincipalName());
        logger.debug("username: " + authInfo.getUserName());

        if (user.hasAdminAreaAccessRight())
            authInfo.getRoles().add("ADMIN");
        if (user.hasSuperAdminPermission())
            authInfo.getRoles().add("SUPER_ADMIN");


        List<AbstractDataStock> readableStocks = user.getUserObject().getReadableStocks();

        for (AbstractDataStock ads : readableStocks) {
            DataStockVO stockVO = new DataStockVO();
            stockVO.setUuid(ads.getUuidAsString());
            stockVO.setShortName(ads.getName());

            //FIXME: wildcard permissions support
            List<StockAccessRight> permissions = user.getUserObject().getStockAccessRights();

            for (StockAccessRight permission : permissions) {
                // -1 is the wildcard equivalent
                if (permission.getStockId() == ads.getId() || permission.getStockId() == -1) {
                    ProtectionType[] roles = ProtectionType.toTypes(permission.getValue());
                    for (ProtectionType protectionType : roles) {
                        if (!stockVO.getRoles().contains(protectionType.name()))
                            stockVO.getRoles().add(protectionType.name());
                    }
                }
            }
            authInfo.getDataStockRoles().add(stockVO);
        }

        return authInfo;

    }

    /**
     * Retrieves representation of an instance of de.iai.ilcd.service.AuthenticateResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Path("login")
    @Produces("text/plain")
    public Response login(@QueryParam("userName") String userName, @QueryParam("password") String password) {
        // TODO return proper representation object
        logger.info("authenticate/login");
        Subject currentUser = SecurityUtils.getSubject();
        UserDao dao = new UserDao();
        String salt = null;
        if (!currentUser.isAuthenticated()) {
            Response pass = checkCredentials(userName, password, salt, dao);
            // no user currently logged in
            if (pass != null) {
                return pass;
            }
            salt = dao.getSalt(userName);
            IlcdUsernamePasswordToken token = new IlcdUsernamePasswordToken(userName, IlcdSecurityRealm.getEncryptedPassword(password, salt));
            // token.setRememberMe(true);
            try {
                currentUser.login(token);

                return Response.ok("Login successful").build();
                // message.setSummary("Your login was successful");
            } catch (UnknownAccountException uae) {
                return Response.status(Status.UNAUTHORIZED).entity("unknown error while verifying credentials").build();
            } catch (IncorrectCredentialsException ice) {
                return Response.status(Status.UNAUTHORIZED).entity("incorrect password or user name").build();
            } catch (LockedAccountException lae) {
                return Response.status(Status.UNAUTHORIZED).entity("account is locked; contact administrator").build();
            } catch (AuthenticationException au) {
                return Response.status(Status.UNAUTHORIZED).entity("incorrect password or user name").build();
            } catch (Exception e) {
                return Response.status(Status.UNAUTHORIZED).entity("unknown error while verifying credentials").build();
            }
        }
        return Response.ok("You are already logged in as a user").build();
    }

    /**
     * Gets an API token for token-based authentication (RSA-256)
     *
     * @param userName user name of user retrieving token
     * @param password password of user retrieving token
     * @return a String resembling the token if credentials matched principals
     */
    @GET
    @Path("getToken")
    @Produces({"application/xml", "text/xml"})
    public Response createToken(@QueryParam("userName") String userName, @QueryParam("password") String password, @QueryParam("issueTime") long issueTime) {

        // default value of issue time is now
        if (issueTime == 0) issueTime = System.currentTimeMillis();

        UserDao dao = new UserDao();
        String salt = null;
        logger.info("authenticate/createToken");

        Response pass = checkCredentials(userName, password, salt, dao);

        if (pass != null) {
            logger.debug("did not pass credentials.");
            return pass;

        }
        logger.debug("passed credentials.");
        try {
            User user = dao.getUser(userName);
            salt = dao.getSalt(userName);

            if (user != null && user.getPasswordHash().equals(IlcdSecurityRealm.getEncryptedPassword(password, salt)) && user.isApiKeyAllowed()) {
                return Response.ok(jwtController.generateToken(user, issueTime), "text/plain").build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("permission denied").type("text/plain").build();
            }
        } catch (UnknownAccountException uae) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("unknown error while verifying credentials").type("text/plain").build();
        } catch (IncorrectCredentialsException ice) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("incorrect password or user name").type("text/plain").build();
        } catch (LockedAccountException lae) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("account is locked; contact administrator").type("text/plain").build();
        } catch (AuthenticationException au) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("incorrect password or user name").type("text/plain").build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("unknown error while verifying credentials").type("text/plain").build();
        }
    }

    /**
     * Gets an API token for token-based authentication
     *
     * @param userName user name of user retrieving token
     * @param password password of user retrieving token
     * @return a String resembling the token if credentials matched principals
     */
    @GET
    @Path("getTokenOLD")
    @Produces({"application/xml", "text/xml"})
    @Deprecated
    public Response createTokenOLD(@QueryParam("userName") String userName, @QueryParam("password") String password) {
        UserDao dao = new UserDao();
        String salt = null;
        logger.info("authenticate/createToken");

        Response pass = checkCredentials(userName, password, salt, dao);

        if (pass != null) {
            logger.debug("did not pass credentials.");
            return pass;

        }
        logger.debug("passed credentials.");
        try {
            User user = dao.getUser(userName);
            salt = dao.getSalt(userName);

            if (user != null && user.getPasswordHash().equals(IlcdSecurityRealm.getEncryptedPassword(password, salt)) && user.isApiKeyAllowed()) {

                String random = RandomPassword.getPassword(1071);
                String jwt = jwtController.generateTokenOLD(userName, random); // TODO: for testing only

                user.setApiKey(random);
                try {
                    dao.merge(user);
                    return Response.ok(jwt, "text/plain").build();
                } catch (MergeException me) {
                    if (logger.isDebugEnabled()) {
                        me.printStackTrace();
                    }

                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("permission denied").type("text/plain").build();
            }
        } catch (UnknownAccountException uae) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("unknown error while verifying credentials").type("text/plain").build();
        } catch (IncorrectCredentialsException ice) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("incorrect password or user name").type("text/plain").build();
        } catch (LockedAccountException lae) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("account is locked; contact administrator").type("text/plain").build();
        } catch (AuthenticationException au) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("incorrect password or user name").type("text/plain").build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("unknown error while verifying credentials").type("text/plain").build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).entity("incorrect user name or password.").type("text/plain").build();
    }

    /**
     * Checks credentials upon given information.
     *
     * @param userName The name of user who wants to authenticate
     * @param password The password that is to be checked against given user name
     * @param salt     The salt that is to be checked in combination with other credential
     * @param dao      The dao for getting user object with given name
     * @return
     */
    private Response checkCredentials(String userName, String password, String salt, UserDao dao) {
        if (userName == null || password == null) {
            //Response.status( Status.UNAUTHORIZED );
            logger.debug("username or password is null.");
            return Response.status(Status.UNAUTHORIZED).entity("user name and password must have a value").type("text/plain").build();
        }
        salt = dao.getSalt(userName);
        if (salt == null) {
            //Response.status( Status.UNAUTHORIZED );
            logger.debug("salt is null.");
            return Response.status(Status.UNAUTHORIZED).entity("incorrect password or user name").type("text/plain").build();
        }
        return null;
    }

    @GET
    @Path("logout")
    @Produces("text/plain")
    public Response logout() {
        logger.info("authenticate/logout");
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            SecurityUtils.getSecurityManager().logout(currentUser);
            return Response.ok("successfully logged out").build();
        }
        // no user currently logged in
        return Response.ok("currently not authenticated").build();
    }

}
