package com.example.app.exception;

public class FileNotFoundException extends Exception{
    public FileNotFoundException(){
        super("Not found file with this id");
    }

}
