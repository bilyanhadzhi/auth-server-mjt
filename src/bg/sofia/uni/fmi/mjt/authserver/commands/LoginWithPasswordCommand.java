package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.AuthAuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.Event;
import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.FailedLoginEvent;
import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.AuthenticationException;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Optional;

public class LoginWithPasswordCommand extends LoginCommand {
    public LoginWithPasswordCommand(Authenticator authenticator, CommandParser parser,
                                    AuthAuditLogger auditLogger, SocketChannel socketChannel) {
        super(authenticator, parser, auditLogger, socketChannel);
    }

    @Override
    public String execute() {
        Optional<String> argumentValidationError = validateArgumentsAndGetErrorMessage();

        if (argumentValidationError.isPresent()) {
            return argumentValidationError.get();
        }

        String username = parser.getArgumentValue("username");
        String password = parser.getArgumentValue("password");

        Session session;

        SocketAddress socketAddress;
        try {
            socketAddress = socketChannel.getRemoteAddress();
        } catch (IOException exception) {
            throw new RuntimeException("There was an error while fetching socket address", exception);
        }

        try {
            session = authenticator.logUserInWithPassword(username, password);
        } catch (AuthenticationException exception) {
            Event failedLoginEvent = new FailedLoginEvent(LocalDateTime.now(), username, socketAddress);

            auditLogger.logEvent(failedLoginEvent);
            return getFailedLoginResponse(exception);
        }

        return getSuccessfulLoginResponse(session);
    }

    public static String getSuccessfulLoginResponse(Session session) {
        return "Logged in successfully, new session id is: "
                + System.lineSeparator() + session.getId();
    }

    public static String getFailedLoginResponse(AuthenticationException exception) {
        return "Failed login: " + exception.getMessage();
    }
}
