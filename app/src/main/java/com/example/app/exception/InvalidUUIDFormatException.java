package com.example.app.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
public class InvalidUUIDFormatException extends RuntimeException {
    private final String fieldName;
    private final String invalidValue;

    public InvalidUUIDFormatException(String message,String fieldName,String invalidValue){
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }
}
