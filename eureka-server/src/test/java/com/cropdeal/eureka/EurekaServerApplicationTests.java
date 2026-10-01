package com.cropdeal.eureka;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class EurekaServerApplicationTests {
	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
	}

	@Test
	void usesEurekaApplicationName() {
		assertEquals("eureka-server", environment.getProperty("spring.application.name"));
	}

	@Test
	void listensOnRegistryPort() {
		assertEquals("8761", environment.getProperty("server.port"));
	}

	@Test
	void serverDoesNotRegisterAsClient() {
		assertFalse(environment.getProperty("eureka.client.register-with-eureka", Boolean.class, true));
	}

	@Test
	void serverDoesNotFetchClientRegistry() {
		assertFalse(environment.getProperty("eureka.client.fetch-registry", Boolean.class, true));
	}

}
