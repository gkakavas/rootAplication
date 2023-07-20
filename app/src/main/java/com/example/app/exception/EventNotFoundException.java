package com.example.app.exception;

public class EventNotFoundException extends Exception{
    public EventNotFoundException(){
        super("Not found event with this id");
    }

}