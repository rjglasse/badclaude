import json
import urllib.error
import urllib.request


class LlmClient:
    """
    Talks to any OpenAI-compatible chat API (OpenAI, OpenRouter, Groq, a
    course-provided proxy, ...). Which one is decided by base_url and model
    in config.properties.

    One method: send a list of messages, get the assistant's reply back.
    """

    def __init__(self, base_url, api_key, model):
        self.base_url = base_url
        self.api_key = api_key
        self.model = model

    def chat(self, messages):
        """Sends the conversation (a list of Message) to the model and returns its reply text."""
        # Build the request body as Python dictionaries and lists, then let
        # json.dumps turn it into JSON text. It is simpler than it looks:
        # {"model": "...", "messages": [{"role": "...", "content": "..."}, ...]}
        message_list = []
        for m in messages:
            message_list.append({"role": m.role, "content": m.content})
        body = {"model": self.model, "messages": message_list}

        request = urllib.request.Request(
            self.base_url + "/chat/completions",
            data=json.dumps(body).encode("utf-8"),
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer " + self.api_key,
            },
            method="POST",
        )

        try:
            response = urllib.request.urlopen(request)
            status = response.status
            text = response.read().decode("utf-8")
        except urllib.error.HTTPError as e:
            # urllib raises HTTPError for any error status (404, 401, ...),
            # but the body of the error response still tells us what went wrong.
            status = e.code
            text = e.read().decode("utf-8", "replace")

        if status != 200:
            if text.strip() == "":
                details = "(empty response -- is base_url right? It usually ends in /v1)"
            else:
                details = text
            if "role 'tool'" in details:
                details += "\n(Hint: send tool results back as \"user\" messages, not \"tool\" -- see Suggestion 3.)"
            raise RuntimeError("API returned " + str(status) + ": " + details)

        data = json.loads(text)
        content = find_reply(data)
        if content is None:
            raise RuntimeError("Could not find a reply in: " + text)
        return str(content)


def find_reply(data):
    """
    Digs data["choices"][0]["message"]["content"] out of the parsed JSON.
    Returns None if any step along the way is missing.
    """
    if not isinstance(data, dict):
        return None
    choices = data.get("choices")
    if not isinstance(choices, list) or len(choices) == 0:
        return None
    first = choices[0]
    if not isinstance(first, dict):
        return None
    message = first.get("message")
    if not isinstance(message, dict):
        return None
    return message.get("content")
