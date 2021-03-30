package bg.sofia.uni.fmi.mjt.authserver.auditlog.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class EndResourceChangeEvent extends AbstractEvent {
    private static final String SUCCESS_TEXT = "SUCCESS";
    private static final String FAILURE_TEXT = "FAILURE";

    private final UUID id;
    private final boolean successful;

    public EndResourceChangeEvent(LocalDateTime timestamp, BeginResourceChangeEvent beginEvent,
                                  boolean successful) {
        super(timestamp, beginEvent.getPerformerUsername(),
                beginEvent.getPerformerAddress());

        this.id = beginEvent.getId();
        this.successful = successful;
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
                successful ? SUCCESS_TEXT : FAILURE_TEXT);
    }
}
