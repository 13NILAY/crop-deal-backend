package com.cropdeal.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class ConfigServerApplicationTests {
    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
    }

    @Test
    void usesConfigServerApplicationName() {
        assertEquals("config-server", environment.getProperty("spring.application.name"));
    }

    @Test
    void listensOnConfiguredPort() {
        assertEquals("8888", environment.getProperty("server.port"));
    }

    @Test
    void usesNativeConfigurationProfile() {
        assertEquals("native", environment.getProperty("spring.profiles.active"));
    }

    @Test
    void searchesClasspathConfigDirectory() {
        assertEquals("classpath:/config", environment.getProperty("spring.cloud.config.server.native.search-locations"));
    }
}