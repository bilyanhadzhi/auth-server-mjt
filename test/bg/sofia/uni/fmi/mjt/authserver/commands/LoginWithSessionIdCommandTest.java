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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class LoginWithSessionIdCommandTest {
    private static final String DEFAULT_COMMAND_TEXT = "login --session-id %s";

    private static final UUID RANDOM_ID = UUID.randomUUID();
    private static final String INVALID_SESSION_ID_TEXT = "1234";

    private CommandParser commandParser;
    private LoginWithSessionIdCommand command;

    @Mock
    private Authenticator authenticatorMock;

    @Before
    public void setUpCommand() {
        initMocks(this);
    }

    @Test
    public void testLoginCommandInvalidSessionId() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT,
                INVALID_SESSION_ID_TEXT));
        command = new LoginWithSessionIdCommand(authenticatorMock, commandParser);

        String response = command.execute();

        assertEquals(LoginWithSessionIdCommand.getInvalidSessionIdResponse(), response);
    }

    @Test
    public void testLoginCommandCorrectSessionId() throws CommandParseException, AuthenticationException {
        Session expectedSession = new Session(RANDOM_ID);
        when(authenticatorMock.logUserInWithSessionId(any(Session.class)))
                .thenReturn(expectedSession);

        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID));
        command = new LoginWithSessionIdCommand(authenticatorMock, commandParser);

        String response = command.execute();

        assertEquals(LoginWithSessionIdCommand.getSuccessfulLoginResponse(expectedSession), response);
    }
}
