package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.DefaultAuthAuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;

import java.nio.channels.SocketChannel;

public class CommandExecutor {
    public static final String COMMAND_REGISTER = "register";
    public static final String COMMAND_LOGIN = "login";
    public static final String COMMAND_UPDATE_USER = "update-user";
    public static final String COMMAND_RESET_PASSWORD = "reset-password";
    public static final String COMMAND_LOGOUT = "logout";
    public static final String COMMAND_ADD_ADMIN_USER = "add-admin-user";
    public static final String COMMAND_REMOVE_ADMIN_USER = "remove-admin-user";
    public static final String COMMAND_DELETE_USER = "delete-user";
    public static final String COMMAND_EXIT = "exit";

    private final Authenticator authenticator;

    public CommandExecutor(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public String executeCommand(String command, SocketChannel socketChannel) {
        String response;
        CommandParser parser;

        try {
            parser = CommandParser.parse(command);
        } catch (CommandParseException exception) {
            response = exception.getMessage();
            return response;
        }

        String commandName = parser.getCommandName();

        switch (commandName) {
            case COMMAND_REGISTER -> {
                Command registerCommand = new RegisterCommand(authenticator, parser);
                response = registerCommand.execute();
            }
            case COMMAND_LOGIN -> {
                Command loginCommand = new LoginCommand(authenticator, parser,
                        new DefaultAuthAuditLogger(authenticator.getConfiguration().getAuditLogPath()),
                        socketChannel);
                response = loginCommand.execute();
            }
            case COMMAND_UPDATE_USER -> {
                Command updateUserCommand = new UpdateUserCommand(authenticator, parser);
                response = updateUserCommand.execute();
            }
            case COMMAND_RESET_PASSWORD -> {
                Command resetPasswordCommand = new ResetPasswordCommand(authenticator, parser);
                response = resetPasswordCommand.execute();
            }
            case COMMAND_LOGOUT -> {
                Command logoutCommand = new LogoutCommand(authenticator, parser);
                response = logoutCommand.execute();
            }
            case COMMAND_ADD_ADMIN_USER -> {
                Command addAdminUserCommand = new AddAdminUserCommand(authenticator, parser,
                        new DefaultAuthAuditLogger(authenticator.getConfiguration().getAuditLogPath()),
                        socketChannel);
                response = addAdminUserCommand.execute();
            }
            case COMMAND_REMOVE_ADMIN_USER -> {
                Command removeAdminUserCommand = new RemoveAdminUserCommand(authenticator, parser,
                        authenticator.getConfiguration(),
                        new DefaultAuthAuditLogger(authenticator.getConfiguration().getAuditLogPath()),
                        socketChannel);
                response = removeAdminUserCommand.execute();
            }
            case COMMAND_DELETE_USER -> {
                Command deleteUserCommand = new DeleteUserCommand(authenticator, parser);
                response = deleteUserCommand.execute();
            }
            case COMMAND_EXIT -> response = getExitResponse();
            default -> response = getUnknownCommandResponse();
        }

        return response;
    }

    private String getExitResponse() {
        return "Have a good day! :)";
    }

    private String getUnknownCommandResponse() {
        return "Unknown command";
    }
}
