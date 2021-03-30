package bg.sofia.uni.fmi.mjt.authserver.validation;

import java.util.List;

/**
 * Represents an object that can check for another object's validity.
 * @param <T> the type of the object to be validated
 */
public interface Validator<T> {
    /**
     * Checks for an object's validity.
     * @param data the data to be checked for a valid state
     * @return whether the data is valid
     */
    boolean validate(T data);

    /**
     * Gets all invalidity messages. If the object is in a valid state,
     * it should be an empty list.
     * @return a list of messages relating to issues with the object's validity.
     */
    List<String> getValidationMessages();
}
