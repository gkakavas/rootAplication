package com.example.app.exception;

public class LeaveNotFoundException extends Exception{
    public LeaveNotFoundException(){
        super("Not found leave with this id");
    }
}
