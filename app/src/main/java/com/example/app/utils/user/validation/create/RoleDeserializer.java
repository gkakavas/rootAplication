package com.example.app.utils.user.validation.create;

import com.example.app.entities.Role;
import com.example.app.exception.InvalidRoleException;
import com.example.app.exception.NullRoleException;
import com.example.app.utils.user.validation.patch.AllowUserFields;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.EnumUtils;

import java.io.IOException;

public class RoleDeserializer extends StdDeserializer<Role> {
    public RoleDeserializer() {
        super(com.example.app.entities.Role.class);
    }

    @Override
    public Role deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        try {
            String roleString = jsonParser.getValueAsString();
            if(!EnumUtils.isValidEnum(Role.class, roleString)){
                throw new InvalidRoleException();
            }
            else if(roleString==null){
                throw new NullRoleException();
            }
            else return Role.valueOf(roleString);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException();
        }
    }
}
