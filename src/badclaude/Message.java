package badclaude;

/**
 * One chat message. The role is "system", "user" or "assistant"
 * (and later maybe "tool" -- that one is up to you).
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
