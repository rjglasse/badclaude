package main

// Tool is something the model can ask the harness to do in the real world:
// calculate, roll dice, read a file, compile some Go...
//
// The model itself can only produce text, so you also need a protocol:
// tell it (in the system prompt) how to ask for a tool, spot that request
// in its reply, run the tool, and send the result back. See Suggestion 3.
type Tool interface {
	// Name is the short name the model uses to call this tool, e.g. "calculator".
	Name() string

	// Description is one line telling the model what this tool does and what
	// input it wants.
	Description() string

	// Run runs the tool on the given input and returns the result as text.
	Run(input string) string
}
