package bg.sofia.uni.fmi.mjt.authserver.auditlog.events;

import java.net.SocketAddress;
import java.time.LocalDateTime;

public class FailedLoginEvent extends AbstractEvent {
    public FailedLoginEvent(LocalDateTime timestamp, String performerUsername,
                            SocketAddress performerAddress) {
        super(timestamp, performerUsername, performerAddress);
    }

    @Override
    public EventType getType() {
        return EventType.FAILED_LOGIN;
    }

    @Override
    public String toString() {
        return String.join(DELIMITER,
                "[" + getTimestamp() + "]",
                getType().toString(),
                getPerformerUsername(),
                getPerformerAddressFormatted());
    }
}
