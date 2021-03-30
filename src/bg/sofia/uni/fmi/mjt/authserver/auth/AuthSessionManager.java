package bg.sofia.uni.fmi.mjt.authserver.auth;

import bg.sofia.uni.fmi.mjt.authserver.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

/**
 * The authentication session manager keeps all ongoing sessions of users.
 */
public interface AuthSessionManager {
    /**
     * Logs user in and returns the new session. Invalidates previous session if it existed.
     * @param username the username of the user to be logged in
     * @return the newly created session
     */
    Session logUserIn(String username) throws AuthenticationException;

    /**
     * Retrieves a logged-in user by a session, identified by its id.
     * @param session the session to retrieve by
     * @return the found user or {@code null} if not found
     */
    User getUserBySession(Session session);

    /**
     * Retrieves the session for a given user. A user has at most one
     * valid session at any given time.
     * @param username the username to search by
     * @return their session, if a valid one exists, {@code null} otherwise.
     */
    Session getSessionByUsername(String username);

    /**
     * Invalidates a given session, if still valid.
     * @param session the session to look for
     */
    void invalidateSession(Session session);

    /**
     * Creates a new task which will execute whenever the session has to expire
     * and will invalidate it.
     * @param session the session to be scheduled for invalidation
     */
    void scheduleSessionInvalidation(Session session);
}
