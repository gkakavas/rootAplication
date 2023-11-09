package com.example.app.utils.deserializers;

import com.example.app.exception.InvalidUUIDFormatException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.*;


public class UUIDSetDeserializer extends JsonDeserializer<Set<UUID>> {
    @Override
    public Set<UUID> deserialize(JsonParser parser, DeserializationContext context) throws IOException{
        ArrayNode arrayNode = parser.getCodec().readTree(parser);
        Set<UUID> uuidSet = new HashSet<>();
        List<String> invalidUUIDsList = new ArrayList<>();
        for (JsonNode jsonNode:arrayNode) {
            String uuidString = jsonNode.asText();
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
                uuidSet.add(uuid);
            }catch (IllegalArgumentException  ignore){
                invalidUUIDsList.add(uuidString);
            }
        }
        if(!invalidUUIDsList.isEmpty()){
            String message = "Invalid UUID values provided";
            throw new InvalidUUIDFormatException(message, "idsSet",invalidUUIDsList);
        }
        return uuidSet;
    }
}
