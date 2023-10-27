package com.example.app.utils.validator.date;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateTimeValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateTime {
    String message() default "Invalid date and time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String format() default "yyyy-MM-ddTHH:mm:ss";
}
