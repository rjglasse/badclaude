package main

import (
	"bufio"
	"os"
	"strings"
)

// Config holds the settings for the harness, read from config.properties in
// the folder you run from. The API key can also come from the OPENAI_API_KEY
// environment variable, which takes priority -- and keeps keys out of git.
type Config struct {
	BaseURL string
	Model   string
	APIKey  string
}

// LoadConfig reads config.properties (if there is one) and the environment.
func LoadConfig() Config {
	props := readProperties("config.properties")

	baseURL := strings.TrimSpace(getProperty(props, "base_url", "https://api.openai.com/v1"))
	if strings.HasSuffix(baseURL, "/") {
		baseURL = baseURL[:len(baseURL)-1]
	}
	model := strings.TrimSpace(getProperty(props, "model", "gpt-4o-mini"))

	apiKey := os.Getenv("OPENAI_API_KEY")
	if strings.TrimSpace(apiKey) == "" {
		apiKey = getProperty(props, "api_key", "")
	}

	return Config{BaseURL: baseURL, Model: model, APIKey: strings.TrimSpace(apiKey)}
}

// readProperties reads a file of key=value lines into a map. Blank lines and
// lines starting with # are skipped.
func readProperties(path string) map[string]string {
	props := make(map[string]string)
	file, err := os.Open(path)
	if err != nil {
		// No config file is fine if the environment provides the key.
		return props
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line == "" || strings.HasPrefix(line, "#") {
			continue
		}
		equals := strings.Index(line, "=")
		if equals < 0 {
			continue
		}
		key := strings.TrimSpace(line[:equals])
		value := strings.TrimSpace(line[equals+1:])
		props[key] = value
	}
	return props
}

// getProperty returns the value for key, or defaultValue if the key is missing.
func getProperty(props map[string]string, key string, defaultValue string) string {
	value, found := props[key]
	if !found {
		return defaultValue
	}
	return value
}
