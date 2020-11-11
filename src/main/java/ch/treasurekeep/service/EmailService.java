package ch.treasurekeep.service;

import ch.treasurekeep.config.EmailConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * This Service offers to send a email from a previous configurated mail-account to its slef
 * Only text-content is provided
 */
@Service
@Component
public class EmailService {

    private final EmailConfiguration configuration;
    public EmailService(EmailConfiguration configuration) {
        this.configuration = configuration;
    }

    public void sendEmail(String subject, String messageString) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", this.configuration.getHostname());
        prop.put("mail.smtp.port", this.configuration.getPort());
        prop.put("mail.smtp.auth", this.configuration.getAuthMethods());
        prop.put("mail.smtp.starttls.enable", this.configuration.getTlsEnabled()); //TLS

        Session session = Session.getInstance(
                prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                EmailService.this.configuration.getAddress(),
                                EmailService.this.configuration.getPassword()
                        );
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(this.configuration.getAddress()));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(this.configuration.getAddress())
        );
        message.setSubject(subject);
        message.setText(messageString);

        Transport.send(message);
    }
}
