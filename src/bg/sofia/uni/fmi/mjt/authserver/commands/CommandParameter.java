package bg.sofia.uni.fmi.mjt.authserver.commands;

import java.util.Objects;

public class CommandParameter {
    private static final String argumentPrefix = "--";
    private final String name;
    private final boolean required;

    public CommandParameter(String name) {
        this.name = name;
        this.required = true;
    }

    public CommandParameter(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public String getName() {
        return argumentPrefix + name;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CommandParameter otherCommandParameter = (CommandParameter) other;
        return name.equals(otherCommandParameter.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, required);
    }
}
