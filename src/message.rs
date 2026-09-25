use std::fmt;

/// One chat message. The role is "system", "user" or "assistant" -- the API
/// accepts nothing else from BadClaude.
///
/// (The API also has a "tool" role, but it belongs to the provider's built-in
/// function calling, which BadClaude doesn't use: a "tool" message is rejected
/// unless it answers a special "tool_calls" message. Send your tool results
/// back as user messages instead -- see Suggestion 3.)
//
// `#[derive(Clone)]` lets you make a copy of a message with `.clone()`.
#[derive(Clone)]
pub struct Message {
    pub role: String,
    pub content: String,
}

impl Message {
    pub fn new(role: &str, content: &str) -> Message {
        Message {
            role: role.to_string(),
            content: content.to_string(),
        }
    }

    pub fn system(content: &str) -> Message {
        Message::new("system", content)
    }

    pub fn user(content: &str) -> Message {
        Message::new("user", content)
    }

    pub fn assistant(content: &str) -> Message {
        Message::new("assistant", content)
    }
}

/// Lets you print a message with `println!("{}", message)`.
impl fmt::Display for Message {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}: {}", self.role, self.content)
    }
}
