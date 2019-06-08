package sk.rolandkortvely.cassovia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Server application for CRUD management of given resources
 */
@SpringBootApplication
@Configuration
public class CassoviaApplication {

    /**
     * Run Server
     * To fill the database with default data, go to /api/install
     * @param args app arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CassoviaApplication.class, args);
    }

    /**
     * SMTP username, pass and port... to send emails
     * @return new instance of JavaMailSender
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost("smtp.mailtrap.io");
        mailSender.setPort(587);
        mailSender.setUsername("fbe92898d8e539");
        mailSender.setPassword("e0ffcfe8a5df9c");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
