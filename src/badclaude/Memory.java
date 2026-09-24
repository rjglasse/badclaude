package badclaude;

import java.util.List;

/**
 * What the harness remembers between turns.
 *
 * The harness calls add() after every exchange and recall() before every
 * request. What you store, how much of it, and what you give back is
 * entirely up to your implementation.
 */
public interface Memory {

    /** Called after each message so the memory can store it (or not). */
    void add(Message message);

    /** Returns the messages to include before the newest user message. */
    List<Message> recall();
}
