package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.MissingRequiredArgumentsException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.UnknownArgumentException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractCommand implements Command {
    protected Authenticator authenticator;
    protected CommandParser parser;
    protected Set<CommandParameter> parameters;

    public AbstractCommand(Authenticator authenticator, CommandParser parser) {
        this.authenticator = authenticator;
        this.parser = parser;
        this.parameters = new HashSet<>();
    }

    public abstract String execute();

    protected void validate() throws CommandParseException {
        parser.requireAllArgumentNamesAreValidParameterNames(parameters);
        parser.requireAllRequiredArgumentsArePresent(parameters);
    }

    protected Optional<String> validateArgumentsAndGetErrorMessage() {
        try {
            validate();
        } catch (UnknownArgumentException exception) {
            return Optional.of(getNonExistingParameterResponse(exception.getParameters()));
        } catch (MissingRequiredArgumentsException exception) {
            return Optional.of(getMissingRequiredArgumentsResponse(exception.getMissingArguments()));
        } catch (CommandParseException exception) {
            return Optional.of(getGeneralPreExecutionFailureResponse());
        }

        return Optional.empty();
    }

    public String getNonExistingParameterResponse(Set<String> parameters) {
        return "Could not execute command, unknown parameters: "
                + System.lineSeparator() + parameters;
    }

    public String getMissingRequiredArgumentsResponse(Set<String> missingArguments) {
        return "Could not execute command, the following required arguments were not supplied: "
                + System.lineSeparator() + missingArguments;
    }

    public static String getInvalidSessionIdResponse() {
        return "Session id is not a valid UUID";
    }

    public static String getFailedSessionLoginResponse() {
        return "Session id does not correspond to any users";
    }

    public static String getGeneralPreExecutionFailureResponse() {
        return "Could not execute command";
    }

    public static String getGeneralValidationErrorResponse(ValidationException exception) {
        return "There was a validation error: " + System.lineSeparator() + exception.getMessage();
    }
}
