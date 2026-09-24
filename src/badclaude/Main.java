package badclaude;

/**
 * BadClaude: a deliberately minimal LLM harness.
 *
 * It works, but it is bad on purpose: it forgets everything, it can't use
 * tools, and it gives up after one reply. Your job is to make it less bad.
 * Start with SUGGESTIONS.md.
 */
public class Main {

    public static void main(String[] args) {
        Config config = Config.load();
        if (config.apiKey.isEmpty()) {
            System.out.println("No API key found.");
            System.out.println("Set the OPENAI_API_KEY environment variable,");
            System.out.println("or copy config.example.properties to config.properties and fill in api_key.");
            return;
        }

        LlmClient llm = new LlmClient(config.baseUrl, config.apiKey, config.model);
        InteractionLog log = new InteractionLog(config.model);

        // SEAM: BadClaude ships with no memory at all. Swap in your own
        // implementation of the Memory interface (Suggestion 1).
        Memory memory = new NoMemory();

        Harness harness = new Harness(llm, memory, log);
        harness.run();
    }
}
