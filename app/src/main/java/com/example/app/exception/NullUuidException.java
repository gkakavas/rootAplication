package com.example.app.exception;

public class NullUuidException extends RuntimeException {
    public NullUuidException(){
        super("Not allowed null UUID");
    }
}
