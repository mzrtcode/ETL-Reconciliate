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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }


    @Async
    public void sendEmail(String from, String to, String subject, String content, byte[] attatchment, String fileName, String fileType) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // true = multipart para adjuntos
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false); // true = HTML

            if (attatchment != null && attatchment.length > 0) {
                DataSource dataSource = new ByteArrayDataSource(
                        attatchment,
                        fileType
                );
                helper.addAttachment(fileName, dataSource);
            }

            javaMailSender.send(mimeMessage);
            logger.info("Email sent to {}", to);

        } catch (MessagingException ex) {
            logger.error("Error sending email to {}", to, ex);
        } catch (Exception ex) {
            logger.error("Error sending email to {}", to, ex);
        }
    }



}
