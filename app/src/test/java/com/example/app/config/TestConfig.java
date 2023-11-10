package com.example.app.config;

import com.example.app.models.requests.UserIdsSet;
import com.example.app.utils.deserializers.UUIDDeserializer;
import com.example.app.utils.deserializers.UUIDSetDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Configuration
@Profile("unit")
@ComponentScan(basePackages = "com.example.app.utils.deserializers")
public class TestConfig {
    @Bean
    public Clock clock(){
        return Clock.fixed(Instant.now(), ZoneId.systemDefault());
    }

   @Bean
    public ObjectMapper testObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        JavaTimeModule timeModule = new JavaTimeModule();
        simpleModule.addDeserializer(UserIdsSet.class, new UUIDSetDeserializer());
        simpleModule.addDeserializer(UUID.class, new UUIDDeserializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.registerModule(timeModule);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
    @Bean
    public MappingJackson2HttpMessageConverter testMappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(testObjectMapper());
        return converter;
    }
}
