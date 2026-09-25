// BadClaude: a deliberately minimal LLM harness.
//
// It works, but it is bad on purpose: it forgets everything, it can't use
// tools, and it gives up after one reply. Your job is to make it less bad.
// Start with SUGGESTIONS.md.
package main

import "fmt"

func main() {
	config := LoadConfig()
	if config.APIKey == "" {
		fmt.Println("No API key found.")
		fmt.Println("Set the OPENAI_API_KEY environment variable,")
		fmt.Println("or copy config.example.properties to config.properties and fill in api_key.")
		return
	}

	llm := NewLLMClient(config.BaseURL, config.APIKey, config.Model)
	log := NewInteractionLog(config.Model)

	// SEAM: BadClaude ships with no memory at all. Swap in your own
	// implementation of the Memory interface (Suggestion 1).
	var memory Memory = NoMemory{}

	harness := NewHarness(llm, memory, log)
	harness.Run()
}
