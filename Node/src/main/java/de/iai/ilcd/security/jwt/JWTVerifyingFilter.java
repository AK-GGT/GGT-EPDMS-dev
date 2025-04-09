package de.iai.ilcd.security.jwt;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.security.jwtold.BearerTokenOLD;
import de.iai.ilcd.security.sql.IlcdUsernamePasswordToken;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

/**
 * The filter for bearer token-based authentication
 *
 * @author sarai
 */
public class JWTVerifyingFilter extends AuthenticatingFilter {

    protected static final String AUTHORIZATION_HEADER = "Authorization";

    //    public static final String USER_ID = "userName"; // TODO: obsolete?
//    public static final String PASSWORD = "password";
    Logger logger = LogManager.getLogger(JWTVerifyingFilter.class);

//	/**
//	 * Initializing the Filter.
//	 */
//	public JWTVerifyingFilter() {
//		setLoginUrl(DEFAULT_LOGIN_URL); // TODO: wrong login url
//	}
//	
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void setLoginUrl(String loginUrl) { // TODO: obsolete?
//		String previous = getLoginUrl();
//		if (previous != null) {
//			this.appliedPaths.remove(previous);
//		}
//		super.setLoginUrl(loginUrl);
//		this.appliedPaths.put(getLoginUrl(),  null);
//	}

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated // TODO: i really REALLY want to rewrite this part
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        logger.debug("servlet request UIR: {}", ((HttpServletRequest) request).getRequestURI());
        if (logger.isTraceEnabled()) {
            String authzHeader = getHeader(request, AUTHORIZATION_HEADER);
            logger.trace("token: {}", authzHeader);
        }

        boolean loggedIn = false;
        if (isLoginRequest(request, response) || isLoggedAttempt(request, response)) { // TODO: it's just loggedattempt
            loggedIn = executeLogin(request, response);
            if (!loggedIn) {
                accessDenied(response);
            }
            logger.debug("user is finally logged in: {}", loggedIn);

            return loggedIn;
        }

        if (!loggedIn) {
            logger.debug("user not authorized according to wrong bearer token.");
        }
        if (ConfigurationService.INSTANCE.isTokenOnly()) {
            if (!loggedIn) {
                accessDenied(response);
            }
            return loggedIn;
        }
        return true;
    }

    private void accessDenied(ServletResponse response) throws IOException {
        response.resetBuffer();
        response.getOutputStream().write("Permission denied.".getBytes());
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        logger.debug("Trying to create token.");

//		if (isLoginRequest(request, response)) { // TODO: obsolete?
//            String json = IOUtils.toString(request.getInputStream());
//
//            if (json != null && !json.isEmpty()) {
//
//                try (JsonReader jr = Json.createReader(new StringReader(json))) {
//                    JsonObject object = jr.readObject();
//                    String username = object.getString(USER_ID);
//                    String password = object.getString(PASSWORD);
//                    return new IlcdUsernamePasswordToken(username, password);
//                }
//            }
//        }

        if (isLoggedAttempt(request, response)) {
            String token = getHeader(request, AUTHORIZATION_HEADER);
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(token.indexOf(" "));
                try {
//					return createToken(token);
                    return createToken_or_OLD(token.trim());
                } catch (AuthenticationException ae) {
                    //Act as unauthenticated user // TODO: are you sure about this?
                }
            }
        }
        return new IlcdUsernamePasswordToken();
    }


    /**
     * Support old API token with this header.
     * <p>
     * If this signing algorithm is HS256 and there is a "random" claim the
     * bearer token will be treated with the old token realm.
     * <p>
     * Added as backward compatible for already generated tokens.
     */
    private AuthenticationToken createToken_or_OLD(String token) {
        var t = token.split("\\.");
        try {
            var sig = new String(Base64.getDecoder().decode(t[0]));
            var body = new String(Base64.getDecoder().decode(t[1]));
            if (sig.contains("HS256") && body.contains("random")) // TODO: better check
                return createTokenOLD(token);
        } catch (Exception e) {
//			e.printStackTrace();
            logger.error("Malformed token");
//			throw e;
            throw new AuthenticationException("Malformed token");
        }
        return createToken(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        return false;
    }

    /**
     * Checks if request is a request for authorization.
     *
     * @param request  The request to check whether it is an authorization request
     * @param response
     * @return True if the request is a log attempt
     */
    protected boolean isLoggedAttempt(ServletRequest request, ServletResponse response) {
        String authzHeader = getHeader(request, AUTHORIZATION_HEADER);
        return authzHeader != null;
    }

    /**
     * Gets the authorization header from given request.
     *
     * @param request The request containing the authorization header
     * @return The authorization header of the given request
     */
    protected String getHeader(ServletRequest request, String header) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        return httpRequest.getHeader(header);
    }

    public BearerTokenOLD createTokenOLD(String tokenOLD) {
        try {
            JWTController jwt = ApplicationContextHolder.getApplicationContext().getBean(JWTController.class);
            var claims = jwt.parseClaimsJwsOLD(tokenOLD);

            String username = claims.getSubject();
            String randomness = claims.get("random", String.class);

            return new BearerTokenOLD(username, randomness, tokenOLD);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("following error occurred:" + e);
                e.printStackTrace();
            }
            throw new AuthenticationException();
        }
    }

    /**
     * Creates a bearer token.
     *
     * @param token The String containing information to create bearer token
     * @return A bearer token containing relevant information from given token String
     */
    //TODO: cachable?
    //TODO: don't validate token in filter. better do it during authentication
    public BearerToken createToken(String token) {
        try {

            JWTController jwt = ApplicationContextHolder.getApplicationContext().getBean(JWTController.class);

            var claims = jwt.parseClaimsJws(token);
            var username = claims.getSubject();

            return new BearerToken(username, token);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("following error occurred:" + e);
                e.printStackTrace();
            }
            throw new AuthenticationException();
        }
    }

}
