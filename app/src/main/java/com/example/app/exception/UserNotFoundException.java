package com.example.app.exception;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(){
        super("Not found user with this id");
    }

}
