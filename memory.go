package main

// Memory is what the harness remembers between turns.
//
// The harness calls Add() after every exchange and Recall() before every
// request. What you store, how much of it, and what you give back is
// entirely up to your implementation.
type Memory interface {
	// Add is called after each message so the memory can store it (or not).
	Add(message Message)

	// Recall returns the messages to include before the newest user message.
	Recall() []Message
}
