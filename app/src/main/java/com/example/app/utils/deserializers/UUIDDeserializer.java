package com.example.app.utils.deserializers;

import com.example.app.exception.InvalidUUIDFormatException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.UUID;


@JsonComponent
public class UUIDDeserializer extends JsonDeserializer<UUID> {
    @Override
    public UUID deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String uuidString = parser.getValueAsString();
        if(!isValidUUID(uuidString)){
            String fieldName = parser.getCurrentName();
            String message = "Invalid UUID value provided";
            throw new InvalidUUIDFormatException(message,fieldName,uuidString);
        }
        return UUID.fromString(uuidString);
    }

    private boolean isValidUUID(String uuidString){
        try{
            UUID.fromString(uuidString);
            return true;
        }catch (Exception ignore){
            return false;
        }
    }
}
