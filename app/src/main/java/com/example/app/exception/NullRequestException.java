package com.example.app.exception;

public class NullRequestException extends RuntimeException {
    public NullRequestException(){
        super("Not allowed null request body");
    }
}
