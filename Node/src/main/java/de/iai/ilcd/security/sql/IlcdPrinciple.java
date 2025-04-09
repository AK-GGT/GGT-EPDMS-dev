package de.iai.ilcd.security.sql;

import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.IUser;
import de.iai.ilcd.security.FatPrincipal;

public class IlcdPrinciple implements FatPrincipal {

    private String username;
    private IUser user;

    public IlcdPrinciple(IUser user) { // use this constructor whenever possible
        if (user != null) {
            this.username = user.getUserName();
            this.user = user;
        }
    }

    public IlcdPrinciple(String username) {
        this.username = username;
        var dao = new UserDao(); // TODO: atleast this should be managed by spring
        this.user = dao.getUser(username);
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public IUser getSodaUser() {
        return user;
    }

}
