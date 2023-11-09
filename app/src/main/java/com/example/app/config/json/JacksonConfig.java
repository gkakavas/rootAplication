package com.example.app.config;

import com.example.app.utils.deserializers.UUIDDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

@Configuration
@Slf4j
@Profile({"dev","integration"})
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(uuidModule());
        return objectMapper;
    }

    @Bean
    public SimpleModule uuidModule() {
        SimpleModule uuidModule = new SimpleModule("UUIDModule");
        uuidModule.addDeserializer(UUID.class, new UUIDDeserializer());
        return uuidModule;
    }
}

