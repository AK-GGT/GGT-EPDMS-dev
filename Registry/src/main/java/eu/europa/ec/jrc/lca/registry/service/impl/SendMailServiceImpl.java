package eu.europa.ec.jrc.lca.registry.service.impl;

import eu.europa.ec.jrc.lca.commons.mail.MailAuthenticator;
import eu.europa.ec.jrc.lca.registry.service.SendMailService;
import eu.europa.ec.jrc.lca.registry.util.ILCDMessageSource;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service("sendMailService")
public class SendMailServiceImpl implements SendMailService {

    private static final Logger LOGGER = LogManager
            .getLogger(SendMailServiceImpl.class);
    private final String mailHost;
    private final Integer mailPort;
    private final String mailFrom;
    private final Boolean mailAuth;
    private final String mailUser;
    private final String mailPassword;

    @Autowired
    private ILCDMessageSource messageSource;

    public SendMailServiceImpl() {
        Context ctx;
        try {
            ctx = new InitialContext();
            Context envCtx = (Context) ctx.lookup("java:comp/env");
            this.mailHost = (String) envCtx.lookup("registry.mail.smtp.host");
            this.mailPort = (Integer) envCtx.lookup("registry.mail.smtp.port");
            this.mailAuth = (Boolean) envCtx.lookup("registry.mail.smtp.auth");
            this.mailFrom = (String) envCtx.lookup("registry.mail.smtp.from");
            this.mailUser = (String) envCtx.lookup("registry.mail.smtp.user");
            this.mailPassword = (String) envCtx.lookup("registry.mail.smtp.password");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void sendMail(List<String> to, String subject, String messageText) {
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", this.mailHost);
        mailProps.put("mail.smtp.port", this.mailPort);
        mailProps.put("mail.smtp.auth", this.mailAuth);
        mailProps.put("mail.smtp.starttls.enable", true);

        mailProps.put("mail.debug", LOGGER.isDebugEnabled());
        mailProps.put("mail.debug.auth", LOGGER.isDebugEnabled());

        MailAuthenticator authenticator = new MailAuthenticator(this.mailUser, this.mailPassword);

        Session mailSession = Session.getDefaultInstance(mailProps, authenticator);

        mailSession.setDebugOut(System.out);
        mailSession.setDebug(LOGGER.isDebugEnabled());

        MimeMessage message = new MimeMessage(mailSession);
        try {
            message.addFrom(new Address[]{new InternetAddress(this.mailFrom)});
            LOGGER.debug("sending message from {}", this.mailFrom);
            for (String recipient : to) {
                LOGGER.debug("sending message to {}", recipient);
                message.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(recipient));
            }
            message.setSubject(subject);
            message.setContent(messageText, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (AddressException e) {
            LOGGER.error("[sendMail]", e);
            LOGGER.error( "address: {}", e.getRef());
            LOGGER.error( "position: {}", e.getPos());
        } catch (MessagingException e) {
            LOGGER.error("[sendMail]", e);
        }
    }

    @Async
    public void sendMail(String to, String subject, String messageText) {
        List<String> recipients = new ArrayList<>();
        recipients.add(to);
        sendMail(recipients, subject, messageText);
    }

    @Async
    public void sendMail(List<String> to, String subjectKey, String messageKey,
                         Object[] subjectParams, Object[] messageParams) {
        String subject = messageSource
                .getTranslation(subjectKey, subjectParams);
        String messageBody = messageSource.getTranslation(messageKey,
                messageParams);
        sendMail(to, subject, messageBody);
    }

    @Async
    public void sendMail(String to, String subjectKey, String messageKey,
                         Object[] subjectParams, Object[] messageParams) {
        List<String> recipients = new ArrayList<>();
        recipients.add(to);
        sendMail(recipients, subjectKey, messageKey, subjectParams,
                messageParams);
    }

}
