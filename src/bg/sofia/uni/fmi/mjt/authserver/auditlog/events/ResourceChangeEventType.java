package bg.sofia.uni.fmi.mjt.authserver.auditlog.events;

public enum ResourceChangeEventType {
    ADD_ADMIN("ADD-ADMIN"),
    REMOVE_ADMIN("REMOVE-ADMIN");

    private final String text;

    ResourceChangeEventType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
