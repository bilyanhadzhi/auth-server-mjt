package bg.sofia.uni.fmi.mjt.authserver.auditlog.events;

import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.UUID;

public class BeginResourceChangeEvent extends AbstractEvent {
    private final UUID id;
    private final String performedOnUsername;
    private final ResourceChangeEventType resourceChangeEventType;

    public BeginResourceChangeEvent(LocalDateTime timestamp, String performerUsername,
                                    SocketAddress performerAddress, String performedOnUsername,
                                    ResourceChangeEventType resourceChangeEventType) {
        super(timestamp, performerUsername, performerAddress);
        this.id = UUID.randomUUID();
        this.performedOnUsername = performedOnUsername;
        this.resourceChangeEventType = resourceChangeEventType;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public EventType getType() {
        return EventType.RESOURCE_CHANGE;
    }

    @Override
    public String toString() {
        return String.join(DELIMITER,
                "[" + getTimestamp() + "]",
                getType().toString(),
                id.toString(),
                getPerformerUsername(),
                getPerformerAddressFormatted(),
                resourceChangeEventType.toString(),
                performedOnUsername);
    }
}
