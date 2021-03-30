package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class LogoutCommandTest {
    private static final String DEFAULT_COMMAND_TEXT = "logout --session-id %s";

    private static final UUID RANDOM_ID = UUID.randomUUID();
    private static final String INVALID_SESSION_ID_TEXT = "1234";

    private CommandParser commandParser;
    private LogoutCommand command;

    @Mock
    private Authenticator authenticatorMock;

    @Before
    public void setUpMocks() {
        authenticatorMock = mock(Authenticator.class);
    }

    @Test
    public void testLogoutCommandInvalidSessionId() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT,
                INVALID_SESSION_ID_TEXT));
        command = new LogoutCommand(authenticatorMock, commandParser);

        String response = command.execute();
        assertEquals(LogoutCommand.getInvalidSessionIdResponse(), response);
    }

    @Test
    public void testLogoutCommandFailedLogin() throws CommandParseException,
            AuthenticationException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID));
        command = new LogoutCommand(authenticatorMock, commandParser);

        doThrow(AuthenticationException.class)
                .when(authenticatorMock)
                .logUserOut(any(Session.class));

        String response = command.execute();
        assertEquals(LogoutCommand.getFailedSessionLoginResponse(), response);
    }

    @Test
    public void testLogoutCommandSuccessful() throws CommandParseException,
            AuthenticationException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID));
        command = new LogoutCommand(authenticatorMock, commandParser);

        doNothing()
                .when(authenticatorMock)
                .logUserOut(any(Session.class));

        String response = command.execute();
        assertEquals(LogoutCommand.getSuccessfulLogoutResponse(), response);
    }
}
