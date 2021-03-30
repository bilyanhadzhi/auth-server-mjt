package bg.sofia.uni.fmi.mjt.authserver.auth;

import bg.sofia.uni.fmi.mjt.authserver.config.AuthConfiguration;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.FailedLoginException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.LockedUserException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;
import bg.sofia.uni.fmi.mjt.authserver.storage.AuthStorage;
import bg.sofia.uni.fmi.mjt.authserver.storage.TSVFileAuthStorage;
import bg.sofia.uni.fmi.mjt.authserver.user.Authority;
import bg.sofia.uni.fmi.mjt.authserver.user.Email;
import bg.sofia.uni.fmi.mjt.authserver.user.Password;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import bg.sofia.uni.fmi.mjt.authserver.validation.EmailValidator;
import bg.sofia.uni.fmi.mjt.authserver.validation.PasswordValidator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AuthenticatorTest {
    private static final AuthConfiguration DEFAULT_CONFIG;
    private static final Path DEFAULT_USERS_PATH = Path.of("test-users.tsv");
    private static final Path DEFAULT_SESSIONS_PATH = Path.of("test-sessions.tsv");
    private static final Path DEFAULT_AUDIT_LOG_PATH = Path.of("test-audit.log");

    private static final PasswordValidator DEFAULT_PASS_VALIDATOR = new PasswordValidator();
    private static final EmailValidator DEFAULT_EMAIL_VALIDATOR = new EmailValidator();

    private static final String DEFAULT_USERNAME = "john_doe";
    private static final String SECOND_VALID_USERNAME = "jane_doe";

    private static final String DEFAULT_FIRST_NAME = "John";
    private static final String SECOND_VALID_FIRST_NAME = "James";

    private static final String DEFAULT_LAST_NAME = "Doe";
    private static final String SECOND_VALID_LAST_NAME = "Vowles";

    private static final String DEFAULT_PASSWORD = "DefaultPassword678";
    private static final String SECOND_VALID_PASSWORD = "DefaultPassword123";
    private static final String INVALID_PASSWORD_TOO_SHORT = "Pass123";
    private static final String INVALID_PASSWORD_NO_DIGITS = "Password";
    private static final String INVALID_PASSWORD_NO_UPPER = "password123";
    private static final String INVALID_PASSWORD_NO_LOWER = "PASSWORD123";

    private static final String DEFAULT_EMAIL = "default@mail.com";
    private static final String SECOND_VALID_EMAIL = "second@mail.com";
    private static final String INVALID_EMAIL_NO_AT_SIGN = "defaultmail.com";
    private static final String INVALID_EMAIL_NO_DOT = "default@mail";
    private static final String INVALID_EMAIL_NO_TLD = "de.fault@mail";
    private static final String INVALID_EMAIL_TLD_LEN_TOO_SHORT = "default@mail.a";
    private static final String INVALID_EMAIL_TLD_LEN_TOO_LONG = "default@mail.abcde";

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    static {
        DEFAULT_CONFIG = AuthConfiguration.builder(DEFAULT_USERS_PATH,
                DEFAULT_SESSIONS_PATH, DEFAULT_AUDIT_LOG_PATH)
                .setLockTimeout(60)
                .setMinimumAdminCount(1)
                .setMaxLoginAttemptFailures(1)
                .build();
    }

    private static Authenticator authenticator;
    private static AuthStorage storage;
    private static AuthSessionManager sessionManager;

    @BeforeClass
    public static void initAuthenticator() {
        storage = new TSVFileAuthStorage(DEFAULT_USERS_PATH);
        sessionManager = new TSVFileSessionManager(DEFAULT_CONFIG, storage);
        authenticator = new Authenticator(DEFAULT_CONFIG, storage, sessionManager);
    }

    @Before
    public void emptyFiles() throws IOException {
        try (var usersWriter = new FileWriter(DEFAULT_USERS_PATH.toString());
             var sessionsWriter = new FileWriter(DEFAULT_SESSIONS_PATH.toString())) {
        }
    }

    private void registerDefaultUser() throws UserAlreadyExistsException, ValidationException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(DEFAULT_PASSWORD, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(DEFAULT_EMAIL, DEFAULT_EMAIL_VALIDATOR));
    }

    private Session logDefaultUserInWithPassword() throws AuthenticationException {
        return authenticator.logUserInWithPassword(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    @Test(expected = InvalidPasswordException.class)
    public void testRegisterUserWithInvalidPasswordTooShort()
            throws ValidationException, UserAlreadyExistsException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(INVALID_PASSWORD_TOO_SHORT, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(DEFAULT_EMAIL, DEFAULT_EMAIL_VALIDATOR));
    }

    @Test(expected = InvalidPasswordException.class)
    public void testRegisterUserWithInvalidPasswordNoDigits()
            throws ValidationException, UserAlreadyExistsException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(INVALID_PASSWORD_NO_DIGITS, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(DEFAULT_EMAIL, DEFAULT_EMAIL_VALIDATOR));
    }

    @Test(expected = InvalidPasswordException.class)
    public void testRegisterUserWithInvalidPasswordNoUpper()
            throws ValidationException, UserAlreadyExistsException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(INVALID_PASSWORD_NO_UPPER, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(DEFAULT_EMAIL, DEFAULT_EMAIL_VALIDATOR));
    }

    @Test(expected = InvalidPasswordException.class)
    public void testRegisterUserWithInvalidPasswordNoLower()
            throws ValidationException, UserAlreadyExistsException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(INVALID_PASSWORD_NO_LOWER, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(DEFAULT_EMAIL, DEFAULT_EMAIL_VALIDATOR));
    }

    @Test(expected = InvalidEmailException.class)
    public void testRegisterUserWithInvalidEmailNoAtSign()
            throws ValidationException, UserAlreadyExistsException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(DEFAULT_PASSWORD, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(INVALID_EMAIL_NO_AT_SIGN, DEFAULT_EMAIL_VALIDATOR));
    }

    @Test(expected = InvalidEmailException.class)
    public void testRegisterUserWithInvalidEmailNoDot()
            throws ValidationException, UserAlreadyExistsException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(DEFAULT_PASSWORD, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(INVALID_EMAIL_NO_DOT, DEFAULT_EMAIL_VALIDATOR));
    }


    @Test(expected = InvalidEmailException.class)
    public void testRegisterUserWithInvalidEmailNoTLD()
            throws ValidationException, UserAlreadyExistsException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(DEFAULT_PASSWORD, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(INVALID_EMAIL_NO_TLD, DEFAULT_EMAIL_VALIDATOR));
    }

    @Test(expected = InvalidEmailException.class)
    public void testRegisterUserWithInvalidEmailTLDTooShort()
            throws ValidationException, UserAlreadyExistsException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(DEFAULT_PASSWORD, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(INVALID_EMAIL_TLD_LEN_TOO_SHORT, DEFAULT_EMAIL_VALIDATOR));
    }

    @Test(expected = InvalidEmailException.class)
    public void testRegisterUserWithInvalidEmailTLDTooLong()
            throws ValidationException, UserAlreadyExistsException {
        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(DEFAULT_PASSWORD, DEFAULT_PASS_VALIDATOR),
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME,
                new Email(INVALID_EMAIL_TLD_LEN_TOO_LONG, DEFAULT_EMAIL_VALIDATOR));
    }

    @Test
    public void testRegisterValidUser() throws ValidationException, UserAlreadyExistsException {
        assertNull(storage.getUserByUsername(DEFAULT_USERNAME));

        registerDefaultUser();

        Password expectedPassword = new Password(DEFAULT_PASSWORD, DEFAULT_PASS_VALIDATOR);
        expectedPassword.hash();
        Email expectedEmail = new Email(DEFAULT_EMAIL, DEFAULT_EMAIL_VALIDATOR);
        Authority expectedAuthority = Authority.USER;

        User expectedUser = new User(DEFAULT_USERNAME, expectedPassword,
                DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME, expectedEmail, expectedAuthority, null);

        User registeredUser = storage.getUserByUsername(DEFAULT_USERNAME);

        assertEquals(expectedUser, registeredUser);
        assertTrue(expectedUser.getPasswordHash().check(DEFAULT_PASSWORD));
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void testRegisterUserWithTakenUsername() throws ValidationException, UserAlreadyExistsException {
        registerDefaultUser();

        authenticator.registerUser(DEFAULT_USERNAME,
                new Password(SECOND_VALID_PASSWORD, DEFAULT_PASS_VALIDATOR),
                SECOND_VALID_FIRST_NAME, SECOND_VALID_LAST_NAME,
                new Email(SECOND_VALID_EMAIL, DEFAULT_EMAIL_VALIDATOR));
    }

    @Test(expected = FailedLoginException.class)
    public void testLoginWithNonExistingUsername() throws AuthenticationException,
            ValidationException, UserAlreadyExistsException {
        registerDefaultUser();

        authenticator.logUserInWithPassword(SECOND_VALID_USERNAME, DEFAULT_PASSWORD);
    }

    @Test(expected = LockedUserException.class)
    public void testLoginWithLockedUser() throws AuthenticationException, UserAlreadyExistsException,
            ValidationException {
        registerDefaultUser();

        User registeredUser = storage.getUserByUsername(DEFAULT_USERNAME);
        registeredUser.setLockedUntil(LocalDateTime.now().plusHours(1));

        storage.replaceUserByUsername(registeredUser.getUsername(), registeredUser);
        authenticator.logUserInWithPassword(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    @Test(expected = FailedLoginException.class)
    public void testLoginWithExistingUsernameWrongPassword() throws AuthenticationException,
            UserAlreadyExistsException, ValidationException {
        registerDefaultUser();

        authenticator.logUserInWithPassword(DEFAULT_USERNAME, SECOND_VALID_PASSWORD);
    }

    @Test
    public void testMoreThanMaxFailedLoginAttemptsBlocksUser() throws UserAlreadyExistsException,
            ValidationException {
        registerDefaultUser();

        for (int i = 0; i < DEFAULT_CONFIG.getMaxLoginAttemptFails(); i++) {
            try {
                authenticator.logUserInWithPassword(DEFAULT_USERNAME, SECOND_VALID_PASSWORD);
            } catch (AuthenticationException exception) {
                // do nothing
            }
        }

        assertFalse(storage.getUserByUsername(DEFAULT_USERNAME).isLocked());
        try {
            authenticator.logUserInWithPassword(DEFAULT_USERNAME, SECOND_VALID_PASSWORD);
        } catch (AuthenticationException exception) {
            // do nothing
        }

        assertTrue(storage.getUserByUsername(DEFAULT_USERNAME).isLocked());
    }

    @Test
    public void testValidLoginWithPasswordSetsNewUserSession() throws UserAlreadyExistsException,
            ValidationException, AuthenticationException {
        registerDefaultUser();

        User registeredUser = storage.getUserByUsername(DEFAULT_USERNAME);
        Session firstSession = logDefaultUserInWithPassword();
        Session secondSession = logDefaultUserInWithPassword();

        assertNull(sessionManager.getUserBySession(firstSession));
        assertEquals(registeredUser, sessionManager.getUserBySession(secondSession));
    }

    @Test(expected = FailedLoginException.class)
    public void testLoginWithWrongSessionId() throws UserAlreadyExistsException,
            ValidationException, AuthenticationException {
        registerDefaultUser();
        logDefaultUserInWithPassword();

        Session randomSession = new Session(RANDOM_UUID);
        authenticator.logUserInWithSessionId(randomSession);
    }

    @Test
    public void testValidLoginWithSessionIdSetsNewUserSession() throws UserAlreadyExistsException,
            ValidationException, AuthenticationException {
        registerDefaultUser();

        User registeredUser = storage.getUserByUsername(DEFAULT_USERNAME);
        Session firstSession = logDefaultUserInWithPassword();
        Session secondSession = authenticator.logUserInWithSessionId(firstSession);

        assertNull(sessionManager.getUserBySession(firstSession));
        assertEquals(registeredUser, sessionManager.getUserBySession(secondSession));
    }

    @Test
    public void testUpdateUserChangesAllUserAttributes() throws UserAlreadyExistsException,
            ValidationException {
        registerDefaultUser();
        User registeredUser = authenticator.getUserByUsername(DEFAULT_USERNAME);

        registeredUser.setUsername(SECOND_VALID_USERNAME);

        LocalDateTime lockedUntil = LocalDateTime.now();
        registeredUser.setLockedUntil(lockedUntil);

        registeredUser.setAuthority(Authority.ADMIN);

        Password newPassword = new Password(SECOND_VALID_PASSWORD, new PasswordValidator());
        newPassword.validate();
        newPassword.hash();

        registeredUser.setPasswordHash(newPassword);

        registeredUser.setFirstName(SECOND_VALID_FIRST_NAME);
        registeredUser.setLastName(SECOND_VALID_LAST_NAME);

        Email newEmail = new Email(SECOND_VALID_EMAIL, new EmailValidator());
        registeredUser.setEmail(newEmail);

        authenticator.replaceUser(DEFAULT_USERNAME, registeredUser);

        User replacedUser = authenticator.getUserByUsername(SECOND_VALID_USERNAME);
        assertEquals(registeredUser, replacedUser);
        assertEquals(registeredUser.getPasswordHash(), replacedUser.getPasswordHash());
        assertEquals(registeredUser.getAuthority(), replacedUser.getAuthority());
        assertEquals(registeredUser.getEmail(), replacedUser.getEmail());
        assertEquals(registeredUser.getFirstName(), replacedUser.getFirstName());
        assertEquals(registeredUser.getLastName(), replacedUser.getLastName());
        assertEquals(registeredUser.getLockedUntil(), replacedUser.getLockedUntil());
    }

    @Test(expected = AuthenticationException.class)
    public void testLogUserOutForNotLoggedInUser() throws AuthenticationException {
        authenticator.logUserOut(new Session(RANDOM_UUID));
    }

    @Test
    public void testLogUserOutForLoggedInUserInvalidatesSession() throws AuthenticationException,
            UserAlreadyExistsException, ValidationException {
        registerDefaultUser();

        User registeredUser = storage.getUserByUsername(DEFAULT_USERNAME);
        Session session = authenticator.logUserInWithPassword(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        assertEquals(registeredUser, sessionManager.getUserBySession(session));
        authenticator.logUserOut(session);
        assertNull(authenticator.getUserBySession(session));
    }

    @Test
    public void testDeleteUserRemovesExistingUser() throws AuthenticationException,
            UserAlreadyExistsException, ValidationException {
        registerDefaultUser();

        User registeredUser = storage.getUserByUsername(DEFAULT_USERNAME);
        assertNotNull(authenticator.getUserByUsername(registeredUser.getUsername()));

        authenticator.deleteUser(registeredUser.getUsername());
        assertNull(authenticator.getUserByUsername(registeredUser.getUsername()));
    }
}
