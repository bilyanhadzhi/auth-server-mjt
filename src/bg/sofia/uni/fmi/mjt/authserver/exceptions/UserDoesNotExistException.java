package bg.sofia.uni.fmi.mjt.authserver.exceptions;

public class UserDoesNotExistException extends AuthenticationException {
    public UserDoesNotExistException(String msg) {
        super(msg);
    }
}
