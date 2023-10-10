package com.example.app.exception;

import org.springframework.http.converter.HttpMessageNotReadableException;

public class NullUuidException extends HttpMessageNotReadableException {
    public NullUuidException(){
        super("Not allowed null UUID");
    }
}
