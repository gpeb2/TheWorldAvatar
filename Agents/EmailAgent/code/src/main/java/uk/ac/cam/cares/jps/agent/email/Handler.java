package uk.ac.cam.cares.jps.agent.email;

import static uk.ac.cam.cares.jps.agent.email.Config.KEY_FROM_ADDRESS;
import static uk.ac.cam.cares.jps.agent.email.Config.KEY_SMTP_AUTH;
import static uk.ac.cam.cares.jps.agent.email.Config.KEY_SMTP_HOST;
import static uk.ac.cam.cares.jps.agent.email.Config.KEY_SMTP_PASS;
import static uk.ac.cam.cares.jps.agent.email.Config.KEY_SMTP_PORT;
import static uk.ac.cam.cares.jps.agent.email.Config.KEY_SSL_ENABLE;
import static uk.ac.cam.cares.jps.agent.email.Config.KEY_STARTTLS_ENABLE;
import static uk.ac.cam.cares.jps.agent.email.Config.KEY_SUBJECT_PREFIX;
import static uk.ac.cam.cares.jps.agent.email.Config.KEY_TO_ADDRESS;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class handles the connection to a specified SMTP server to send the requested email.
 *
 * @author Michael Hillman
 */
public class Handler {

    /**
     * Logger for error output.
     */
    private static final Logger LOGGER = LogManager.getLogger(Handler.class);

    /**
     * Prefix for email body
     */
    private static final String BODY_PREFIX = "<html>"
            + "What follows is an automated email generated by the <a href=\"https://github.com/cambridge-cares/TheWorldAvatar/tree/develop/Agents/EmailAgent\">EmailAgent</a> "
            + "agent on behalf of another KG service.<br>Please do not reply.<br><br><hr><br>";

    /**
     * Suffix for email body.
     */
    private static final String BODY_SUFFIX = "<br><br><hr><br></html>";

    /**
     * Config object.
     */
    private Config config;

    /**
     * Constructor.
     */
    public Handler() {
        // Empty
    }

    /**
     * Sets the config object.
     * 
     * @param config config object.
     */
    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * Attempts to send an email to the SMTP server specified in the properties.
     *
     * @param subject email subject
     * @param body email body
     * @param response HTTP response
     * 
     * @throws IOException
     */
    public void submitEmail(String subject, String body, HttpServletResponse response) throws IOException {

        // Load SMTP properites
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", config.getProperty(KEY_SMTP_HOST));
        mailProps.put("mail.smtp.port", config.getProperty(KEY_SMTP_PORT));
        mailProps.put("mail.smtp.starttls.enable", config.getProperty(KEY_STARTTLS_ENABLE));
        mailProps.put("mail.smtp.ssl.enable", config.getProperty(KEY_SSL_ENABLE));
        mailProps.put("mail.smtp.auth", config.getProperty(KEY_SMTP_AUTH));

        // Create authenticated session
        Session mailSession = Session.getDefaultInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        config.getProperty(KEY_FROM_ADDRESS),
                        config.getProperty(KEY_SMTP_PASS)
                );
            }
        });
        mailSession.setDebug(true);

        // Configure email
        Message email = new MimeMessage(mailSession);

        try {
            // To address 
            String toAddress = config.getProperty(KEY_TO_ADDRESS);
            String[] toAddresses = toAddress.split(",");
            for (String address : toAddresses) {
                email.addRecipient(RecipientType.TO, new InternetAddress(address));
            }

            // From address
            String fromAddress = config.getProperty(KEY_FROM_ADDRESS);
            email.setFrom(new InternetAddress(fromAddress));

            // Subject
            String fullSubject = config.getProperty(KEY_SUBJECT_PREFIX) + " - " + subject;
            email.setSubject(fullSubject);

            // Body
            String fullBody = BODY_PREFIX + body + BODY_SUFFIX;
            email.setContent(fullBody, "text/html");

        } catch (MessagingException | IllegalStateException exception) {
            LOGGER.error("Could not create email object.", exception);

            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.getWriter().write("{\"description\":\"Could not construct email object, invalid properties?\"}");
            return;
        }

        // Send the email
        sendEmail(email, response);
    }

    /**
     * Attempts to send the input email.
     *
     * @param email email to send.
     * @param response HTTP response
     * 
     * @throws IOException
     */
    public void sendEmail(Message email, HttpServletResponse response) throws IOException {
        try {
            LOGGER.info("Submitting email to remote SMTP server.");
            Transport.send(email);
            LOGGER.info("Submission sent.");

        } catch (MessagingException exception) {
            LOGGER.error("Could not send email to remote SMTP server.", exception);

            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.getWriter().write("{\"description\":\"Could not send email to remote SMTP server.\"}");
        }
    }
}
// End of class.
