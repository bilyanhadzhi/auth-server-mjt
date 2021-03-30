package bg.sofia.uni.fmi.mjt.authserver.user;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An existing user.
 */
public class User {
    private static final String NULL_TEXT = "NULL";

    private String username;
    private Password passwordHash;
    private String firstName;
    private String lastName;
    private Email email;
    private LocalDateTime lockedUntil;
    private Authority authority;

    public User(String username, Password passwordHash, String firstName, String lastName,
                Email email, Authority authority, LocalDateTime lockedUntil) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.authority = authority;
        this.lockedUntil = lockedUntil;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(Password passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    public String getUsername() {
        return username;
    }

    public Password getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Email getEmail() {
        return email;
    }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public Authority getAuthority() {
        return authority;
    }

    public String toStringWithDelimiter(String delimiter) {
        return String.join(delimiter,
                getUsername(),
                getPasswordHash().getValue(),
                getFirstName(),
                getLastName(),
                getEmail().getValue(),
                getAuthority().toString(),
                Integer.toString(passwordHash.getFailedAttempts()),
                lockedUntil == null ? NULL_TEXT : lockedUntil.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        User otherUser = (User) other;
        return username.equals(otherUser.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
