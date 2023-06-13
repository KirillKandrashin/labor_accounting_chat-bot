package kirill.kandrashin.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
@AutoConfigureTestDatabase
class MainApplicationTests {

    @Test
    void contextLoads() {
    }

}
