package bg.sofia.uni.fmi.mjt.authserver.storage;

import bg.sofia.uni.fmi.mjt.authserver.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

/**
 * <p>
 *     The storage solution for the authentication server
 * </p>
 */
public interface AuthStorage {
    /**
     * Adds a user to the storage.
     * @param user the user to be added
     * @throws UserAlreadyExistsException if a user with the same username already existed
     */
    void addUser(User user) throws UserAlreadyExistsException;

    /**
     * Retrieves a user from the storage by their username.
     * @param username the username of the queried user
     * @return the found user or {@code null} if they were not found
     */
    User getUserByUsername(String username);

    /**
     * Looks for a user in the storage and removes them if found.
     * @param username the username of the user to be removed
     */
    void removeUserByUsername(String username);

    /**
     * Looks for a user in the storage and replaces them if found
     * @param username the username of the user to be replaced
     */
    void replaceUserByUsername(String username, User newUser);

    /**
     * Admins are specially authorized users able to delete users,
     * add or remove other admins etc.
     * @return the number of users who are admins
     */
    long getAdminCount();
}
