package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.ValidationException;
import bg.sofia.uni.fmi.mjt.authserver.user.Email;
import bg.sofia.uni.fmi.mjt.authserver.user.Password;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class RegisterCommandTest {
    private static final String DEFAULT_COMMAND_TEXT =
            "register --username name --password pass --first-name name --last-name name --email mail";

    private CommandParser commandParser;
    private RegisterCommand command;

    @Mock
    private Authenticator authenticatorMock;

    @Before
    public void setUpCommand() throws CommandParseException {
        authenticatorMock = mock(Authenticator.class);
        commandParser = CommandParser.parse(DEFAULT_COMMAND_TEXT);
        command = new RegisterCommand(authenticatorMock, commandParser);
    }

    @Test
    public void testRegisterCommandInvalidPassword() throws UserAlreadyExistsException, ValidationException {
        doThrow(new InvalidPasswordException(Collections.emptyList()))
                .when(authenticatorMock)
                .registerUser(any(String.class), any(Password.class), any(String.class),
                        any(String.class), any(Email.class));

        String response = command.execute();

        assertEquals(RegisterCommand.getInvalidPasswordResponse(
                new InvalidPasswordException(Collections.emptyList())),
                response);
    }

    @Test
    public void testRegisterCommandInvalidEmail() throws UserAlreadyExistsException,
            ValidationException {
        doThrow(new InvalidEmailException(Collections.emptyList()))
                .when(authenticatorMock)
                .registerUser(any(String.class), any(Password.class), any(String.class),
                        any(String.class), any(Email.class));

        String response = command.execute();

        assertEquals(RegisterCommand.getInvalidEmailResponse(
                new InvalidEmailException(Collections.emptyList())),
                response);
    }

    @Test
    public void testRegisterCommandUserAlreadyExists() throws UserAlreadyExistsException,
            ValidationException {
        doThrow(UserAlreadyExistsException.class)
                .when(authenticatorMock)
                .registerUser(any(String.class), any(Password.class), any(String.class),
                        any(String.class), any(Email.class));

        String response = command.execute();

        assertEquals(RegisterCommand.getUserAlreadyExistsResponse(), response);
    }

    @Test
    public void testRegisterCommandValid() throws UserAlreadyExistsException,
            ValidationException {

        doNothing()
                .when(authenticatorMock)
                .registerUser(any(String.class), any(Password.class), any(String.class),
                        any(String.class), any(Email.class));

        String response = command.execute();

        assertEquals(RegisterCommand.getSuccessfulRegisterResponse(), response);
    }
}
