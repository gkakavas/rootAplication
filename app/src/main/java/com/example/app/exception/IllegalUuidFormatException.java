package com.example.app.exception;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

public class IllegalUuidFormatException extends RuntimeException {
    public IllegalUuidFormatException(){
        super("This UUID is not valid");
    }
}
