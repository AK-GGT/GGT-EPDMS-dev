package eu.europa.ec.jrc.lca.commons.mail;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class MailAuthenticator extends Authenticator {

    private PasswordAuthentication authentication;

    public MailAuthenticator(String user, String pwd) {
        authentication = new PasswordAuthentication(user, pwd);
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return authentication;
    }

}
