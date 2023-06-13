package kirill.kandrashin.tgbot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-bot.properties")
@Getter
public class BotConfig {
    private @Value("${bot.name}") String botName;
    private @Value("${bot.token}") String botToken;

    private @Value("${boss.username}") String bossUsername;
}



