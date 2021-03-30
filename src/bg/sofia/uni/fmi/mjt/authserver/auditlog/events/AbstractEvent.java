package bg.sofia.uni.fmi.mjt.authserver.auditlog.events;

import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class AbstractEvent implements Event {
    protected static final String DELIMITER = " ";
    protected static final String PORT_DELIMITER = ":";

    // not sure what to call this
    protected static final String SLASH = "/";

    private final LocalDateTime timestamp;

    private final String performerUsername;
    private final SocketAddress performerAddress;

    public AbstractEvent(LocalDateTime timestamp,
                         String performerUsername, SocketAddress performerAddress) {
        this.timestamp = timestamp.truncatedTo(ChronoUnit.SECONDS);
        this.performerUsername = performerUsername;
        this.performerAddress = performerAddress;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public abstract EventType getType();

    @Override
    public String getPerformerUsername() {
        return performerUsername;
    }

    @Override
    public SocketAddress getPerformerAddress() {
        return performerAddress;
    }

    @Override
    public String getPerformerAddressFormatted() {
        return formatAddress(performerAddress);
    }

    private String formatAddress(SocketAddress socketAddress) {
        String address = socketAddress.toString();
        return address
                .substring(0, address.lastIndexOf(PORT_DELIMITER))
                .replace(SLASH, "");
    }
}
