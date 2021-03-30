package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.AuthAuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.config.AuthConfiguration;
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
public class RemoveAdminUserCommandTest {
    private static final String DEFAULT_COMMAND_TEXT =
            "remove-admin-user --session-id %s --username %s";

    private static final String DEFAULT_USERNAME = "user1";

    private static final long DEFAULT_ADMIN_COUNT = 1;

    private static final UUID RANDOM_ID = UUID.randomUUID();
    private static final String INVALID_SESSION_ID_TEXT = "1234";

    private CommandParser commandParser;
    private RemoveAdminUserCommand command;
    private SocketChannel socketChannel;

    @Mock
    private Authenticator authenticatorMock;

    @Mock
    private AuthAuditLogger auditLoggerMock;

    @Mock
    private AuthConfiguration configurationMock;

    @Mock
    private User requestUserMock;

    @Mock
    private User userToBeRemovedMock;

    @Before
    public void setUpCommand() throws CommandParseException, IOException {
        initMocks(this);

        socketChannel = SocketChannel.open();

        commandParser = CommandParser.parse(DEFAULT_COMMAND_TEXT);
        command = new RemoveAdminUserCommand(authenticatorMock, commandParser,
                configurationMock, auditLoggerMock, socketChannel);
    }

    @Test
    public void testRemoveAdminUserCommandInvalidSessionId() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, INVALID_SESSION_ID_TEXT,
                DEFAULT_USERNAME));
        command = new RemoveAdminUserCommand(authenticatorMock, commandParser, configurationMock, auditLoggerMock, socketChannel);

        String response = command.execute();

        assertEquals(RemoveAdminUserCommand.getInvalidSessionIdResponse(), response);
    }

    @Test
    public void testRemoveAdminUserCommandNonExistingUserWhoRequests() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                DEFAULT_USERNAME));
        command = new RemoveAdminUserCommand(authenticatorMock, commandParser, configurationMock, auditLoggerMock, socketChannel);

        String response = command.execute();

        assertEquals(RemoveAdminUserCommand.getFailedSessionLoginResponse(), response);
    }

    @Test
    public void testRemoveAdminUserCommandRequesterHasNoAuthority() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                DEFAULT_USERNAME));
        command = new RemoveAdminUserCommand(authenticatorMock, commandParser, configurationMock, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(requestUserMock);

        when(requestUserMock.getAuthority())
                .thenReturn(Authority.USER);

        String response = command.execute();

        assertEquals(RemoveAdminUserCommand.getUnauthorizedResponse(null), response);
    }

    @Test
    public void testRemoveAdminUserCommandUserToRemoveDoesNotExist()
            throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                DEFAULT_USERNAME));
        command = new RemoveAdminUserCommand(authenticatorMock, commandParser, configurationMock, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(requestUserMock);

        when(requestUserMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(authenticatorMock.getUserByUsername(any(String.class)))
                .thenReturn(null);

        String response = command.execute();

        assertEquals(RemoveAdminUserCommand.getUserToRemoveNotFoundResponse(DEFAULT_USERNAME), response);
    }

    @Test
    public void testRemoveAdminUserCommandUserToRemoveIsNotAdmin() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                DEFAULT_USERNAME));
        command = new RemoveAdminUserCommand(authenticatorMock, commandParser, configurationMock, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(requestUserMock);

        when(requestUserMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(authenticatorMock.getUserByUsername(any(String.class)))
                .thenReturn(userToBeRemovedMock);

        when(userToBeRemovedMock.getAuthority())
                .thenReturn(Authority.USER);

        String response = command.execute();

        assertEquals(RemoveAdminUserCommand.getUserIsNotAdminResponse(), response);
    }

    @Test
    public void testRemoveAdminUserCommandNotEnoughAdmins() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                DEFAULT_USERNAME));
        command = new RemoveAdminUserCommand(authenticatorMock, commandParser, configurationMock, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(requestUserMock);

        when(requestUserMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(authenticatorMock.getUserByUsername(any(String.class)))
                .thenReturn(userToBeRemovedMock);

        when(userToBeRemovedMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(configurationMock.getMinimumAdminCount())
                .thenReturn(DEFAULT_ADMIN_COUNT);

        when(authenticatorMock.getAdminCount())
                .thenReturn(DEFAULT_ADMIN_COUNT);

        String response = command.execute();

        assertEquals(command.getAdminCountTooLowResponse(), response);
    }

    @Test
    public void testRemoveAdminUserCommandSuccess() throws CommandParseException {
        commandParser = CommandParser.parse(String.format(DEFAULT_COMMAND_TEXT, RANDOM_ID,
                DEFAULT_USERNAME));
        command = new RemoveAdminUserCommand(authenticatorMock, commandParser, configurationMock, auditLoggerMock, socketChannel);

        when(authenticatorMock.getUserBySession(any(Session.class)))
                .thenReturn(requestUserMock);

        when(requestUserMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(authenticatorMock.getUserByUsername(any(String.class)))
                .thenReturn(userToBeRemovedMock);

        when(userToBeRemovedMock.getAuthority())
                .thenReturn(Authority.ADMIN);

        when(configurationMock.getMinimumAdminCount())
                .thenReturn(DEFAULT_ADMIN_COUNT);

        when(authenticatorMock.getAdminCount())
                .thenReturn(DEFAULT_ADMIN_COUNT + 1);

        String response = command.execute();

        assertEquals(RemoveAdminUserCommand.getSuccessfulAdminRemoveResponse(), response);
    }
}
