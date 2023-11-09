package com.example.app.exception;

import lombok.Getter;

@Getter
public class InvalidUUIDFormatException extends RuntimeException {
    private final String fieldName;
    private final Object invalidValue;

    public <T> InvalidUUIDFormatException(String message,String fieldName,T invalidValue){
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }
}
