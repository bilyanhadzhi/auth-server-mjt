package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.AuthenticationException;

import java.util.Optional;
import java.util.UUID;

public class LoginWithSessionIdCommand extends LoginCommand {
    public LoginWithSessionIdCommand(Authenticator authenticator, CommandParser parser) {
        super(authenticator, parser, null, null);
        setParameters();
    }

    private void setParameters() {
        parameters.add(new CommandParameter("session-id", true));
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

        Session oldSession = new Session(uuid);
        Session newSession;

        try {
            newSession = authenticator.logUserInWithSessionId(oldSession);
        } catch (AuthenticationException exception) {
            return exception.getMessage();
        }

        return getSuccessfulLoginResponse(newSession);
    }

    public static String getSuccessfulLoginResponse(Session session) {
        return "Logged in successfully, new session id is:"
                + System.lineSeparator() + session.getId();
    }
}
