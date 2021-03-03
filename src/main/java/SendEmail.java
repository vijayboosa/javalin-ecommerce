import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class SendEmail {
    public boolean Send(String message, String toEmail){
        try {
            Email email = EmailBuilder.startingBlank()
                    .to("res", toEmail)
                    .withSubject("hey")
                    .withHTMLText(message)
                    .withHeader("X-Priority", 5)
                    .from("researchvkj@gmail.com")
                    .buildEmail();

            Mailer mailer = MailerBuilder
                    .withSMTPServer("smtp.gmail.com", 587, "researchvkj@gmail.com", "Niltech@12345")
                    .withTransportStrategy(TransportStrategy.SMTP_TLS)
                    .withSessionTimeout(10 * 1000)
                    .buildMailer();

            mailer.sendMail(email);
            return true;
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }

    }
}
