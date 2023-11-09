package com.example.app.config;

import com.example.app.utils.deserializers.UUIDDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;


@ActiveProfiles("unit")
@TestConfiguration
public class TestConfig {
    @Bean
    public Clock clock(){
        return Clock.fixed(Instant.now(), ZoneId.systemDefault());
    }

    @Bean
        public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule("UUIDModule"){{
             addDeserializer(UUID.class, new UUIDDeserializer());
        }});
        return objectMapper;
    }
}
