package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;
import bg.sofia.uni.fmi.mjt.authserver.user.Password;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import bg.sofia.uni.fmi.mjt.authserver.validation.PasswordValidator;

import java.util.Optional;
import java.util.UUID;

public class ResetPasswordCommand extends AbstractCommand {
    public ResetPasswordCommand(Authenticator authenticator, CommandParser parser) {
        super(authenticator, parser);
        setParameters();
    }

    private void setParameters() {
        parameters.add(new CommandParameter("session-id"));
        parameters.add(new CommandParameter("username"));
        parameters.add(new CommandParameter("old-password"));
        parameters.add(new CommandParameter("new-password"));
    }

    @Override
    public String execute() {
        Optional<String> argumentValidationError = validateArgumentsAndGetErrorMessage();

        if (argumentValidationError.isPresent()) {
            return argumentValidationError.get();
        }

        String sessionId = parser.getArgumentValue("session-id");
        String username = parser.getArgumentValue("username");
        String oldPassword = parser.getArgumentValue("old-password");
        String newPassword = parser.getArgumentValue("new-password");

        UUID uuid;
        try {
            uuid = UUID.fromString(sessionId);
        } catch (IllegalArgumentException exception) {
            return getInvalidSessionIdResponse();
        }

        User foundUser = authenticator.getUserBySession(new Session(uuid));

        if (foundUser == null) {
            return getFailedSessionLoginResponse();
        }

        if (!username.equals(foundUser.getUsername())) {
            return getUsernameDoesNotMatchResponse();
        }

        if (!foundUser.getPasswordHash().check(oldPassword)) {
            return getPasswordDoesNotMatchResponse();
        }

        Password newPass = new Password(newPassword, new PasswordValidator());

        try {
            newPass.validate();
        } catch (InvalidPasswordException exception) {
            return getInvalidPasswordResponse(exception);
        } catch (ValidationException exception) {
            return getGeneralValidationErrorResponse(exception);
        }

        newPass.hash();
        foundUser.setPasswordHash(newPass);
        authenticator.replaceUser(foundUser.getUsername(), foundUser);

        return getSuccessfulPasswordResetResponse();
    }

    public static String getSuccessfulPasswordResetResponse() {
        return "Password was reset successfully";
    }

    public static String getInvalidPasswordResponse(InvalidPasswordException exception) {
        return "Could not register:" + System.lineSeparator()
                + exception.getMessage();
    }

    public static String getPasswordDoesNotMatchResponse() {
        return "Old password does not match the logged-in users";
    }

    public static String getUsernameDoesNotMatchResponse() {
        return "Given username does not match the logged-in user's";
    }
}
