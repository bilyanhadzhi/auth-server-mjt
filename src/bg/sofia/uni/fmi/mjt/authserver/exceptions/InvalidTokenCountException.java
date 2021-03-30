package bg.sofia.uni.fmi.mjt.authserver.exceptions;

public class InvalidTokenCountException extends CommandParseException {
    public InvalidTokenCountException(String msg) {
        super(msg);
    }
}
