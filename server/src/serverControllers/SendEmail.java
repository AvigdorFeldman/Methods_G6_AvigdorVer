package serverControllers;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.*;

public class SendEmail {

    /**
     * @param to
     * @param title
     * @param msg
     * Sends Email message to to with subject title with the content msg
     */
    public static void sendMail(String to, String title, String msg) {
        // Gmail SMTP server
        String host = "smtp.gmail.com";
        final String user = "bparkserver@gmail.com";  // Server's Gmail address
        // password: BParkServer5555
        final String password = "zjkk algw croq poxf";  // Server's Gmail app password

        // Set up the properties for the Gmail SMTP server
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Get the session object with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set the From: field
            message.setFrom(new InternetAddress(user));

            // Set the To: field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set the subject of the email
            message.setSubject(title);

            // Set the body of the email
            message.setText(msg);

            // Send the email
            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
