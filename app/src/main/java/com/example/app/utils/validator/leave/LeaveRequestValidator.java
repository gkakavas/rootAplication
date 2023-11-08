package com.example.app.utils.validator.leave;

import com.example.app.models.requests.LeaveRequestEntity;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class LeaveRequestValidator implements ConstraintValidator<ValidLeaveRequest, LeaveRequestEntity> {
    @Override
    public boolean isValid(LeaveRequestEntity value, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();
        if (!isValidDate(value.getLeaveStarts())) {
            context.buildConstraintViolationWithTemplate("Invalid leaveStarts date format. The correct format is yyyy-MM-dd")
                    .addPropertyNode("leaveStarts")
                    .addConstraintViolation();
            isValid = false;
        }
        if (!isValidDate(value.getLeaveEnds())) {
            context.buildConstraintViolationWithTemplate("Invalid leaveEnds date format. The correct format is yyyy-MM-dd")
                    .addPropertyNode("leaveEnds")
                    .addConstraintViolation();
            isValid = false;
        }

        if (isValid) {
            if (!leaveStartsIsBeforeLeaveEnds(value)) {
                context.buildConstraintViolationWithTemplate("Invalid date values. Leave starting date should be before leave ending")
                        .addPropertyNode("leaveStarts")
                        .addConstraintViolation();
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean leaveStartsIsBeforeLeaveEnds(LeaveRequestEntity value) {
        var leaveEnds = LocalDate.parse(value.getLeaveEnds());
        var leaveStarts = LocalDate.parse(value.getLeaveStarts());
        return leaveStarts.isBefore(leaveEnds);
    }

    private boolean isValidDate(String dateString) {
        try {
            LocalDate.parse(dateString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
