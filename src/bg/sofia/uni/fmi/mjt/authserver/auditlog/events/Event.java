package bg.sofia.uni.fmi.mjt.authserver.auditlog.events;

import java.net.SocketAddress;
import java.time.LocalDateTime;

public interface Event {
    LocalDateTime getTimestamp();

    EventType getType();

    String getPerformerUsername();

    SocketAddress getPerformerAddress();

    String getPerformerAddressFormatted();
}
