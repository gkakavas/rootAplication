package com.example.app.exception;

public class IllegalTypeOfFileException extends Exception {
    public IllegalTypeOfFileException(){
        super("This type of file is not supported");
    }
}
