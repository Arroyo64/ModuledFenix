package org.ies.fenix.client.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}