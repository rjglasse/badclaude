from abc import ABC, abstractmethod


class Memory(ABC):
    """
    What the harness remembers between turns.

    The harness calls add() after every exchange and recall() before every
    request. What you store, how much of it, and what you give back is
    entirely up to your subclass.

    Memory is an abstract base class: you can't make a Memory() directly,
    only a subclass that fills in both methods below.
    """

    @abstractmethod
    def add(self, message):
        """Called after each message so the memory can store it (or not)."""

    @abstractmethod
    def recall(self):
        """Returns the list of messages to include before the newest user message."""
