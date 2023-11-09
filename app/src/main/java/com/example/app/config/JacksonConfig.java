package com.example.app.config;

import com.example.app.utils.deserializers.UUIDDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.UUID;

@Configuration
@Slf4j
@Profile({"dev","unit","integration"})
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule("UUIDModule"){{
            addDeserializer(UUID.class, new UUIDDeserializer());
        }});
        return objectMapper;
    }
}
