package com.example.app.utils.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.EnumUtils;

public class UserAllowFieldsValidator implements ConstraintValidator<UserAllowFields,String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) throws ConstraintViolationException {
        return EnumUtils.isValidEnum(AllowUserFields.class, value);
    }
}
