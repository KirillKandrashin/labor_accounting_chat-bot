package kirill.kandrashin.tgbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
@SpringBootConfiguration
class BotApplicationTests {

    @Test
    void contextLoads() {
    }

}
