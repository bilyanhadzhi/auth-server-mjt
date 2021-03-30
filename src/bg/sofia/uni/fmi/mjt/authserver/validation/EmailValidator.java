package bg.sofia.uni.fmi.mjt.authserver.validation;

import bg.sofia.uni.fmi.mjt.authserver.user.Email;

import java.util.ArrayList;
import java.util.List;

public class EmailValidator implements Validator<Email> {
    public static final String INVALID_MESSAGE_NO_DOTS = "No dots found";
    public static final String INVALID_MESSAGE_NO_AT_SYMBOL = "No @ symbol found";
    public static final String INVALID_MESSAGE_TLD_INVALID_LENGTH =
            "Top-level domain is not between 2 and 4 characters long";
    public static final String INVALID_MESSAGE_NO_SECOND_LEVEL_DOMAIN = "Second-level domain not found";
    public static final String INVALID_MESSAGE_AT_SYMBOL_IN_BEGINNING =
            "@ sign can not be in the beginning of the email";

    private static final char CHAR_EMAIL_AT = '@';
    private static final char CHAR_EMAIL_DOT = '.';

    private static final int TLD_MIN_LEN = 2;
    private static final int TLD_MAX_LEN = 4;

    private final List<String> messages = new ArrayList<>();

    // Note: this is a loose attempt at validating email addresses,
    // mostly looking at the @ symbol and the domains
    @Override
    public boolean validate(Email data) {
        String emailValue = data.getValue();
        final int emailLen = emailValue.length();

        boolean isValid = true;

        int lastAtSignIndex = -1;
        int lastDotIndex = -1;

        for (int i = emailLen - 1; i >= 0 && (lastDotIndex == -1 || lastAtSignIndex == -1); --i) {
            char curr = emailValue.charAt(i);

            if (curr == CHAR_EMAIL_DOT && lastDotIndex == -1) {
                lastDotIndex = i;
            }

            if (curr == CHAR_EMAIL_AT && lastAtSignIndex == -1) {
                lastAtSignIndex = i;
            }
        }

        // check @ and . are found
        if (lastAtSignIndex == -1) {
            isValid = false;
            messages.add(INVALID_MESSAGE_NO_AT_SYMBOL);
        }
        if (lastDotIndex == -1) {
            isValid = false;
            messages.add(INVALID_MESSAGE_NO_DOTS);
        }

        // make sure top-level domain is [2,4] chars in length
        final int topLevelDomainLen = emailLen - lastDotIndex - 1;
        if (topLevelDomainLen < TLD_MIN_LEN || topLevelDomainLen > TLD_MAX_LEN) {
            isValid = false;
            messages.add(INVALID_MESSAGE_TLD_INVALID_LENGTH);
        }

        // make sure 2nd-level domain exists
        if (lastDotIndex - lastAtSignIndex < 1) {
            isValid = false;
            messages.add(INVALID_MESSAGE_NO_SECOND_LEVEL_DOMAIN);
        }

        // make sure last @ is not the first character
        if (lastAtSignIndex == 0) {
            isValid = false;
            messages.add(INVALID_MESSAGE_AT_SYMBOL_IN_BEGINNING);
        }

        return isValid;
    }

    @Override
    public List<String> getValidationMessages() {
        return messages;
    }
}
