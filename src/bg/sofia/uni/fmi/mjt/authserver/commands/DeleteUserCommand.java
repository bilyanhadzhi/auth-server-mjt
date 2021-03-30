package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.user.Authority;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.util.Optional;
import java.util.UUID;

public class DeleteUserCommand extends AbstractCommand {
    public DeleteUserCommand(Authenticator authenticator, CommandParser parser) {
        super(authenticator, parser);
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

        if (!userWhoRequested.getAuthority().isAtLeast(Authority.ADMIN)) {
            return getUnauthorizedResponse(userWhoRequested.getUsername());
        }

        User userToRemove = authenticator.getUserByUsername(username);
        if (userToRemove == null) {
            return getUserToDeleteNotFoundResponse(username);
        }

        authenticator.deleteUser(userToRemove.getUsername());
        return getSuccessfulUserDeletionResponse();
    }

    public static String getSuccessfulUserDeletionResponse() {
        return "User deleted successfully";
    }

    public static String getUserToDeleteNotFoundResponse(String username) {
        return "User " + username + " was not found";
    }

    public static String getUnauthorizedResponse(String username) {
        return "User " + username + " is not authorized";
    }
}
