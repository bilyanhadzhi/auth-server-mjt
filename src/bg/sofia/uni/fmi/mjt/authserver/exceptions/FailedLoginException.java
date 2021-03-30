package bg.sofia.uni.fmi.mjt.authserver.exceptions;

public class FailedLoginException extends AuthenticationException {
    public FailedLoginException(String msg) {
        super(msg);
    }
}
