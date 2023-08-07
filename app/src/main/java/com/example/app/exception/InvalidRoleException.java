package com.example.app.exception;

public class InvalidRoleException extends RuntimeException {
   public InvalidRoleException (){
       super("the provided role is not in the correct form");
   }
}
