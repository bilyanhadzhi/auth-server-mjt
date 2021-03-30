package bg.sofia.uni.fmi.mjt.authserver.auth;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Session {
    // session length in seconds
    public static final int SESSION_LENGTH = 15 * 60;

    private final UUID id;
    private final String loggedInUsername;
    private final LocalDateTime expiresAt;

    public Session(UUID id, String loggedInUsername, LocalDateTime expiresAt) {
        this.id = id;
        this.loggedInUsername = loggedInUsername;
        this.expiresAt = expiresAt;
    }

    // sentinel session
    public Session(UUID id) {
        this.id = id;
        this.loggedInUsername = null;
        this.expiresAt = null;
    }

    public static Session generateForUsername(String loggedInUsername) {
        Objects.requireNonNull(loggedInUsername, "Trying to create session for null username");

        UUID id = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(SESSION_LENGTH);

        return new Session(id, loggedInUsername, expiresAt);
    }

    public UUID getId() {
        return id;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Session otherSession = (Session) other;
        return id.equals(otherSession.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
