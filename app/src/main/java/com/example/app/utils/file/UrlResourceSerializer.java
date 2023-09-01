package com.example.app.utils.file;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.core.io.UrlResource;

import java.io.IOException;

public class UrlResourceSerializer extends JsonSerializer<UrlResource> {
    @Override
    public void serialize(UrlResource value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getURL().toString());
    }
}
