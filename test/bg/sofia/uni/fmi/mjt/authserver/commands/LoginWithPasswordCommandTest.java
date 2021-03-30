package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.AuthAuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.FailedLoginException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class LoginWithPasswordCommandTest {
    private static final String DEFAULT_COMMAND_TEXT =
            "login --username name --password pass";

    private static final String NULL_TEXT = "null";

    private CommandParser commandParser;
    private LoginWithPasswordCommand command;
    private SocketChannel socketChannel;

    @Mock
    private Authenticator authenticatorMock;

    @Mock
    private AuthAuditLogger auditLoggerMock;

    @Before
    public void setUpCommand() throws CommandParseException, IOException {
        initMocks(this);

        socketChannel = SocketChannel.open();

        commandParser = CommandParser.parse(DEFAULT_COMMAND_TEXT);
        command = new LoginWithPasswordCommand(authenticatorMock, commandParser,
                auditLoggerMock, socketChannel);
    }

    @After
    public void closeSocketChannel() throws IOException {
        socketChannel.close();
    }

    @Test
    public void testLoginCommandWrongCredentials() throws AuthenticationException {
        doThrow(FailedLoginException.class)
                .when(authenticatorMock)
                .logUserInWithPassword(any(String.class), any(String.class));

        String response = command.execute();


        assertEquals(LoginWithPasswordCommand.getFailedLoginResponse(
                new AuthenticationException(NULL_TEXT)), response);
    }

    @Test
    public void testLoginCommandCorrectCredentials() throws AuthenticationException {
        Session session = new Session(UUID.randomUUID());

        doReturn(session)
                .when(authenticatorMock)
                .logUserInWithPassword(any(String.class), any(String.class));

        String response = command.execute();

        assertEquals(LoginWithPasswordCommand.getSuccessfulLoginResponse(session), response);
    }
}
