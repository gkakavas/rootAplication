package com.example.app.utils.validators.leave;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LeaveRequestValidator.class)
@Documented
public @interface ValidLeaveRequest {
    String message() default "Invalid leave request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
