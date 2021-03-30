package bg.sofia.uni.fmi.mjt.authserver.commands.parser;

import bg.sofia.uni.fmi.mjt.authserver.commands.CommandParameter;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidArgumentNameException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidTokenCountException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.MissingRequiredArgumentsException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.MultipleOccurrencesOfOneArgumentException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.UnknownArgumentException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandParser {
    private final String commandName;
    private final Map<String, String> arguments;

    private static final String WHITESPACE_SEPARATOR = "\\s+";
    private static final String ARGUMENT_NAME_PREFIX = "--";

    private CommandParser(String commandName, Map<String, String> arguments) {
        this.commandName = commandName;
        this.arguments = arguments;
    }

    public static CommandParser parse(String commandText) throws CommandParseException {
        String[] tokens = commandText.strip().split(WHITESPACE_SEPARATOR);

        // every argument name has to be followed by a corresponding value,
        // tokens count must be an odd number (command name + pairs of name<->value)
        if (tokens.length % 2 == 0) {
            throw new InvalidTokenCountException("Token count is not odd");
        }

        String commandName = tokens[0];
        Map<String, String> arguments = new HashMap<>();

        // iterate over pairs
        for (int i = 1; i < tokens.length - 1; i += 2) {
            String argumentName = tokens[i];
            String argumentValue = tokens[i + 1];

            if (!isValidArgumentName(argumentName)) {
                throw new InvalidArgumentNameException("Argument with invalid name: [" + argumentName + "]");
            } else if (arguments.containsKey(argumentName)) {
                throw new MultipleOccurrencesOfOneArgumentException("Multiple occurrences of argument value: ["
                        + argumentName + "]");
            }

            arguments.put(argumentName, argumentValue);
        }

        return new CommandParser(commandName, arguments);
    }

    public String getCommandName() {
        return commandName;
    }

    public String getArgumentValue(String argumentName) {
        return arguments.get(ARGUMENT_NAME_PREFIX + argumentName);
    }

    public void requireAllArgumentNamesAreValidParameterNames(Collection<CommandParameter> parameters)
            throws UnknownArgumentException {
        Set<String> argumentNames = arguments.keySet();

        Set<String> parameterNames = parameters.stream()
                .map(CommandParameter::getName)
                .collect(Collectors.toSet());

        if (!parameterNames.containsAll(argumentNames)) {
            argumentNames.removeAll(parameterNames);

            throw new UnknownArgumentException(argumentNames);
        }
    }

    public void requireAllRequiredArgumentsArePresent(Collection<CommandParameter> parameters)
            throws CommandParseException {
        Set<String> argumentNames = arguments.keySet();

        Set<String> requiredParameterNames = parameters.stream()
                .filter(CommandParameter::isRequired)
                .map(CommandParameter::getName)
                .collect(Collectors.toSet());

        if (!argumentNames.containsAll(requiredParameterNames)) {
            requiredParameterNames.removeAll(argumentNames);

            throw new MissingRequiredArgumentsException(requiredParameterNames);
        }
    }

    public Map<String, String> getArguments() {
        return Collections.unmodifiableMap(arguments);
    }

    private static boolean isValidArgumentName(String token) {
        return token.startsWith(ARGUMENT_NAME_PREFIX);
    }
}
