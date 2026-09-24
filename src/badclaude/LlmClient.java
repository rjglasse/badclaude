package badclaude;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Talks to any OpenAI-compatible chat API (OpenAI, OpenRouter, Groq, a
 * course-provided proxy, ...). Which one is decided by base_url and model
 * in config.properties.
 *
 * One method: send a list of messages, get the assistant's reply back.
 */
public class LlmClient implements Model {

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final HttpClient http = HttpClient.newHttpClient();

    public LlmClient(String baseUrl, String apiKey, String model) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    /** Sends the conversation to the model and returns its reply text. */
    @Override
    public String chat(List<Message> messages) throws Exception {
        // Build the JSON request body by hand -- it is simpler than it looks:
        // {"model": "...", "messages": [{"role": "...", "content": "..."}, ...]}
        StringBuilder body = new StringBuilder();
        body.append("{\"model\":").append(Json.quote(model));
        body.append(",\"messages\":[");
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            if (i > 0) {
                body.append(",");
            }
            body.append("{\"role\":").append(Json.quote(m.role));
            body.append(",\"content\":").append(Json.quote(m.content)).append("}");
        }
        body.append("]}");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            String details = response.body().isBlank()
                    ? "(empty response -- is base_url right? It usually ends in /v1)"
                    : response.body();
            if (details.contains("role 'tool'")) {
                details += "\n(Hint: send tool results back as \"user\" messages, not \"tool\" -- see Suggestion 3.)";
            }
            throw new RuntimeException("API returned " + response.statusCode() + ": " + details);
        }

        Object json = Json.parse(response.body());
        Object content = Json.get(json, "choices", 0, "message", "content");
        if (content == null) {
            throw new RuntimeException("Could not find a reply in: " + response.body());
        }
        return content.toString();
    }
}
