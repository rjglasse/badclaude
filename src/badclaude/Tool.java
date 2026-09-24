package badclaude;

/**
 * Something the model can ask the harness to do in the real world:
 * calculate, roll dice, read a file, compile some Java...
 *
 * The model itself can only produce text, so you also need a protocol:
 * tell it (in the system prompt) how to ask for a tool, spot that request
 * in its reply, run the tool, and send the result back. See Suggestion 3.
 */
public interface Tool {

    /** Short name the model uses to call this tool, e.g. "calculator". */
    String name();

    /** One line telling the model what this tool does and what input it wants. */
    String description();

    /** Runs the tool on the given input and returns the result as text. */
    String run(String input);
}
