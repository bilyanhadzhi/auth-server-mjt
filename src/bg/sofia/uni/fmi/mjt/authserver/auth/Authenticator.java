package bg.sofia.uni.fmi.mjt.authserver.auth;

import bg.sofia.uni.fmi.mjt.authserver.config.AuthConfiguration;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.FailedLoginException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.LockedUserException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;
import bg.sofia.uni.fmi.mjt.authserver.storage.AuthStorage;
import bg.sofia.uni.fmi.mjt.authserver.user.Authority;
import bg.sofia.uni.fmi.mjt.authserver.user.Email;
import bg.sofia.uni.fmi.mjt.authserver.user.Password;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.time.LocalDateTime;

public class Authenticator {
    private final AuthConfiguration configuration;
    private final AuthStorage storage;
    private final AuthSessionManager sessionManager;

    public Authenticator(AuthConfiguration configuration, AuthStorage storage,
                         AuthSessionManager sessionManager) {
        this.configuration = configuration;
        this.storage = storage;
        this.sessionManager = sessionManager;
    }

    public void registerUser(String username, Password password, String firstName, String lastName, Email email,
                             Authority authority)
            throws UserAlreadyExistsException, ValidationException {
        if (storage.getUserByUsername(username) != null) {
            throw new UserAlreadyExistsException("Trying to register user with a taken username");
        }

        password.validate();
        password.hash();

        email.validate();

        storage.addUser(new User(username, password, firstName, lastName, email, authority, null));
    }

    public void registerUser(String username, Password password, String firstName, String lastName, Email email)
            throws UserAlreadyExistsException, ValidationException {
        registerUser(username, password, firstName, lastName, email, Authority.USER);
    }

    public Session logUserInWithPassword(String username, String password) throws AuthenticationException {
        User foundUser = storage.getUserByUsername(username);

        if (foundUser == null) {
            throw new FailedLoginException("Username and/or password do not match any user");
        }

        if (foundUser.isLocked()) {
            throw new LockedUserException("Could not log user in: user is locked");
        }

        Password userPasswordHash = foundUser.getPasswordHash();
        if (!userPasswordHash.check(password)) {
            if (userPasswordHash.getFailedAttempts() > configuration.getMaxLoginAttemptFails()) {
                foundUser.setLockedUntil(LocalDateTime.now()
                        .plusSeconds(configuration.getLockTimeout()));
                foundUser.getPasswordHash().resetFailedAttempts();
            }

            storage.replaceUserByUsername(foundUser.getUsername(), foundUser);

            throw new FailedLoginException("Username and/or password do not match any user");
        }

        return sessionManager.logUserIn(foundUser.getUsername());
    }

    public Session logUserInWithSessionId(Session session) throws AuthenticationException {
        User foundUser = sessionManager.getUserBySession(session);

        if (foundUser == null) {
            throw new FailedLoginException("Could not log in: session id was not found");
        }

        return sessionManager.logUserIn(foundUser.getUsername());
    }

    public void replaceUser(String usernameOfReplaced, User toUser) {
        storage.replaceUserByUsername(usernameOfReplaced, toUser);
    }

    public User getUserBySession(Session session) {
        return sessionManager.getUserBySession(session);
    }

    public User getUserByUsername(String username) {
        return storage.getUserByUsername(username);
    }

    public void logUserOut(Session session) throws AuthenticationException {
        User foundUser = getUserBySession(session);

        if (foundUser == null) {
            throw new AuthenticationException("User with matching session id does not exist");
        }

        sessionManager.invalidateSession(session);
    }

    public void deleteUser(String username) {
        Session session = sessionManager.getSessionByUsername(username);
        if (session != null) {
            sessionManager.invalidateSession(session);
        }

        storage.removeUserByUsername(username);
    }

    public AuthConfiguration getConfiguration() {
        return configuration;
    }

    public long getAdminCount() {
        return storage.getAdminCount();
    }
}
