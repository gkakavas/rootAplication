package com.example.app.utils.deserializers;

import com.example.app.exception.InvalidUUIDFormatException;
import com.example.app.models.requests.UserIdsSet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;

public class UUIDSetDeserializer extends JsonDeserializer<UserIdsSet> {
    @Override
    public UserIdsSet deserialize(JsonParser parser, DeserializationContext context) throws IOException{
        String field = "userIds";
        JsonNode node = parser.getCodec().readTree(parser);
        JsonNode userIdsNode = node.path(field);
        Set<UUID> uuidSet = new HashSet<>();
        List<String> invalidValuesList = new ArrayList<>();
        if (userIdsNode.isArray()) {
            for (JsonNode jsonNode : userIdsNode) {
                String uuidString = jsonNode.asText();
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    uuidSet.add(uuid);
                } catch (IllegalArgumentException ignore) {
                    invalidValuesList.add(uuidString);
                }
            }
        }
        if(userIdsNode.isMissingNode()){
            for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
                String fieldName = it.next();
                invalidValuesList.add(fieldName);
            }
            String message = "Invalid json properties";
            throw new InvalidUUIDFormatException(message,field,invalidValuesList);
        }
        if(!invalidValuesList.isEmpty()){
            String message = "Invalid UUID values provided";
            throw new InvalidUUIDFormatException(message, field,invalidValuesList);
        }
        return UserIdsSet.builder()
                .userIds(uuidSet)
                .build();
    }
}
