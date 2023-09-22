package com.example.app.utils.validator.uuid;

import com.example.app.exception.NullUuidException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class UUIDValidator {
    private static final String pattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    public static boolean isValid(String stringToValidate) {
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(stringToValidate);
        return matcher.matches();
    }
}
