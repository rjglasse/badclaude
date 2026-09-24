package badclaude;

/**
 * One chat message. The role is "system", "user" or "assistant" -- the API
 * accepts nothing else from BadClaude.
 *
 * (The API also has a "tool" role, but it belongs to the provider's built-in
 * function calling, which BadClaude doesn't use: a "tool" message is rejected
 * unless it answers a special "tool_calls" message. Send your tool results
 * back as user messages instead -- see Suggestion 3.)
 */
public class Message {

    public final String role;
    public final String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static Message system(String content) {
        return new Message("system", content);
    }

    public static Message user(String content) {
        return new Message("user", content);
    }

    public static Message assistant(String content) {
        return new Message("assistant", content);
    }

    @Override
    public String toString() {
        return role + ": " + content;
    }
}
