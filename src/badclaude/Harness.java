package badclaude;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The heart of BadClaude: read a line, send it to the model, print the reply.
 * That's it. Every improvement you make plugs in here or replaces a piece
 * this class uses.
 */
public class Harness {

    // SEAM: a better system prompt is the cheapest improvement you can make
    // (Suggestion 2).
    private static final String SYSTEM_PROMPT = "You are BadClaude, a helpful assistant.";

    private final Model llm;
    private final Memory memory;
    private final InteractionLog log;

    // SEAM: register your tools here and teach the model to ask for them
    // (Suggestions 3 and 4).
    private final List<Tool> tools = new ArrayList<>();

    public Harness(Model llm, Memory memory, InteractionLog log) {
        this.llm = llm;
        this.memory = memory;
        this.log = log;
    }

    public void run() {
        System.out.println("BadClaude is listening. Type /quit to exit.");
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("you> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            if (input.equals("/quit")) {
                break;
            }

            log.record("user", input);
            Message userMessage = Message.user(input);

            // Build the request: system prompt, whatever the memory recalls,
            // then the new user message. With NoMemory, the model sees only
            // the newest message -- try asking it about your previous one!
            List<Message> request = new ArrayList<>();
            request.add(Message.system(SYSTEM_PROMPT));
            request.addAll(memory.recall());
            request.add(userMessage);

            String reply;
            try {
                reply = llm.chat(request);
            } catch (Exception e) {
                // Some exceptions (e.g. "can't connect") have no message, only a type.
                String problem = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                System.out.println("[error] " + problem);
                log.record("error", problem);
                continue;
            }

            memory.add(userMessage);
            memory.add(Message.assistant(reply));
            log.record("assistant", reply);

            // SEAM: this is where an agent loop would check the reply for a
            // tool request, run the tool, and go back to the model with the
            // result instead of printing straight away (Suggestion 4).
            System.out.println();
            System.out.println("badclaude> " + reply);
            System.out.println();
        }

        log.close();
        System.out.println("Bye!");
    }
}
