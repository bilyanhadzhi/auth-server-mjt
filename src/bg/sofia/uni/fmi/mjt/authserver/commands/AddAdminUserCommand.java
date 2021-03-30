package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.AuthAuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.BeginResourceChangeEvent;
import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.EndResourceChangeEvent;
import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.ResourceChangeEventType;
import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.user.Authority;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class AddAdminUserCommand extends AbstractCommand {
    protected final AuthAuditLogger auditLogger;
    protected final SocketChannel socketChannel;

    public AddAdminUserCommand(Authenticator authenticator, CommandParser parser,
                               AuthAuditLogger auditLogger, SocketChannel socketChannel) {
        super(authenticator, parser);
        this.auditLogger = auditLogger;
        this.socketChannel = socketChannel;
        setParameters();
    }

    private void setParameters() {
        parameters.add(new CommandParameter("session-id"));
        parameters.add(new CommandParameter("username"));
    }

    @Override
    public String execute() {
        Optional<String> argumentValidationError = validateArgumentsAndGetErrorMessage();

        if (argumentValidationError.isPresent()) {
            return argumentValidationError.get();
        }

        String sessionId = parser.getArgumentValue("session-id");
        String username = parser.getArgumentValue("username");

        UUID uuid;
        try {
            uuid = UUID.fromString(sessionId);
        } catch (IllegalArgumentException exception) {
            return getInvalidSessionIdResponse();
        }

        Session session = new Session(uuid);

        User userWhoRequested = authenticator.getUserBySession(session);
        if (userWhoRequested == null) {
            return getFailedSessionLoginResponse();
        }

        SocketAddress performerAddress;
        try {
            performerAddress = socketChannel.getRemoteAddress();
        } catch (IOException exception) {
            throw new RuntimeException("There was an error while trying to read IP of performer", exception);
        }

        var beginEvent = new BeginResourceChangeEvent(LocalDateTime.now(), userWhoRequested.getUsername(),
                performerAddress, username, ResourceChangeEventType.ADD_ADMIN);
        auditLogger.logEvent(beginEvent);

        if (!userWhoRequested.getAuthority().isAtLeast(Authority.ADMIN)) {
            auditLogger.logEvent(new EndResourceChangeEvent(LocalDateTime.now(), beginEvent, false));
            return getUnauthorizedResponse(userWhoRequested.getUsername());
        }

        User userToAdd = authenticator.getUserByUsername(username);
        if (userToAdd == null) {
            auditLogger.logEvent(new EndResourceChangeEvent(LocalDateTime.now(), beginEvent, false));
            return getUserToAddNotFoundResponse(username);
        }

        if (userToAdd.getAuthority().isAtLeast(Authority.ADMIN)) {
            auditLogger.logEvent(new EndResourceChangeEvent(LocalDateTime.now(), beginEvent, false));
            return getUserIsAlreadyAdminResponse();
        }

        userToAdd.setAuthority(Authority.ADMIN);
        authenticator.replaceUser(userToAdd.getUsername(), userToAdd);

        auditLogger.logEvent(new EndResourceChangeEvent(LocalDateTime.now(), beginEvent, true));
        return getSuccessfulAdminAddResponse();
    }

    public static String getUserIsAlreadyAdminResponse() {
        return "User is already admin, nothing has changed";
    }

    public static String getSuccessfulAdminAddResponse() {
        return "User was set as admin successfully";
    }

    public static String getUserToAddNotFoundResponse(String username) {
        return "User " + username + " was not found";
    }

    public static String getUnauthorizedResponse(String username) {
        return "User " + username + " is not authorized";
    }
}
