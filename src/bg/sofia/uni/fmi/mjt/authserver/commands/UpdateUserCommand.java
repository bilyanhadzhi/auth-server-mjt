package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;
import bg.sofia.uni.fmi.mjt.authserver.user.Email;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import bg.sofia.uni.fmi.mjt.authserver.validation.EmailValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UpdateUserCommand extends AbstractCommand {
    private List<String> validationErrors = new ArrayList<>();

    public UpdateUserCommand(Authenticator authenticator, CommandParser parser) {
        super(authenticator, parser);
        setParameters();
    }

    private void setParameters() {
        parameters.add(new CommandParameter("session-id"));
        parameters.add(new CommandParameter("new-username", false));
        parameters.add(new CommandParameter("new-first-name", false));
        parameters.add(new CommandParameter("new-last-name", false));
        parameters.add(new CommandParameter("new-email", false));
    }

    @Override
    public String execute() {
        Optional<String> argumentValidationError = validateArgumentsAndGetErrorMessage();

        if (argumentValidationError.isPresent()) {
            return argumentValidationError.get();
        }

        String sessionId = parser.getArgumentValue("session-id");
        String newUsername = parser.getArgumentValue("new-username");
        String newFirstName = parser.getArgumentValue("new-first-name");
        String newLastName = parser.getArgumentValue("new-last-name");
        String newEmail = parser.getArgumentValue("new-email");

        UUID uuid;
        try {
            uuid = UUID.fromString(sessionId);
        } catch (IllegalArgumentException exception) {
            return getInvalidSessionIdResponse();
        }

        Session sessionToSearchBy = new Session(uuid);
        User foundUser = authenticator.getUserBySession(sessionToSearchBy);

        if (foundUser == null) {
            return getFailedSessionLoginResponse();
        }

        String oldUsername = foundUser.getUsername();

        if (newUsername != null && authenticator.getUserByUsername(newUsername) != null) {
            validationErrors.add(getUserAlreadyExistsResponse());
        }

        Email newMail = new Email(newEmail, new EmailValidator());
        if (newEmail != null) {
            try {
                newMail.validate();
            } catch (ValidationException exception) {
                validationErrors.add(exception.getMessage());
            }
        }

        if (!validationErrors.isEmpty()) {
            return getUnsuccessfulUpdateResponse();
        }

        if (newUsername != null) {
            foundUser.setUsername(newUsername);
        }
        if (newFirstName != null) {
            foundUser.setFirstName(newFirstName);
        }
        if (newLastName != null) {
            foundUser.setLastName(newLastName);
        }
        if (newEmail != null) {
            foundUser.setEmail(newMail);
        }

        authenticator.replaceUser(oldUsername, foundUser);
        return getSuccessfulUpdateResponse();
    }

    public static String getSuccessfulUpdateResponse() {
        return "User updated successfully";
    }

    public String getUnsuccessfulUpdateResponse() {
        return "Did not update user, issues: " + System.lineSeparator() + validationErrors;
    }

    public static String getUserAlreadyExistsResponse() {
        return "Username is already taken";
    }

    public List<String> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }
}
