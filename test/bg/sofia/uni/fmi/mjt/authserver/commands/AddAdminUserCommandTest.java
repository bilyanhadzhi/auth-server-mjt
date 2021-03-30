package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.AuthAuditLogger;
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

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class AddAdminUserCommandTest {
    private static final String DEFAULT_COMMAND_TEXT =
            "add-admin-user --session-id %s --username %s";

    private static final String DEFAULT_USERNAME = "user1";
    private static final String SECOND_USERNAME = "user2";

    private static final UUID RANDOM_ID = UUID.randomUUID();
    private static final String INVALID_SESSION_ID_TEXT = "1234";

    private CommandParser commandParser;
    private AddAdminUserCommand command;
    private SocketChannel socketChannel;

    @Mock
    private Authenticator authenticatorMock;

    @Mock
    private AuthAuditLogger auditLoggerMock;

    @Mock
    private User userMock;

    @Mock
    private User secondUserMock;

    @Before
    public void setUpCommand() throws CommandParseException, IOException {
        initMocks(this);

        socketChannel = SocketChannel.open();

        commandParser = CommandParser.parse(DEFAULT_COMMAND_TEXT);
        command = new AddAdminUserCommand(authenticatorMock, commandParser,
                auditLoggerMock, socketChannel);
    }

    @Test
    public void testAddAdminUserCommandInvalidSessionId() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, INVALID_SESSION_ID_TEXT,
                SECOND_USERNAME));
        command = new AddAdminUserCommand(authenticatorMock, commandParser, auditLoggerMock, socketChannel);

        String response = command.execute();
        assertEquals(RegisterCommand.getInvalidSessionIdResponse(), response);
    }

    @Test
    public void testAddAdminUserCommandUserWhoRequestedDoesNotExist() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                SECOND_USERNAME));
        command = new AddAdminUserCommand(authenticatorMock, commandParser, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserByUsername(any(String.class)))
                .thenReturn(null);

        String response = command.execute();
        assertEquals(RegisterCommand.getFailedSessionLoginResponse(), response);
    }

    @Test
    public void testAddAdminUserCommandUserWhoRequestedIsNotAuthorized() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                SECOND_USERNAME));
        command = new AddAdminUserCommand(authenticatorMock, commandParser, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);

        when(userMock.getUsername())
                .thenReturn(DEFAULT_USERNAME);

        when(userMock.getAuthority())
                .thenReturn(Authority.USER);

        String response = command.execute();
        assertEquals(AddAdminUserCommand.getUnauthorizedResponse(DEFAULT_USERNAME), response);
    }

    @Test
    public void testAddAdminUserCommandUserToAddDoesNotExist() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                SECOND_USERNAME));
        command = new AddAdminUserCommand(authenticatorMock, commandParser, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);

        when(userMock.getUsername())
                .thenReturn(DEFAULT_USERNAME);

        when(userMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(authenticatorMock.getUserByUsername(SECOND_USERNAME))
                .thenReturn(null);

        String response = command.execute();
        assertEquals(AddAdminUserCommand.getUserToAddNotFoundResponse(SECOND_USERNAME), response);
    }

    @Test
    public void testAddAdminUserCommandUserToAddIsAlreadyAdmin() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                SECOND_USERNAME));
        command = new AddAdminUserCommand(authenticatorMock, commandParser, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);

        when(userMock.getUsername())
                .thenReturn(DEFAULT_USERNAME);

        when(userMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(authenticatorMock.getUserByUsername(SECOND_USERNAME))
                .thenReturn(userMock);

        String response = command.execute();
        assertEquals(AddAdminUserCommand.getUserIsAlreadyAdminResponse(), response);
    }

    @Test
    public void testAddAdminUserCommandSuccess() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                SECOND_USERNAME));
        command = new AddAdminUserCommand(authenticatorMock, commandParser, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(userMock);

        when(userMock.getUsername())
                .thenReturn(DEFAULT_USERNAME);

        when(userMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(authenticatorMock.getUserByUsername(SECOND_USERNAME))
                .thenReturn(secondUserMock);

        when(secondUserMock.getAuthority())
                .thenReturn(Authority.USER);

        String response = command.execute();
        assertEquals(AddAdminUserCommand.getSuccessfulAdminAddResponse(), response);
    }
}
