package com.example.app.utils.validator.user;

import com.example.app.entities.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserPatchMapValueValidator implements ConstraintValidator<ValidUserPatchValue, Map<String,String>> {

    @Override
    public boolean isValid(Map<String, String> patchMap, ConstraintValidatorContext context) {
        for (Map.Entry<String, String> entry : patchMap.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            switch (fieldName) {
                case "firstname" : if (!isValidFirstname(fieldValue)){
                    context.buildConstraintViolationWithTemplate("Firstname must be between 4 and 50 characters")
                            .addConstraintViolation();
                    return false;
                }
                case "lastname" : if (!isValidLastname(fieldValue)){
                    context.buildConstraintViolationWithTemplate("Lastname must be between 4 and 50 characters")
                            .addConstraintViolation();
                    return false;
                }
                case "email" : if (!isValidEmail(fieldValue)){
                    context.buildConstraintViolationWithTemplate("Email must be in a normal email form")
                            .addConstraintViolation();
                    return false;
                }
                case "role" : if (!isValidRole(fieldValue)) {
                    context.buildConstraintViolationWithTemplate("Role value is not in the correct form")
                            .addConstraintViolation();
                    return false;
                }
                case "group" : if (!isValidGroup(fieldValue)){
                    context.buildConstraintViolationWithTemplate("UUID must be in a normal UUID from")
                            .addConstraintViolation();
                    return false;}
            }
        }
        return true;
    }

    public boolean isValidFirstname(String firstnameValue){
        return firstnameValue.length() >= 4 && firstnameValue.length() <= 50;
    }

    public boolean isValidLastname(String lastnameValue){
        return lastnameValue.length() >= 4 && lastnameValue.length() <= 50;
    }

    public boolean isValidEmail(String emailValue){
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(emailValue);
        return matcher.matches();
    }

    public boolean isValidRole(String roleValue){
        return Arrays.asList(Role.values()).contains(Role.valueOf(roleValue));
    }

    public boolean isValidGroup(String groupValue){
        try{
            var uuid = UUID.fromString(groupValue);
            return true;
        }catch (IllegalArgumentException ex){
            return false;
        }
    }
}
