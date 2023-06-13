package kirill.kandrashin.tgbot.domain.bot.mail;

import kirill.kandrashin.tgbot.config.MailConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class MailSender {
    private final MailConfig mailConfig;

    @Autowired
    private JavaMailSender mailSender;

    public void send(String mailTo, String message){
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(mailConfig.getUsername());
        mailMessage.setTo(mailTo);
        mailMessage.setSubject("Confirmation");
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }
}




