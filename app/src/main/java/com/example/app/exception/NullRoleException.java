package com.example.app.exception;

public class NullRoleException extends RuntimeException{
    public NullRoleException(){
        super("Role is required");
    }
}
