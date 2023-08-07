package com.example.app.utils.user.validation.patch;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
@Target({ElementType.TYPE_USE, ElementType.TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = UserAllowFieldsValidator.class)
@Documented
public @interface UserAllowFields {
    String message() default "You have perform illegal action";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
