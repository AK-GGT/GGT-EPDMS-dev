package de.iai.ilcd.security.sql;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Shiro's default implementation for getPrinciple returns a string.
 * <p>
 * I think it should returns something that implements Principle instead.
 *
 * @author MK
 * @since soda4LCA 7.0.0
 */
public class IlcdUsernamePasswordToken extends UsernamePasswordToken {

    private static final long serialVersionUID = 8071643336569358487L;

    public IlcdUsernamePasswordToken() {
        super();
    }

    public IlcdUsernamePasswordToken(String userName, String password) {
        super(userName, password);
    }

    @Override
    public Object getPrincipal() {
        return new IlcdPrinciple(getUsername());
    }

}
