package com.example.app.utils.validator.user;

import com.example.app.entities.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserPatchMapValueValidator implements ConstraintValidator<UserPatchValue, Map<String,String>> {

    @Override
    public boolean isValid(Map<String, String> patchMap, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        int constraintViolationsCounter = 0;
        for (Map.Entry<String, String> entry : patchMap.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            var fieldValues = Arrays.stream(AllowedUserFields.values()).map(Enum::name).toList();
                if (!fieldValues.contains(fieldName)) {
                    context.buildConstraintViolationWithTemplate("Field value is not valid")
                            .addPropertyNode(fieldName)
                            .addConstraintViolation();
                    ++constraintViolationsCounter;
                }
            switch (fieldName) {
                case "firstname" ->{
                    if (!isValidFirstname(fieldValue)) {
                        context.buildConstraintViolationWithTemplate(
                                "Firstname must be between 4 and 50 characters")
                                .addPropertyNode("firstname")
                                .addConstraintViolation();
                        ++constraintViolationsCounter;
                    }
                }
                case "lastname" -> {
                    if (!isValidLastname(fieldValue)) {
                        context.buildConstraintViolationWithTemplate(
                                "Lastname must be between 4 and 50 characters")
                                .addPropertyNode("lastname")
                                .addConstraintViolation();
                        ++constraintViolationsCounter;
                    }
                }
                case "email" -> {
                    if (!isValidEmail(fieldValue)) {
                        context.buildConstraintViolationWithTemplate(
                                "Email must be in a normal email form")
                                .addPropertyNode("email")
                                .addConstraintViolation();
                        ++constraintViolationsCounter;
                    }
                }
                case "role" -> {
                    if (!isValidRole(fieldValue)) {
                        context.buildConstraintViolationWithTemplate(
                                "Role value is not in the correct form")
                                .addPropertyNode("role")
                                .addConstraintViolation();
                        ++constraintViolationsCounter;
                    }
                }
                case "group" -> {
                    if (!isValidGroup(fieldValue)) {
                        context.buildConstraintViolationWithTemplate(
                                "UUID must be in a normal UUID form")
                                .addPropertyNode("group")
                                .addConstraintViolation();
                        ++constraintViolationsCounter;
                    }
                }
            }
        }
        return constraintViolationsCounter <= 0;
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
