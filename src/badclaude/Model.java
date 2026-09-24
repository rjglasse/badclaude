package badclaude;

import java.util.List;

/**
 * Anything that can answer a conversation: send it the messages so far, get
 * the next reply back.
 *
 * LlmClient is the real one (it calls the API). ScriptedModel is a fake one
 * that plays back replies from a file, so you can test your harness without
 * spending money and get the same answers every run (Suggestion 4).
 */
public interface Model {

    /** Returns the model's reply to the conversation so far. */
    String chat(List<Message> messages) throws Exception;
}
