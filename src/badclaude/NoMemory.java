package badclaude;

import java.util.ArrayList;
import java.util.List;

/**
 * BadClaude's default memory: none. Every message is a fresh start and the
 * model has no idea what was said one turn ago.
 *
 * Replacing this class is Suggestion 1 -- and probably the single biggest
 * improvement you will ever make to BadClaude.
 */
public class NoMemory implements Memory {

    @Override
    public void add(Message message) {
        // Forget it immediately.
    }

    @Override
    public List<Message> recall() {
        return new ArrayList<>();
    }
}
