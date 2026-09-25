from badclaude.memory import Memory


class NoMemory(Memory):
    """
    BadClaude's default memory: none. Every message is a fresh start and the
    model has no idea what was said one turn ago.

    Replacing this class is Suggestion 1 -- and probably the single biggest
    improvement you will ever make to BadClaude.
    """

    def add(self, message):
        # Forget it immediately.
        pass

    def recall(self):
        return []
