
package com.example.app.utils.validators.venum;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, String> {
    private List<String> acceptedValues;

    @Override
    public void initialize(ValueOfEnum annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) throws ConstraintViolationException {
        if (value == null) {
            return true;
        }
        return acceptedValues.contains(value);
    }
}
