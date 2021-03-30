import bg.sofia.uni.fmi.mjt.authserver.AuthServer;

public class Main {

    public static void main(String[] args) {
        AuthServer server = new AuthServer(8000);
        Thread serverThread = new Thread(server);

        serverThread.start();
    }
}
