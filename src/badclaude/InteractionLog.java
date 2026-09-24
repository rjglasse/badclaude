package badclaude;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

/**
 * Appends every interaction to a JSON Lines file under logs/ -- one JSON
 * object per line, one file per session.
 *
 * PLEASE LEAVE THE LOGGING IN PLACE. The logs are part of your submission:
 * they let you (and us) see exactly how each improvement changed what
 * BadClaude can do. If a session contains something you'd rather not share,
 * you may delete that session's file.
 *
 * Feel free to record MORE events (tool calls are a great one).
 */
public class InteractionLog {

    private PrintWriter out;

    public InteractionLog(String model) {
        try {
            Files.createDirectories(Paths.get("logs"));
            String stamp = Instant.now().toString().replace(":", "-");
            FileWriter file = new FileWriter("logs/session-" + stamp + ".jsonl", true);
            out = new PrintWriter(file, true);
            record("session_start", "model=" + model);
        } catch (IOException e) {
            System.out.println("[warn] Could not open log file: " + e.getMessage());
            out = null;
        }
    }

    /** Records one event. Role is e.g. "user", "assistant", "tool", "error". */
    public void record(String role, String content) {
        if (out == null) {
            return;
        }
        out.println("{\"time\":" + Json.quote(Instant.now().toString())
                + ",\"role\":" + Json.quote(role)
                + ",\"content\":" + Json.quote(content) + "}");
    }

    public void close() {
        if (out != null) {
            record("session_end", "");
            out.close();
        }
    }
}
