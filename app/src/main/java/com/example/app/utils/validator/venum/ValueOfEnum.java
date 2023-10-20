package com.example.app.utils.validator.venum;

import com.example.app.utils.validator.venum.ValueOfEnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValueOfEnumValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})

public @interface ValueOfEnum {
    Class<? extends Enum<?>> enumClass();
    String message() default "error value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

