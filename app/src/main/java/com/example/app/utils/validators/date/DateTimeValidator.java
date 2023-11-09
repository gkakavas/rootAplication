package com.example.app.utils.validators.date;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class DateTimeValidator implements ConstraintValidator<DateTime,String> {
    private String format;

    @Override
    public void initialize(DateTime constraintAnnotation) {
        this.format = constraintAnnotation.format();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Allow null or empty values
        }
        try {
            LocalDateTime parsedDateTime = LocalDateTime.parse(value);
            return true;
        } catch (Exception e) {
            return false; // Invalid LocalDateTime format
        }
    }
}
