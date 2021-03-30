package bg.sofia.uni.fmi.mjt.authserver.exceptions;

public class MultipleOccurrencesOfOneArgumentException extends CommandParseException {
    public MultipleOccurrencesOfOneArgumentException(String msg) {
        super(msg);
    }
}
