package com.example.app.utils.requestid;

import com.example.app.exception.IllegalUuidFormatException;
import com.example.app.exception.NullUuidException;
import com.example.app.models.requests.RequestId;
import com.example.app.utils.validator.uuid.UUIDValidator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StringToRequestIdConverter implements Converter<String, RequestId> {
    @Override
    public RequestId convert(@Nullable String uuid) {
        if(uuid!=null && UUIDValidator.isValid(uuid)){
            return RequestId.builder()
                    .uuid(UUID.fromString(uuid))
                    .build();
        }
        else if(!UUIDValidator.isValid(uuid)){
            throw new IllegalUuidFormatException();
        }
        else throw new NullUuidException();
    }
}
