import json
import os
from datetime import datetime, timezone


def now_utc():
    """The current time in UTC as ISO 8601 text, e.g. 2026-09-25T09:24:00.123456Z"""
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


class InteractionLog:
    """
    Appends every interaction to a JSON Lines file under logs/ -- one JSON
    object per line, one file per session.

    PLEASE LEAVE THE LOGGING IN PLACE. The logs are part of your submission:
    they let you (and us) see exactly how each improvement changed what
    BadClaude can do. If a session contains something you'd rather not share,
    you may delete that session's file.

    Feel free to record MORE events (tool calls are a great one).
    """

    def __init__(self, model):
        try:
            os.makedirs("logs", exist_ok=True)
            stamp = now_utc().replace(":", "-")
            self.out = open("logs/session-" + stamp + ".jsonl", "a", encoding="utf-8")
            self.record("session_start", "model=" + model)
        except OSError as e:
            print("[warn] Could not open log file: " + str(e))
            self.out = None

    def record(self, role, content):
        """Records one event. Role is e.g. "user", "assistant", "tool", "error"."""
        if self.out is None:
            return
        event = {"time": now_utc(), "role": role, "content": content}
        # separators=(",", ":") leaves out the spaces, so each line is compact.
        line = json.dumps(event, ensure_ascii=False, separators=(",", ":"))
        self.out.write(line + "\n")
        self.out.flush()  # write it to disk now, in case the program crashes

    def close(self):
        if self.out is not None:
            self.record("session_end", "")
            self.out.close()
