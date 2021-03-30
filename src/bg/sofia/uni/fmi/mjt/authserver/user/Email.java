package bg.sofia.uni.fmi.mjt.authserver.user;

import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;
import bg.sofia.uni.fmi.mjt.authserver.validation.Validatable;
import bg.sofia.uni.fmi.mjt.authserver.validation.Validator;

import java.util.List;
import java.util.Objects;

public class Email implements Validatable {
    private final String value;
    private final Validator<Email> validator;

    public Email(String value, Validator<Email> validator) {
        this.value = value;
        this.validator = validator;
    }

    @Override
    public void validate() throws ValidationException {
        if (!validator.validate(this)) {
            throw new InvalidEmailException(getMessages());
        }
    }

    @Override
    public List<String> getMessages() {
        return validator.getValidationMessages();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Email otherEmail = (Email) other;
        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
