package bg.sofia.uni.fmi.mjt.authserver.exceptions;

public class UserAlreadyExistsException extends StorageException {
    public UserAlreadyExistsException(String msg) {
        super(msg);
    }
}
