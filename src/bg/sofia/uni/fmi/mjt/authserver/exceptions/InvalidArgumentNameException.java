package bg.sofia.uni.fmi.mjt.authserver.exceptions;

public class InvalidArgumentNameException extends CommandParseException {
    public InvalidArgumentNameException(String msg) {
        super(msg);
    }
}
