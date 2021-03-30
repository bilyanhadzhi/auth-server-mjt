package bg.sofia.uni.fmi.mjt.authserver.commands.parser;

import bg.sofia.uni.fmi.mjt.authserver.commands.CommandParameter;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidArgumentNameException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidTokenCountException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.MissingRequiredArgumentsException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.MultipleOccurrencesOfOneArgumentException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.UnknownArgumentException;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class CommandParserTest {
    private static final String COMMAND_INVALID_TOKEN_COUNT = "command --arg1";
    private static final String COMMAND_INVALID_ARGUMENT_NAME = "command arg1 val1";
    private static final String COMMAND_INVALID_MULTIPLE_OCCURRENCES = "command --arg1 val1 --arg1 val2";
    private static final String COMMAND_MISSING_REQUIRED_ARGS = "command --arg2 val2";
    private static final String COMMAND_UNKNOWN_ARGS = "command --arg1 val1 --arg2 val2 --argN --valN";
    private static final String COMMAND_DEFAULT = "command --arg1 val1 --arg2 val2 --arg3 --val3";

    private static final Set<CommandParameter> DEFAULT_PARAMETERS;

    static {
        DEFAULT_PARAMETERS = Set.of(new CommandParameter("arg1", true),
                new CommandParameter("arg2", true),
                new CommandParameter("arg3", false));
    }

    @Test(expected = InvalidTokenCountException.class)
    public void testParseCommandWithInvalidTokenCount() throws CommandParseException {
        CommandParser.parse(COMMAND_INVALID_TOKEN_COUNT);
    }

    @Test(expected = InvalidArgumentNameException.class)
    public void testParseCommandWithInvalidArgumentName() throws CommandParseException {
        CommandParser.parse(COMMAND_INVALID_ARGUMENT_NAME);
    }

    @Test(expected = MultipleOccurrencesOfOneArgumentException.class)
    public void testParseCommandWithMultipleOccurrencesOfArgument() throws CommandParseException {
        CommandParser.parse(COMMAND_INVALID_MULTIPLE_OCCURRENCES);
    }

    @Test
    public void testParsedCommandContainsCorrectArgumentsAndValues() throws CommandParseException {
        CommandParser parser = CommandParser.parse(COMMAND_DEFAULT);

        Map<String, String> expectedArgumentPairs = Map.of("--arg1", "val1",
                "--arg2", "val2",
                "--arg3", "--val3");

        Map<String, String> argumentPairs = parser.getArguments();

        assertEquals(expectedArgumentPairs, argumentPairs);
    }

    @Test
    public void testGetArgumentNameWithoutPrefixWorks() throws CommandParseException {
        CommandParser parser = CommandParser.parse(COMMAND_DEFAULT);

        assertEquals("val1", parser.getArgumentValue("arg1"));
        assertEquals("val2", parser.getArgumentValue("arg2"));
        assertEquals("--val3", parser.getArgumentValue("arg3"));
    }

    @Test(expected = MissingRequiredArgumentsException.class)
    public void testCommandWithMissingRequiredArguments() throws CommandParseException {
        CommandParser parser = CommandParser.parse(COMMAND_MISSING_REQUIRED_ARGS);

        Set<String> missingRequiredArgumentNames = Set.of("--arg1");

        try {
            parser.requireAllRequiredArgumentsArePresent(DEFAULT_PARAMETERS);
        } catch (MissingRequiredArgumentsException exception) {
            assertEquals(missingRequiredArgumentNames, exception.getMissingArguments());
            throw exception;
        }
    }

    @Test(expected = UnknownArgumentException.class)
    public void testCommandWithUnknownArguments() throws CommandParseException {
        CommandParser parser = CommandParser.parse(COMMAND_UNKNOWN_ARGS);

        Set<String> expectedUnknownParameterNames = Set.of("--argN");

        try {
            parser.requireAllArgumentNamesAreValidParameterNames(DEFAULT_PARAMETERS);
        } catch (UnknownArgumentException exception) {
            assertEquals(expectedUnknownParameterNames, exception.getParameters());
            throw exception;
        }
    }
}
