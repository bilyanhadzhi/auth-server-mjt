package bg.sofia.uni.fmi.mjt.authserver;

import bg.sofia.uni.fmi.mjt.authserver.auth.AuthSessionManager;
import bg.sofia.uni.fmi.mjt.authserver.auth.Authenticator;
import bg.sofia.uni.fmi.mjt.authserver.auth.TSVFileSessionManager;
import bg.sofia.uni.fmi.mjt.authserver.commands.CommandExecutor;
import bg.sofia.uni.fmi.mjt.authserver.commands.RegisterCommand;
import bg.sofia.uni.fmi.mjt.authserver.commands.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.authserver.config.AuthConfiguration;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.CommandParseException;
import bg.sofia.uni.fmi.mjt.authserver.storage.AuthStorage;
import bg.sofia.uni.fmi.mjt.authserver.storage.TSVFileAuthStorage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class AuthServer implements Runnable {
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private static final int MIN_ADMIN_COUNT = 1;
    private static final int MAX_LOGIN_FAIL_ATTEMPTS = 3;
    private static final int LOCK_TIMEOUT_MINUTES = 15;
    private static final Path USERS_DATABASE_PATH = Path.of("users.tsv");
    private static final Path SESSIONS_DATABASE_PATH = Path.of("sessions.tsv");
    private static final Path AUDIT_LOG_PATH = Path.of("audit.log");

    private static final String NO_ADMIN_USERS_MSG =
            "In order to proceed you must add an administrator user."
            + System.lineSeparator() + "Do it via the register command.";

    private final CommandExecutor commandExecutor;
    private final int serverPort;
    private Selector selector;
    private boolean shouldListen;

    public AuthServer(int port) {
        serverPort = port;
        shouldListen = false;

        AuthConfiguration configuration = AuthConfiguration.builder(USERS_DATABASE_PATH,
                SESSIONS_DATABASE_PATH, AUDIT_LOG_PATH)
                .setLockTimeout(LOCK_TIMEOUT_MINUTES * 60)
                .setMaxLoginAttemptFailures(MAX_LOGIN_FAIL_ATTEMPTS)
                .setMinimumAdminCount(MIN_ADMIN_COUNT)
                .build();

        AuthStorage storage = new TSVFileAuthStorage(configuration.getUsersDatabasePath());
        AuthSessionManager sessionManager = new TSVFileSessionManager(configuration, storage);

        Authenticator authenticator = new Authenticator(configuration, storage, sessionManager);

        while (storage.getAdminCount() < MIN_ADMIN_COUNT) {
            addInitialAdmin(authenticator);
        }

        commandExecutor = new CommandExecutor(authenticator);
    }

    private void addInitialAdmin(Authenticator authenticator) {
        boolean finished = false;

        System.out.println(NO_ADMIN_USERS_MSG);
        Scanner scanner = new Scanner(System.in);

        CommandParser parser;
        while (!finished) {
            String input = scanner.nextLine();

            try {
                parser = CommandParser.parse(input);
            } catch (CommandParseException exception) {
                System.out.println(exception.getMessage());
                continue;
            }

            RegisterCommand registerCommand = new RegisterCommand(authenticator, parser, true);
            String response = registerCommand.execute();

            if (response.equals(registerCommand.getSuccessfulRegisterResponse())) {
                finished = true;
            }
            System.out.println(response);
        }
    }

    @Override
    public void run() {
        start();
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, serverPort));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            shouldListen = true;

            while (true) {
                int readyChannels = selector.select();

                if (readyChannels == 0) {
                    if (!shouldListen) {
                        break;
                    }

                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        buffer.clear();
                        int r;
                        try {
                            r = socketChannel.read(buffer);
                        } catch (IOException exception) {
                            System.out.println("Could not read from client:");
                            System.out.println(exception.getMessage());
                            socketChannel.close();
                            continue;
                        }

                        if (r < 0) {
                            socketChannel.close();
                            continue;
                        }

                        String command = new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8);

                        buffer.clear();

                        String response = commandExecutor.executeCommand(command, socketChannel);

                        buffer.put(response.getBytes());
                        buffer.put(System.lineSeparator().getBytes());
                        buffer.flip();

                        socketChannel.write(buffer);
                    } else if (key.isAcceptable()) {
                        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();

                        SocketChannel accept = socketChannel.accept();
                        accept.configureBlocking(false);
                        accept.register(selector, SelectionKey.OP_READ);
                    }

                    keyIterator.remove();
                }
            }

        } catch (IOException e) {
            System.out.println("There was a problem with the server socket");
            e.printStackTrace();
        }
    }

    public void stop() {
        shouldListen = false;
        selector.wakeup();
    }
}
