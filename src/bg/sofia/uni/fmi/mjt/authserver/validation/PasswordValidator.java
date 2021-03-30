package bg.sofia.uni.fmi.mjt.authserver.validation;

import bg.sofia.uni.fmi.mjt.authserver.user.Password;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidator implements Validator<Password> {
    public static final String INVALID_MESSAGE_TOO_SHORT = "Password is too short, must be at least %d characters long";
    public static final String INVALID_MESSAGE_NO_LOWERCASE = "Password must contain a lowercase letter";
    public static final String INVALID_MESSAGE_NO_UPPERCASE = "Password must contain an uppercase letter";
    public static final String INVALID_MESSAGE_NO_DIGITS = "Password must contain a digit";

    public static final int MIN_PASSWORD_LEN = 8;

    List<String> messages = new ArrayList<>();

    @Override
    public boolean validate(Password password) {
        messages.clear();

        String passwordText = password.getValue();
        final int passwordLen = passwordText.length();

        if (passwordLen < MIN_PASSWORD_LEN) {
            messages.add(String.format(INVALID_MESSAGE_TOO_SHORT, MIN_PASSWORD_LEN));
        }

        boolean containsLower = false;
        boolean containsUpper = false;
        boolean containsNumeric = false;

        for (int i = 0; i < passwordLen; i++) {
            char curr = passwordText.charAt(i);

            containsLower = containsLower || Character.isLowerCase(curr);
            containsUpper = containsUpper || Character.isUpperCase(curr);
            containsNumeric = containsNumeric || Character.isDigit(curr);
        }

        if (!containsLower) {
            messages.add(INVALID_MESSAGE_NO_LOWERCASE);
        }
        if (!containsUpper) {
            messages.add(INVALID_MESSAGE_NO_UPPERCASE);
        }
        if (!containsNumeric) {
            messages.add(INVALID_MESSAGE_NO_DIGITS);
        }

        return messages.isEmpty();
    }

    @Override
    public List<String> getValidationMessages() {
        return messages;
    }
}
