package bg.sofia.uni.fmi.mjt.authserver.exceptions;

import java.util.List;

public class InvalidEmailException extends ValidationException {
    private final List<String> messages;

    public InvalidEmailException(List<String> messages) {
        super("Invalid email" + formatMessages(messages));
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
