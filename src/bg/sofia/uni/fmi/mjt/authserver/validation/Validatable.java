package bg.sofia.uni.fmi.mjt.authserver.validation;

import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;

import java.util.List;

/**
 * Represents an object that can be validated.
 */
public interface Validatable {
    /**
     * Checks for the object's validity.
     * @throws ValidationException if the object was not in a valid state
     */
    void validate() throws ValidationException;

    /**
     * Gets all invalidity messages. If the object is in a valid state,
     * it should be an empty list.
     * @return a list of messages relating to issues with the object's validity.
     */
    List<String> getMessages();
}
