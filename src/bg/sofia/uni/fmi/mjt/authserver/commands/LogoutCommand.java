package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.AuthenticationException;

import java.util.Optional;
import java.util.UUID;

public class LogoutCommand extends AbstractCommand {
    public LogoutCommand(Authenticator authenticator, CommandParser parser) {
        super(authenticator, parser);
        setParameters();
    }

    private void setParameters() {
        parameters.add(new CommandParameter("session-id"));
    }

    @Override
    public String execute() {
        Optional<String> argumentValidationError = validateArgumentsAndGetErrorMessage();

        if (argumentValidationError.isPresent()) {
            return argumentValidationError.get();
        }

        String sessionId = parser.getArgumentValue("session-id");

        UUID uuid;
        try {
            uuid = UUID.fromString(sessionId);
        } catch (IllegalArgumentException exception) {
            return getInvalidSessionIdResponse();
        }

        Session session = new Session(uuid);

        try {
            authenticator.logUserOut(session);
        } catch (AuthenticationException exception) {
            return getFailedSessionLoginResponse();
        }

        return getSuccessfulLogoutResponse();
    }

    public static String getSuccessfulLogoutResponse() {
        return "User logged out successfully";
    }
}
