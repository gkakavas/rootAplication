package com.example.app.exception;

public class NewPasswordConfirmationNewPasswordNotMatchException extends Exception{
   public NewPasswordConfirmationNewPasswordNotMatchException() {
       super("New password and confirmation new password not match");
   }
}
