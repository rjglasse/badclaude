package badclaude;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A fake model that ignores what you send it and plays back replies from a
 * script file, one reply per call. Run it with:
 *
 *     ./run.sh --script scripts/two-dice.txt
 *
 * A script is plain text; replies are separated by a line containing only
 * "---". It can't read your tool results, so it can't really add up dice --
 * what it tests is your harness: does it loop, stop and recover the way you
 * meant it to? Write your own scripts for the cases you want to check.
 */
public class ScriptedModel implements Model {

    private final List<String> replies = new ArrayList<>();
    private int next = 0;

    public ScriptedModel(String scriptFile) throws Exception {
        String text = new String(Files.readAllBytes(Paths.get(scriptFile)), StandardCharsets.UTF_8);
        StringBuilder reply = new StringBuilder();
        for (String line : text.split("\r?\n", -1)) {
            if (line.trim().equals("---")) {
                replies.add(reply.toString().trim());
                reply.setLength(0);
            } else {
                reply.append(line).append("\n");
            }
        }
        if (!reply.toString().trim().isEmpty()) {
            replies.add(reply.toString().trim());
        }
    }

    @Override
    public String chat(List<Message> messages) {
        if (next >= replies.size()) {
            throw new RuntimeException("the script has no more replies (it had " + replies.size() + ")");
        }
        return replies.get(next++);
    }
}
