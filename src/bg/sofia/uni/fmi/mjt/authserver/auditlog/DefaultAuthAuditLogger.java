package bg.sofia.uni.fmi.mjt.authserver.auditlog;

import bg.sofia.uni.fmi.mjt.authserver.auditlog.events.Event;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class DefaultAuthAuditLogger implements AuthAuditLogger {
    private final Path auditLogFilePath;

    public DefaultAuthAuditLogger(Path auditLogFilePath) {
        this.auditLogFilePath = auditLogFilePath;
    }

    @Override
    public void logEvent(Event event) {
        try (var bufferedWriter = new BufferedWriter(new FileWriter(auditLogFilePath.toString(), true))) {
            bufferedWriter.write(event.toString() + System.lineSeparator());
        } catch (IOException exception) {
            throw new RuntimeException("An error occurred while writing to audit log", exception);
        }
    }
}
