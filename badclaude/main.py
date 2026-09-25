"""
BadClaude: a deliberately minimal LLM harness.

It works, but it is bad on purpose: it forgets everything, it can't use
tools, and it gives up after one reply. Your job is to make it less bad.
Start with SUGGESTIONS.md.
"""

from badclaude.config import Config
from badclaude.harness import Harness
from badclaude.interaction_log import InteractionLog
from badclaude.llm_client import LlmClient
from badclaude.no_memory import NoMemory


def main():
    config = Config.load()
    if config.api_key == "":
        print("No API key found.")
        print("Set the OPENAI_API_KEY environment variable,")
        print("or copy config.example.properties to config.properties and fill in api_key.")
        return

    llm = LlmClient(config.base_url, config.api_key, config.model)
    log = InteractionLog(config.model)

    # SEAM: BadClaude ships with no memory at all. Swap in your own
    # subclass of Memory (Suggestion 1).
    memory = NoMemory()

    harness = Harness(llm, memory, log)
    harness.run()
