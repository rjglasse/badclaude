package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

// SEAM: a better system prompt is the cheapest improvement you can make
// (Suggestion 2).
const systemPrompt = "You are BadClaude, a helpful assistant."

// Harness is the heart of BadClaude: read a line, send it to the model,
// print the reply. That's it. Every improvement you make plugs in here or
// replaces a piece this type uses.
type Harness struct {
	llm    *LLMClient
	memory Memory
	log    *InteractionLog

	// SEAM: register your tools here and teach the model to ask for them
	// (Suggestions 3 and 4).
	tools []Tool
}

// NewHarness puts the pieces together.
func NewHarness(llm *LLMClient, memory Memory, log *InteractionLog) *Harness {
	return &Harness{llm: llm, memory: memory, log: log, tools: []Tool{}}
}

// Run is the chat loop. It returns when you type /quit or input ends.
func (h *Harness) Run() {
	fmt.Println("BadClaude is listening. Type /quit to exit.")
	fmt.Println()

	reader := bufio.NewReader(os.Stdin)
	for {
		fmt.Print("you> ")
		line, err := reader.ReadString('\n')
		if err != nil && line == "" {
			break // end of input
		}
		input := strings.TrimSpace(line)
		if input == "" {
			continue
		}
		if input == "/quit" {
			break
		}

		h.log.Record("user", input)
		userMessage := UserMessage(input)

		// Build the request: system prompt, whatever the memory recalls,
		// then the new user message. With NoMemory, the model sees only
		// the newest message -- try asking it about your previous one!
		request := []Message{SystemMessage(systemPrompt)}
		for _, remembered := range h.memory.Recall() {
			request = append(request, remembered)
		}
		request = append(request, userMessage)

		reply, err := h.llm.Chat(request)
		if err != nil {
			problem := err.Error()
			fmt.Println("[error] " + problem)
			h.log.Record("error", problem)
			continue
		}

		h.memory.Add(userMessage)
		h.memory.Add(AssistantMessage(reply))
		h.log.Record("assistant", reply)

		// SEAM: this is where an agent loop would check the reply for a
		// tool request, run the tool, and go back to the model with the
		// result instead of printing straight away (Suggestion 4).
		fmt.Println()
		fmt.Println("badclaude> " + reply)
		fmt.Println()
	}

	h.log.Close()
	fmt.Println("Bye!")
}
