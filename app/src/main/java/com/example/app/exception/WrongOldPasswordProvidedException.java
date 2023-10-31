package com.example.app.exception;

public class WrongOldPasswordProvidedException extends Exception {
    public WrongOldPasswordProvidedException(){
        super("The old password you have provided is invalid");
    }
}
