package bg.sofia.uni.fmi.mjt.authserver.exceptions;

import java.util.List;

public class ValidationException extends Exception {
    protected static final String MESSAGE_PREFIX = "  - " + System.lineSeparator();

    public ValidationException(String msg) {
        super(msg);
    }

    protected static String formatMessages(List<String> messages) {
        return String.join(MESSAGE_PREFIX, messages);
    }
}
