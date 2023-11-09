package com.example.app.utils.deserializers;

import com.example.app.exception.InvalidUUIDFormatException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.*;

@JsonComponent
public class SetUUIDDeserializer extends JsonDeserializer<Set<UUID>> {

    @Override
    public Set<UUID> deserialize(JsonParser parser, DeserializationContext context) throws IOException{
        ArrayNode arrayNode = parser.getCodec().readTree(parser);
        Set<UUID> uuidSet = new HashSet<>();
        List<String> invalidUUIDsList = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
            ObjectNode objectNode = nodeFactory.objectNode();
            objectNode.set("uuid", arrayNode.get(i));
            UUID uuid;
            try {
                uuid = UUID.fromString(objectNode.get("uuid").asText());
                uuidSet.add(uuid);
            }catch (IllegalArgumentException  ignore){
                invalidUUIDsList.add(objectNode.get("uuid").asText());
            }
        }
        if(!invalidUUIDsList.isEmpty()){
            String message = "Invalid UUID value provided";
            String invalidField = parser.getCurrentName();
            throw new InvalidUUIDFormatException(message,invalidField,invalidUUIDsList);
        }
        return uuidSet;
    }
}
