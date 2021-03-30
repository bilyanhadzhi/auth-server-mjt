package bg.sofia.uni.fmi.mjt.authserver;

import bg.sofia.uni.fmi.mjt.authserver.commands.CommandExecutor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class AuthClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8000;
    private static final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private static SocketChannel socketChannel;

    public static void main(String[] args) {
        start();
    }

    public static void start() {
        Scanner scanner = new Scanner(System.in);

        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            while (true) {
                String command = scanner.nextLine();

                String response = sendCommand(command);
                System.out.print(response);

                if (command.equals(CommandExecutor.COMMAND_EXIT)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("There is a problem with the network communication");
            e.printStackTrace();
        }
    }

    private static String sendCommand(String command) throws IOException {
        if (socketChannel == null) {
            throw new IllegalStateException("Cannot communicate: no connection established");
        }

        buffer.clear();
        buffer.put(command.getBytes());
        buffer.put(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        socketChannel.write(buffer);

        buffer.clear();
        socketChannel.read(buffer);
        String response = new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8);

        buffer.flip();

        return response;
    }
}
