package com.example.app.exception;

public class GroupNotFoundException extends Exception{
    public GroupNotFoundException(){
        super("Not found group with this id");
    }
}
