use std::env;
use std::fs;

/// Settings for the harness, read from config.properties in the folder you
/// run from. The API key can also come from the OPENAI_API_KEY environment
/// variable, which takes priority -- and keeps keys out of git.
pub struct Config {
    pub base_url: String,
    pub model: String,
    pub api_key: String,
}

impl Config {
    pub fn load() -> Config {
        let mut base_url = String::from("https://api.openai.com/v1");
        let mut model = String::from("gpt-4o-mini");
        let mut api_key = String::new();

        // No config file is fine if the environment provides the key,
        // so if reading fails we just keep the defaults.
        if let Ok(text) = fs::read_to_string("config.properties") {
            // Each line looks like `key=value`. Lines starting with # are
            // comments.
            for line in text.lines() {
                let line = line.trim();
                if line.is_empty() || line.starts_with('#') {
                    continue;
                }
                // Split at the first '=' into the key and the value.
                if let Some((key, value)) = line.split_once('=') {
                    let key = key.trim();
                    let value = value.trim().to_string();
                    if key == "base_url" {
                        base_url = value;
                    } else if key == "model" {
                        model = value;
                    } else if key == "api_key" {
                        api_key = value;
                    }
                }
            }
        }

        if base_url.ends_with('/') {
            base_url.pop();
        }

        // The environment variable wins over the file.
        if let Ok(from_env) = env::var("OPENAI_API_KEY") {
            if !from_env.trim().is_empty() {
                api_key = from_env;
            }
        }

        Config {
            base_url,
            model,
            api_key: api_key.trim().to_string(),
        }
    }
}
