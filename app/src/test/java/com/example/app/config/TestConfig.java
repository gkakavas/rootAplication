package com.example.app.config;

import com.example.app.utils.deserializers.UUIDSetDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

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
        simpleModule.addDeserializer(Set.class, new UUIDSetDeserializer());
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
    @Bean
    public MappingJackson2HttpMessageConverter testMappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(testObjectMapper());
        return converter;
    }
}
