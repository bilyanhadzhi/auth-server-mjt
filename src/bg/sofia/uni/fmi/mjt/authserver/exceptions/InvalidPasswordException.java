package bg.sofia.uni.fmi.mjt.authserver.exceptions;

import java.util.Collections;
import java.util.List;

public class InvalidPasswordException extends ValidationException {
    private final List<String> messages;

    public InvalidPasswordException(List<String> messages) {
        super("Invalid password:"
                + System.lineSeparator() + formatMessages(messages));
        this.messages = messages;
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
