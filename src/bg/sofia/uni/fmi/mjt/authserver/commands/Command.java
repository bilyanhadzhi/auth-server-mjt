package bg.sofia.uni.fmi.mjt.authserver.commands;

/**
 * <p>
 *     A command is an act that can be executed.
 *     The user of this interface must implement the {@code execute} method,
 *     and return a proper message as a result from executing the command.
 * </p>
 */
public interface Command {
    /**
     * Executes the command and returns a response from its execution.
     *
     * @return a message - response from the execution
     */
    String execute();
}
