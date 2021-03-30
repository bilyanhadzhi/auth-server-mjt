package bg.sofia.uni.fmi.mjt.authserver.tasks;

import bg.sofia.uni.fmi.mjt.authserver.auth.AuthSessionManager;
import bg.sofia.uni.fmi.mjt.authserver.auth.Session;

public class InvalidateSessionTask implements Runnable {
    private final AuthSessionManager sessionManager;
    private final Session session;

    public InvalidateSessionTask(AuthSessionManager sessionManager, Session session) {
        this.sessionManager = sessionManager;
        this.session = session;
    }

    @Override
    public void run() {
        sessionManager.invalidateSession(session);
    }
}
