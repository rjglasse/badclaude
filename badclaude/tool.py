from abc import ABC, abstractmethod


class Tool(ABC):
    """
    Something the model can ask the harness to do in the real world:
    calculate, roll dice, read a file, run some Python...

    The model itself can only produce text, so you also need a protocol:
    tell it (in the system prompt) how to ask for a tool, spot that request
    in its reply, run the tool, and send the result back. See Suggestion 3.

    Tool is an abstract base class: make a subclass that fills in all three
    methods below.
    """

    @abstractmethod
    def name(self):
        """Short name the model uses to call this tool, e.g. "calculator"."""

    @abstractmethod
    def description(self):
        """One line telling the model what this tool does and what input it wants."""

    @abstractmethod
    def run(self, input_text):
        """Runs the tool on the given input and returns the result as text."""
