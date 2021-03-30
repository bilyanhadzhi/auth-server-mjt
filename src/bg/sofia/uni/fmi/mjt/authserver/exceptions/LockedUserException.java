package bg.sofia.uni.fmi.mjt.authserver.exceptions;

public class LockedUserException extends AuthenticationException {
    public LockedUserException(String msg) {
        super(msg);
    }
}
