package com.example.app.utils.validator.date;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
            LocalDateTime parsedDateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(format));
            return true;
        } catch (Exception e) {
            return false; // Invalid LocalDateTime format
        }
    }
}
