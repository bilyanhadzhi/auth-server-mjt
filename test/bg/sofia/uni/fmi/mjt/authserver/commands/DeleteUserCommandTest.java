package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import bg.sofia.uni.fmi.mjt.authserver.user.Authority;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUserCommandTest {
    private static final String DEFAULT_COMMAND_TEXT =
            "delete-user --session-id %s --username %s";

    private CommandParser commandParser;
    private DeleteUserCommand command;

    @Mock
    private Authenticator authenticatorMock;

    @Mock
    private User requestUserMock;

    @Mock
    private User userToBeDeletedMock;

    private static final String DEFAULT_USERNAME = "user1";

    private static final UUID RANDOM_ID = UUID.randomUUID();
    private static final String INVALID_SESSION_ID_TEXT = "1234";

    @Before
    public void setUpCommand() throws CommandParseException {
        initMocks(this);

        commandParser = CommandParser.parse(DEFAULT_COMMAND_TEXT);
        command = new DeleteUserCommand(authenticatorMock, commandParser);
    }

    @Test
    public void testDeleteUserCommandInvalidSessionId() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, INVALID_SESSION_ID_TEXT,
                DEFAULT_USERNAME));

        command = new DeleteUserCommand(authenticatorMock, commandParser);

        String response = command.execute();

        assertEquals(DeleteUserCommand.getInvalidSessionIdResponse(), response);
    }

    @Test
    public void testDeleteUserCommandInvalidLogin() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                DEFAULT_USERNAME));

        command = new DeleteUserCommand(authenticatorMock, commandParser);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(null);

        String response = command.execute();

        assertEquals(DeleteUserCommand.getFailedSessionLoginResponse(), response);
    }

    @Test
    public void testDeleteUserCommandUserWhoRequestedNotAuthorized() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                DEFAULT_USERNAME));

        command = new DeleteUserCommand(authenticatorMock, commandParser);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(requestUserMock);

        when(requestUserMock.getAuthority())
                .thenReturn(Authority.USER);

        String response = command.execute();

        assertEquals(DeleteUserCommand.getUnauthorizedResponse(null), response);
    }

    @Test
    public void testDeleteUserCommandUserToRemoveNotFound() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                DEFAULT_USERNAME));

        command = new DeleteUserCommand(authenticatorMock, commandParser);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(requestUserMock);

        when(requestUserMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(authenticatorMock.getUserByUsername(DEFAULT_USERNAME))
                .thenReturn(userToBeDeletedMock);

        String response = command.execute();

        assertEquals(DeleteUserCommand.getSuccessfulUserDeletionResponse(), response);
    }
}
