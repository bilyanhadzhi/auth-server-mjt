package bg.sofia.uni.fmi.mjt.authserver.commands;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.AuthAuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class LoginCommand extends AbstractCommand {
    protected final AuthAuditLogger auditLogger;
    protected final SocketChannel socketChannel;

    public LoginCommand(Authenticator authenticator, CommandParser parser,
                        AuthAuditLogger auditLogger, SocketChannel socketChannel) {
        super(authenticator, parser);
        this.auditLogger = auditLogger;
        this.socketChannel = socketChannel;
        setParameters();
    }

    private void setParameters() {
        parameters.add(new CommandParameter("username", false));
        parameters.add(new CommandParameter("password", false));
        parameters.add(new CommandParameter("session-id", false));
    }

    @Override
    public String execute() {
        Map<String, String> argumentPairs = parser.getArguments();

        if (argumentPairs.containsKey("--username") || argumentPairs.containsKey("--password")) {
            return new LoginWithPasswordCommand(authenticator, parser, auditLogger, socketChannel)
                    .execute();
        }

        if (argumentPairs.containsKey("--session-id")) {
            return new LoginWithSessionIdCommand(authenticator, parser).execute();
        }

        return getLoginCommandUsageResponse();
    }

    private String getLoginCommandUsageResponse() {
        return "Invalid usage of command";
    }
}
