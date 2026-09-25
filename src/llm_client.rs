use crate::message::Message;
use serde_json::{json, Value};

/// Talks to any OpenAI-compatible chat API (OpenAI, OpenRouter, Groq, a
/// course-provided proxy, ...). Which one is decided by base_url and model
/// in config.properties.
///
/// One method: send a list of messages, get the assistant's reply back.
pub struct LlmClient {
    base_url: String,
    api_key: String,
    model: String,
    http: ureq::Agent,
}

impl LlmClient {
    pub fn new(base_url: String, api_key: String, model: String) -> LlmClient {
        // By default ureq turns an HTTP error status (like 404) into an
        // error and throws the response body away. We want to read that
        // body, because it usually says what went wrong.
        let http_config = ureq::Agent::config_builder()
            .http_status_as_error(false)
            .build();
        LlmClient {
            base_url,
            api_key,
            model,
            http: ureq::Agent::new_with_config(http_config),
        }
    }

    /// Sends the conversation to the model and returns its reply text.
    ///
    /// `&[Message]` means "borrow a list of messages": pass it `&request`
    /// where `request` is your `Vec<Message>`.
    pub fn chat(&self, messages: &[Message]) -> Result<String, String> {
        // Build the JSON request body -- it is simpler than it looks:
        // {"model": "...", "messages": [{"role": "...", "content": "..."}, ...]}
        let mut json_messages = Vec::new();
        for m in messages {
            json_messages.push(json!({"role": m.role, "content": m.content}));
        }
        let body = json!({"model": self.model, "messages": json_messages});

        let url = format!("{}/chat/completions", self.base_url);
        let sent = self
            .http
            .post(&url)
            .header("Content-Type", "application/json")
            .header("Authorization", &format!("Bearer {}", self.api_key))
            .send(body.to_string());
        let mut response = match sent {
            Ok(response) => response,
            Err(e) => return Err(e.to_string()),
        };

        let status = response.status().as_u16();
        let text = match response.body_mut().read_to_string() {
            Ok(text) => text,
            Err(e) => return Err(e.to_string()),
        };

        if status != 200 {
            let mut details = if text.trim().is_empty() {
                String::from("(empty response -- is base_url right? It usually ends in /v1)")
            } else {
                text.clone()
            };
            if details.contains("role 'tool'") {
                details.push_str("\n(Hint: send tool results back as \"user\" messages, not \"tool\" -- see Suggestion 3.)");
            }
            return Err(format!("API returned {}: {}", status, details));
        }

        let json: Value = match serde_json::from_str(&text) {
            Ok(json) => json,
            Err(e) => return Err(e.to_string()),
        };
        // Indexing a JSON value gives Null if that part is missing, so this
        // never crashes -- we just check whether we found a string.
        let content = &json["choices"][0]["message"]["content"];
        match content.as_str() {
            Some(reply) => Ok(reply.to_string()),
            None => Err(format!("Could not find a reply in: {}", text)),
        }
    }
}
