package co.com.itau.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.activation.DataSource;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendEmail(String from, String to, String subject, byte[] attachmentBytes) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // true = multipart para adjuntos
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = "<h1>This is a test from Spring Boot</h1>"
                    + "<p>This is a test from Spring Boot</p>";
            helper.setText(htmlContent, true); // true = HTML

            if (attachmentBytes != null && attachmentBytes.length > 0) {
                DataSource dataSource = new ByteArrayDataSource(
                        attachmentBytes,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );
                helper.addAttachment("archivo.xlsx", dataSource);
            }

            javaMailSender.send(mimeMessage);
            logger.info("Email sent to {}", to);

        } catch (MessagingException ex) {
            logger.error("Error sending email to {}", to, ex);
            // aquí puedes lanzar RuntimeException si quieres propagar
        } catch (Exception ex) {
            logger.error("Unexpected error sending email to {}", to, ex);
        }
    }

}
