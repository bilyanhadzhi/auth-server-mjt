package bg.sofia.uni.fmi.mjt.authserver.exceptions;

import java.util.Collections;
import java.util.Set;

public class UnknownArgumentException extends CommandParseException {
    private final Set<String> parameters;
    public UnknownArgumentException(Set<String> parameters) {
        super("There are no parameters with names: " + parameters);
        this.parameters = parameters;
    }

    public Set<String> getParameters() {
        return Collections.unmodifiableSet(parameters);
    }
}
