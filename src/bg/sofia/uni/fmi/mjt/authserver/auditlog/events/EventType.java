package bg.sofia.uni.fmi.mjt.authserver.auditlog.events;

public enum EventType {
    FAILED_LOGIN("FAILED-LOGIN"),
    RESOURCE_CHANGE("RESOURCE-CHANGE");

    private final String text;

    EventType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
