package main

import (
	"encoding/json"
	"fmt"
	"os"
	"strings"
	"time"
)

// InteractionLog appends every interaction to a JSON Lines file under logs/
// -- one JSON object per line, one file per session.
//
// PLEASE LEAVE THE LOGGING IN PLACE. The logs are part of your submission:
// they let you (and us) see exactly how each improvement changed what
// BadClaude can do. If a session contains something you'd rather not share,
// you may delete that session's file.
//
// Feel free to record MORE events (tool calls are a great one).
type InteractionLog struct {
	file    *os.File
	encoder *json.Encoder
}

// logEvent is one line of the log file.
type logEvent struct {
	Time    string `json:"time"`
	Role    string `json:"role"`
	Content string `json:"content"`
}

// NewInteractionLog opens a new session file and records session_start.
// If the file can't be opened, it warns and the harness runs without logging.
func NewInteractionLog(model string) *InteractionLog {
	log := &InteractionLog{}
	err := os.MkdirAll("logs", 0755)
	if err != nil {
		fmt.Println("[warn] Could not open log file: " + err.Error())
		return log
	}
	stamp := strings.ReplaceAll(now(), ":", "-")
	file, err := os.OpenFile("logs/session-"+stamp+".jsonl", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		fmt.Println("[warn] Could not open log file: " + err.Error())
		return log
	}
	log.file = file
	log.encoder = json.NewEncoder(file)
	log.encoder.SetEscapeHTML(false) // keep < > & readable in the log
	log.Record("session_start", "model="+model)
	return log
}

// Record records one event. Role is e.g. "user", "assistant", "tool", "error".
func (l *InteractionLog) Record(role string, content string) {
	if l.file == nil {
		return
	}
	// Encode writes the event as one line of JSON, newline included.
	l.encoder.Encode(logEvent{Time: now(), Role: role, Content: content})
}

// Close records session_end and closes the file.
func (l *InteractionLog) Close() {
	if l.file != nil {
		l.Record("session_end", "")
		l.file.Close()
		l.file = nil
	}
}

// now returns the current time in UTC, ISO 8601 style,
// e.g. 2026-09-25T07:24:13.123456Z.
func now() string {
	return time.Now().UTC().Format(time.RFC3339Nano)
}
