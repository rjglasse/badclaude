package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"strconv"
	"strings"
)

// LLMClient talks to any OpenAI-compatible chat API (OpenAI, OpenRouter,
// Groq, a course-provided proxy, ...). Which one is decided by base_url and
// model in config.properties.
//
// One method: send a list of messages, get the assistant's reply back.
type LLMClient struct {
	baseURL string
	apiKey  string
	model   string
	http    *http.Client
}

// NewLLMClient makes a client for the given API endpoint, key and model.
func NewLLMClient(baseURL string, apiKey string, model string) *LLMClient {
	return &LLMClient{baseURL: baseURL, apiKey: apiKey, model: model, http: &http.Client{}}
}

// chatRequest is the JSON we send:
// {"model": "...", "messages": [{"role": "...", "content": "..."}, ...]}
type chatRequest struct {
	Model    string    `json:"model"`
	Messages []Message `json:"messages"`
}

// chatResponse is the part of the API's JSON answer we care about:
// {"choices": [{"message": {"content": "..."}}, ...]}
type chatResponse struct {
	Choices []chatChoice `json:"choices"`
}

type chatChoice struct {
	Message Message `json:"message"`
}

// Chat sends the conversation to the model and returns its reply text.
func (c *LLMClient) Chat(messages []Message) (string, error) {
	// encoding/json turns our structs into JSON text for us.
	body, err := json.Marshal(chatRequest{Model: c.model, Messages: messages})
	if err != nil {
		return "", err
	}

	request, err := http.NewRequest("POST", c.baseURL+"/chat/completions", bytes.NewReader(body))
	if err != nil {
		return "", err
	}
	request.Header.Set("Content-Type", "application/json")
	request.Header.Set("Authorization", "Bearer "+c.apiKey)

	response, err := c.http.Do(request)
	if err != nil {
		return "", err
	}
	defer response.Body.Close()

	responseBytes, err := io.ReadAll(response.Body)
	if err != nil {
		return "", err
	}
	responseBody := string(responseBytes)

	if response.StatusCode != 200 {
		details := responseBody
		if strings.TrimSpace(responseBody) == "" {
			details = "(empty response -- is base_url right? It usually ends in /v1)"
		}
		if strings.Contains(details, "role 'tool'") {
			details += "\n(Hint: send tool results back as \"user\" messages, not \"tool\" -- see Suggestion 3.)"
		}
		return "", errors.New("API returned " + strconv.Itoa(response.StatusCode) + ": " + details)
	}

	var parsed chatResponse
	err = json.Unmarshal(responseBytes, &parsed)
	if err != nil {
		return "", err
	}
	if len(parsed.Choices) == 0 || parsed.Choices[0].Message.Content == "" {
		return "", errors.New("Could not find a reply in: " + responseBody)
	}
	return parsed.Choices[0].Message.Content, nil
}
