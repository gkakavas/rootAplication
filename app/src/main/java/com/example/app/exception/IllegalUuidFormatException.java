package com.example.app.exception;

public class IllegalUuidFormatException extends RuntimeException {
    public IllegalUuidFormatException(){
        super("This UUID is not in the correct format");
    }
}
