package com.example.app.utils.validators.event;

import com.example.app.utils.validators.date.DateTimeValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Map;


public class EventPatchMapValueValidator implements ConstraintValidator<EventPatchValue, Map<String, String>> {

    private final DateTimeValidator dateValidator = new DateTimeValidator();
    @Override
    public boolean isValid(Map<String,String> patchMap, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        int constraintViolationsCounter = 0;
        for (Map.Entry<String, String> entry : patchMap.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            var fieldValues = Arrays.stream(AllowedEventFields.values()).map(Enum::name).toList();
            if (!fieldValues.contains(fieldName)) {
                context.buildConstraintViolationWithTemplate("Field name is not valid")
                        .addPropertyNode(fieldName)
                        .addConstraintViolation();
                ++constraintViolationsCounter;
            }
            switch (fieldName) {
                case "eventDescription" ->{
                    if (!isValidEventDescription(fieldValue)) {
                        context.buildConstraintViolationWithTemplate(
                                        "Event description should contain at least 5 and no many than 100 characters including spaces")
                                .addPropertyNode("eventDescription")
                                .addConstraintViolation();
                        ++constraintViolationsCounter;
                    }
                }
                case "eventBody" -> {
                    if (!isValidEventBody(fieldValue)) {
                        context.buildConstraintViolationWithTemplate(
                                        "Event body should contain at least 100 characters including spaces")
                                .addPropertyNode("eventBody")
                                .addConstraintViolation();
                        ++constraintViolationsCounter;
                    }
                }
                case "eventDateTime" -> {
                    if (!isValidEventDateTime(fieldValue)) {
                        context.buildConstraintViolationWithTemplate(
                                        "Invalid event date and time format. The correct format is yyyy-MM-ddTHH:mm:ss")
                                .addPropertyNode("eventDateTime")
                                .addConstraintViolation();
                        ++constraintViolationsCounter;
                    }
                }
                case "eventExpiration" -> {
                    if (!isValidEventExpiration(fieldValue)) {
                        context.buildConstraintViolationWithTemplate(
                                        "Invalid event expiration format. The correct format is yyyy-MM-ddTHH:mm:ss")
                                .addPropertyNode("eventExpiration")
                                .addConstraintViolation();
                        ++constraintViolationsCounter;
                    }
                }
            }
        }
        return constraintViolationsCounter == 0;
    }
    private boolean isValidEventDescription(String descriptionValue) {
        return descriptionValue.length()>=5 && descriptionValue.length()<=100;
    }
    private boolean isValidEventBody(String eventBodyValue) {
        return eventBodyValue.length()>=100;
    }

    private boolean isValidEventDateTime(String eventDateTimeValue) {
        return dateValidator.isValid(eventDateTimeValue,null);
    }
    private boolean isValidEventExpiration(String eventExpirationValue) {
        return dateValidator.isValid(eventExpirationValue,null);
    }

}
