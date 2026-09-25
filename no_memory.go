package main

// NoMemory is BadClaude's default memory: none. Every message is a fresh
// start and the model has no idea what was said one turn ago.
//
// Replacing this type is Suggestion 1 -- and probably the single biggest
// improvement you will ever make to BadClaude.
type NoMemory struct{}

// Add forgets the message immediately.
func (m NoMemory) Add(message Message) {
	// Forget it immediately.
}

// Recall always returns an empty list.
func (m NoMemory) Recall() []Message {
	return []Message{}
}
