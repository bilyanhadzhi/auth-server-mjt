package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateUserCommandTest {
    private static final String DEFAULT_COMMAND_TEXT =
            "update-user --session-id %s --new-username name --new-first-name name --new-last-name name"
                    + " --new-email %s";

    private static final UUID RANDOM_ID = UUID.randomUUID();
    private static final String INVALID_SESSION_ID_TEXT = "1234";

    private static final String VALID_EMAIL_TEXT = "mail@mail.com";
    private static final String INVALID_EMAIL_TEXT = "mail";

    private CommandParser commandParser;
    private UpdateUserCommand command;

    @Mock
    private Authenticator authenticatorMock;

    @Mock
    private User userMock;

    @Before
    public void setUpCommand() {
        authenticatorMock = mock(Authenticator.class);
    }

    @Test
    public void testUpdateUserCommandInvalidSessionId() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT,
                INVALID_SESSION_ID_TEXT, VALID_EMAIL_TEXT));
        command = new UpdateUserCommand(authenticatorMock, commandParser);

        String response = command.execute();
        assertEquals(UpdateUserCommand.getInvalidSessionIdResponse(), response);
    }

    @Test
    public void testUpdateUserCommandNonExistingUser() throws CommandParseException {
        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(null);

        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID, VALID_EMAIL_TEXT));
        command = new UpdateUserCommand(authenticatorMock, commandParser);

        String response = command.execute();
        assertEquals(UpdateUserCommand.getFailedSessionLoginResponse(), response);
    }

    @Test
    public void testUpdateUserUsernameTaken() throws CommandParseException {
        userMock = mock(User.class);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);
        when(authenticatorMock.getUserByUsername(any(String.class)))
                .thenReturn(userMock);

        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID, VALID_EMAIL_TEXT));
        command = new UpdateUserCommand(authenticatorMock, commandParser);

        command.execute();
        assertFalse(command.getValidationErrors().isEmpty());
    }

    @Test
    public void testUpdateUserInvalidEmail() throws CommandParseException {
        userMock = mock(User.class);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);

        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                INVALID_EMAIL_TEXT));
        command = new UpdateUserCommand(authenticatorMock, commandParser);

        command.execute();
        assertFalse(command.getValidationErrors().isEmpty());
    }

    @Test
    public void testUpdateUserValidUser() throws CommandParseException {
        userMock = mock(User.class);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);

        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                VALID_EMAIL_TEXT));
        command = new UpdateUserCommand(authenticatorMock, commandParser);

        String response = command.execute();
        assertEquals(UpdateUserCommand.getSuccessfulUpdateResponse(), response);
    }
}
