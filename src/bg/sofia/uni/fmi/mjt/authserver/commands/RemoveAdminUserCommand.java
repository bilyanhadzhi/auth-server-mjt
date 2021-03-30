package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.AuthAuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.BeginResourceChangeEvent;
import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.EndResourceChangeEvent;
import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.ResourceChangeEventType;
import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.config.AuthConfiguration;
import bg.sofia.uni.fmi.mjt.authserver.user.Authority;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class RemoveAdminUserCommand extends AbstractCommand {
    private final AuthConfiguration configuration;
    protected final AuthAuditLogger auditLogger;
    protected final SocketChannel socketChannel;

    public RemoveAdminUserCommand(Authenticator authenticator, CommandParser parser,
                                  AuthConfiguration configuration,
                                  AuthAuditLogger auditLogger, SocketChannel socketChannel) {
        super(authenticator, parser);
        this.configuration = configuration;
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
                performerAddress, username, ResourceChangeEventType.REMOVE_ADMIN);
        auditLogger.logEvent(beginEvent);

        if (!userWhoRequested.getAuthority().isAtLeast(Authority.ADMIN)) {
            auditLogger.logEvent(new EndResourceChangeEvent(LocalDateTime.now(), beginEvent, false));
            return getUnauthorizedResponse(userWhoRequested.getUsername());
        }

        User userToRemove = authenticator.getUserByUsername(username);
        if (userToRemove == null) {
            auditLogger.logEvent(new EndResourceChangeEvent(LocalDateTime.now(), beginEvent, false));
            return getUserToRemoveNotFoundResponse(username);
        }

        if (!userToRemove.getAuthority().isAtLeast(Authority.ADMIN)) {
            auditLogger.logEvent(new EndResourceChangeEvent(LocalDateTime.now(), beginEvent, false));
            return getUserIsNotAdminResponse();
        }

        if (authenticator.getAdminCount() <= configuration.getMinimumAdminCount()) {
            auditLogger.logEvent(new EndResourceChangeEvent(LocalDateTime.now(), beginEvent, false));
            return getAdminCountTooLowResponse();
        }

        userToRemove.setAuthority(Authority.USER);
        authenticator.replaceUser(userToRemove.getUsername(), userToRemove);

        auditLogger.logEvent(new EndResourceChangeEvent(LocalDateTime.now(), beginEvent, true));
        return getSuccessfulAdminRemoveResponse();
    }

    public String getAdminCountTooLowResponse() {
        return "Cannot remove privileges of user, there must be at least "
                + configuration.getMinimumAdminCount() + " admins on the platform";

    }

    public static String getUserIsNotAdminResponse() {
        return "User is not admin, nothing has changed";
    }

    public static String getSuccessfulAdminRemoveResponse() {
        return "User's admin privileges were removed successfully";
    }

    public static String getUserToRemoveNotFoundResponse(String username) {
        return "User " + username + " was not found";
    }

    public static String getUnauthorizedResponse(String username) {
        return "User " + username + " is not authorized";
    }
}
