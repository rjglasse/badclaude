class Message:
    """
    One chat message. The role is "system", "user" or "assistant" -- the API
    accepts nothing else from BadClaude.

    (The API also has a "tool" role, but it belongs to the provider's built-in
    function calling, which BadClaude doesn't use: a "tool" message is rejected
    unless it answers a special "tool_calls" message. Send your tool results
    back as user messages instead -- see Suggestion 3.)
    """

    def __init__(self, role, content):
        self.role = role
        self.content = content

    @staticmethod
    def system(content):
        return Message("system", content)

    @staticmethod
    def user(content):
        return Message("user", content)

    @staticmethod
    def assistant(content):
        return Message("assistant", content)

    def __str__(self):
        return self.role + ": " + self.content
