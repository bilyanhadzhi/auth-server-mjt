package bg.sofia.uni.fmi.mjt.authserver.auditlog;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.Event;

/**
 * The audit log contains information of noteworthy events.
 * A logger writes to the log.
 */
public interface AuthAuditLogger {
    /**
     * Logs event.
     */
    void logEvent(Event event);
}
