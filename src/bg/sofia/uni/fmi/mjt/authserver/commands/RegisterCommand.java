package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;
import bg.sofia.uni.fmi.mjt.authserver.user.Authority;
import bg.sofia.uni.fmi.mjt.authserver.user.Email;
import bg.sofia.uni.fmi.mjt.authserver.user.Password;
import bg.sofia.uni.fmi.mjt.authserver.validation.EmailValidator;
import bg.sofia.uni.fmi.mjt.authserver.validation.PasswordValidator;

import java.util.Optional;

public class RegisterCommand extends AbstractCommand {
    private final boolean asAdmin;

    public RegisterCommand(Authenticator authenticator, CommandParser parser) {
        this(authenticator, parser, false);
    }

    public RegisterCommand(Authenticator authenticator, CommandParser parser, boolean asAdmin) {
        super(authenticator, parser);
        setParameters();
        this.asAdmin = asAdmin;
    }

    private void setParameters() {
        parameters.add(new CommandParameter("username", true));
        parameters.add(new CommandParameter("password", true));
        parameters.add(new CommandParameter("first-name", true));
        parameters.add(new CommandParameter("last-name", true));
        parameters.add(new CommandParameter("email", true));
    }

    @Override
    public String execute() {
        Optional<String> argumentValidationError = validateArgumentsAndGetErrorMessage();

        if (argumentValidationError.isPresent()) {
            return argumentValidationError.get();
        }

        String username = parser.getArgumentValue("username");
        String password = parser.getArgumentValue("password");
        String firstName = parser.getArgumentValue("first-name");
        String lastName = parser.getArgumentValue("last-name");
        String email = parser.getArgumentValue("email");

        Password userPassword = new Password(password, new PasswordValidator());
        Email userEmail = new Email(email, new EmailValidator());

        try {
            if (asAdmin) {
                authenticator.registerUser(username, userPassword, firstName, lastName, userEmail, Authority.ADMIN);
            } else {
                authenticator.registerUser(username, userPassword, firstName, lastName, userEmail);
            }
        } catch (UserAlreadyExistsException exception) {
            return getUserAlreadyExistsResponse();
        } catch (InvalidPasswordException exception) {
            return getInvalidPasswordResponse(exception);
        } catch (InvalidEmailException exception) {
            return getInvalidEmailResponse(exception);
        } catch (ValidationException exception) {
            return getGeneralValidationErrorResponse(exception);
        }

        return getSuccessfulRegisterResponse();
    }

    public static String getGeneralValidationErrorResponse(ValidationException exception) {
        return "There was a validation error: " + exception.getMessage();
    }

    public static String getSuccessfulRegisterResponse() {
        return "User successfully registered";
    }

    public static String getInvalidPasswordResponse(InvalidPasswordException exception) {
        return "Could not register:"
                + System.lineSeparator() + exception.getMessage();
    }

    public static String getInvalidEmailResponse(InvalidEmailException exception) {
        return "Could not register:"
                + System.lineSeparator() + exception.getMessage();
    }

    public static String getUserAlreadyExistsResponse() {
        return "Could not register: username is already taken";
    }
}
