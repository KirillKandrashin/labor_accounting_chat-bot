package kirill.kandrashin.tgbot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

@Configuration
@Getter
public class MailConfig {
    private @Value("${mail.host}") String host;
    private @Value("${mail.username}") String username;
    private @Value("${mail.password}") String password;
    private @Value("${mail.port}") Integer port;


    @Bean
    public JavaMailSender JavaMailSender(){

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtps");
        //props.put("mail.debug", "true");
        return mailSender;
    }
}



