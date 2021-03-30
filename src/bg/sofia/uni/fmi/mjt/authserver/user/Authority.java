package bg.sofia.uni.fmi.mjt.authserver.user;

public enum Authority {
    USER("USER"),
    ADMIN("ADMIN");

    private final String text;

    Authority(String text) {
        this.text = text;
    }

    public boolean isAtLeast(Authority authority) {
        return this.ordinal() >= authority.ordinal();
    }

    @Override
    public String toString() {
        return text;
    }

    public static Authority fromString(String text) {
        for (Authority authority : Authority.values()) {
            if (authority.toString().equalsIgnoreCase(text)) {
                return authority;
            }
        }
        return null;
    }
}
