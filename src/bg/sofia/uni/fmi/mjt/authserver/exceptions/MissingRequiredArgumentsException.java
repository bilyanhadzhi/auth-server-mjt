package bg.sofia.uni.fmi.mjt.authserver.exceptions;

import java.util.Collections;
import java.util.Set;

public class MissingRequiredArgumentsException extends CommandParseException {
    private final Set<String> missingArguments;

    public MissingRequiredArgumentsException(Set<String> missingArguments) {
        super("The following mandatory parameters were not supplied: " + missingArguments);
        this.missingArguments = missingArguments;
    }

    public Set<String> getMissingArguments() {
        return Collections.unmodifiableSet(missingArguments);
    }
}
