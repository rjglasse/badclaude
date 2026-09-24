package badclaude;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Settings for the harness, read from config.properties in the folder you
 * run from. The API key can also come from the OPENAI_API_KEY environment
 * variable, which takes priority -- and keeps keys out of git.
 */
public class Config {

    public final String baseUrl;
    public final String model;
    public final String apiKey;

    private Config(String baseUrl, String model, String apiKey) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
    }

    public static Config load() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        } catch (Exception e) {
            // No config file is fine if the environment provides the key.
        }

        String baseUrl = props.getProperty("base_url", "https://api.openai.com/v1").trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String model = props.getProperty("model", "gpt-4o-mini").trim();

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = props.getProperty("api_key", "");
        }

        return new Config(baseUrl, model, apiKey.trim());
    }
}
