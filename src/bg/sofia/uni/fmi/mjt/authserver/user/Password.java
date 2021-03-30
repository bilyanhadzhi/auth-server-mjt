package bg.sofia.uni.fmi.mjt.authserver.user;

import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;
import bg.sofia.uni.fmi.mjt.authserver.validation.Validatable;
import bg.sofia.uni.fmi.mjt.authserver.validation.Validator;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Objects;

public class Password implements Validatable {
    private static final String VALIDATION_MESSAGE_PREFIX = System.lineSeparator() + "  - ";

    private String value;
    private int failedAttempts = 0;
    private final Validator<Password> validator;

    public Password(String value, Validator<Password> validator) {
        this.value = value;
        this.validator = validator;
    }

    public Password(String value, Validator<Password> validator, int failedAttempts) {
        this.value = value;
        this.validator = validator;
        if (failedAttempts >= 0) {
            this.failedAttempts = failedAttempts;
        }
    }

    @Override
    public void validate() throws ValidationException {
        if (!validator.validate(this)) {
            throw new InvalidPasswordException(getMessages());
        }
    }

    @Override
    public List<String> getMessages() {
        return validator.getValidationMessages();
    }

    public String getValue() {
        return value;
    }

    public void hash() {
        value = BCrypt.hashpw(value, BCrypt.gensalt());
    }

    public boolean check(String guess) {
        boolean correct = BCrypt.checkpw(guess, value);

        if (!correct) {
            failedAttempts++;
        }

        return correct;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void resetFailedAttempts() {
        failedAttempts = 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Password otherPassword = (Password) other;
        return value.equals(otherPassword.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
