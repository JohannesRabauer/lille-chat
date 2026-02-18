package dev.rabauer.lille.chat.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@SpringBootTest
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
