package main

// Message is one chat message. The role is "system", "user" or "assistant"
// -- the API accepts nothing else from BadClaude.
//
// (The API also has a "tool" role, but it belongs to the provider's built-in
// function calling, which BadClaude doesn't use: a "tool" message is rejected
// unless it answers a special "tool_calls" message. Send your tool results
// back as user messages instead -- see Suggestion 3.)
//
// The `json:"..."` tags tell encoding/json which names to use when a
// Message is turned into JSON for the API.
type Message struct {
	Role    string `json:"role"`
	Content string `json:"content"`
}

// SystemMessage makes a message with the role "system".
func SystemMessage(content string) Message {
	return Message{Role: "system", Content: content}
}

// UserMessage makes a message with the role "user".
func UserMessage(content string) Message {
	return Message{Role: "user", Content: content}
}

// AssistantMessage makes a message with the role "assistant".
func AssistantMessage(content string) Message {
	return Message{Role: "assistant", Content: content}
}

// String is what fmt.Println shows when you print a Message.
func (m Message) String() string {
	return m.Role + ": " + m.Content
}
