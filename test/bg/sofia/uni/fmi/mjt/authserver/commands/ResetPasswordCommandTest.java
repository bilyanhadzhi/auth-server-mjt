package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.authserver.user.Password;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import bg.sofia.uni.fmi.mjt.authserver.validation.PasswordValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResetPasswordCommandTest {
    private static final String DEFAULT_COMMAND_TEXT =
            "reset-password --session-id %s --username %s --old-password %s --new-password %s";

    private static final String DEFAULT_USERNAME = "name";
    private static final String SECOND_USERNAME = "name2";

    private static final UUID RANDOM_ID = UUID.randomUUID();
    private static final String INVALID_SESSION_ID_TEXT = "1234";

    private static final String INVALID_PASSWORD_TEXT = "password123";
    private static final String VALID_PASSWORD_TEXT = "Password5";
    private static final String SECOND_PASSWORD_TEXT = "Password6";

    private CommandParser commandParser;
    private ResetPasswordCommand command;

    @Mock
    private Authenticator authenticatorMock;

    @Mock
    private User userMock;

    @Before
    public void setUpMocks() {
        authenticatorMock = mock(Authenticator.class);
    }

    @Test
    public void testResetPasswordCommandInvalidSessionId() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT,
                INVALID_SESSION_ID_TEXT, DEFAULT_USERNAME, VALID_PASSWORD_TEXT, VALID_PASSWORD_TEXT));
        command = new ResetPasswordCommand(authenticatorMock, commandParser);

        String response = command.execute();
        assertEquals(RegisterCommand.getInvalidSessionIdResponse(), response);
    }

    @Test
    public void testResetPasswordCommandNonExistingUser() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT,
                RANDOM_ID, DEFAULT_USERNAME, VALID_PASSWORD_TEXT, VALID_PASSWORD_TEXT));
        command = new ResetPasswordCommand(authenticatorMock, commandParser);

        String response = command.execute();
        assertEquals(ResetPasswordCommand.getFailedSessionLoginResponse(), response);
    }

    @Test
    public void testResetPasswordCommandNonMatchingUser() throws CommandParseException {
        userMock = mock(User.class);
        when(userMock.getUsername())
                .thenReturn(SECOND_USERNAME);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);

        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT,
                RANDOM_ID, DEFAULT_USERNAME, VALID_PASSWORD_TEXT, VALID_PASSWORD_TEXT));

        command = new ResetPasswordCommand(authenticatorMock, commandParser);

        String response = command.execute();
        assertEquals(ResetPasswordCommand.getUsernameDoesNotMatchResponse(), response);
    }

    @Test
    public void testResetPasswordCommandNonMatchingOldPassword() throws CommandParseException {
        userMock = mock(User.class);
        when(userMock.getUsername())
                .thenReturn(DEFAULT_USERNAME);

        Password password = new Password(VALID_PASSWORD_TEXT, new PasswordValidator());
        password.hash();

        when(userMock.getPasswordHash())
                .thenReturn(password);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);

        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT,
                RANDOM_ID, DEFAULT_USERNAME, SECOND_PASSWORD_TEXT, VALID_PASSWORD_TEXT));

        command = new ResetPasswordCommand(authenticatorMock, commandParser);

        String response = command.execute();
        assertEquals(ResetPasswordCommand.getPasswordDoesNotMatchResponse(), response);
    }

    @Test
    public void testResetPasswordCommandInvalidNewPassword() throws CommandParseException {
        userMock = mock(User.class);
        when(userMock.getUsername())
                .thenReturn(DEFAULT_USERNAME);

        Password password = new Password(VALID_PASSWORD_TEXT, new PasswordValidator());
        password.hash();

        when(userMock.getPasswordHash())
                .thenReturn(password);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);

        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT,
                RANDOM_ID, DEFAULT_USERNAME, VALID_PASSWORD_TEXT, INVALID_PASSWORD_TEXT));

        command = new ResetPasswordCommand(authenticatorMock, commandParser);

        String response = command.execute();
        assertEquals(ResetPasswordCommand.getInvalidPasswordResponse(
                new InvalidPasswordException(List.of(PasswordValidator.INVALID_MESSAGE_NO_UPPERCASE))),
                response
        );
    }
}
