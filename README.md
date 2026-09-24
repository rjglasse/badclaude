# BadClaude 🤖

BadClaude is a working — but deliberately terrible — harness around a large
language model. It can chat, and that is *all* it can do:

- It **forgets everything** the moment you press enter. Ask it your name one
  message after telling it, and see what happens.
- It **can't calculate** reliably. Ask it for `348213 * 917 - 44` and check
  the answer with a real calculator.
- It **can't touch the world**. It can describe a Java program beautifully,
  but it cannot save a file, compile anything, or run a test.
- It **gives up after one reply**. No planning, no retrying, no checking its
  own work.

Your mission this course: **make BadClaude less bad.** The model stays the
same cheap model all semester — every capability it gains comes from *your*
code around it. That surrounding code is called a **harness**, and it is the
same idea that powers real coding assistants.

The end-of-course goal is that your harness can handle a request like:

> *"Code a simple Java app to play a 2-player game of roll-the-dice, that is
> tested and evaluated."*

...by actually writing the files, compiling them, running the tests, and
telling you how it went.

## Getting started

1. Install Java 17 or newer (`java -version` to check).
2. Get an API key (your teacher will tell you which provider the course uses).
3. Copy `config.example.properties` to `config.properties` and fill in
   `base_url` and `model`. Put your key in the `OPENAI_API_KEY` environment
   variable (preferred) or in `config.properties` (which is gitignored —
   never commit a key).
4. Run it:
   - macOS / Linux: `./run.sh`
   - Windows: `run.bat`
5. Chat. Type `/quit` to exit.

Then open **[SUGGESTIONS.md](SUGGESTIONS.md)** and start improving.

## What's in the box

| File | What it does |
| --- | --- |
| `src/badclaude/Main.java` | Wires everything together and starts the loop |
| `src/badclaude/Harness.java` | The chat loop: read → send → print |
| `src/badclaude/LlmClient.java` | Sends messages to the API, returns the reply |
| `src/badclaude/Message.java` | One chat message (role + content) |
| `src/badclaude/Memory.java` | Interface: what the harness remembers |
| `src/badclaude/NoMemory.java` | The default memory: none at all |
| `src/badclaude/Tool.java` | Interface: things the model can ask the harness to do |
| `src/badclaude/Json.java` | Tiny JSON reader/writer (no libraries needed) |
| `src/badclaude/InteractionLog.java` | Logs every session to `logs/*.jsonl` |
| `src/badclaude/Config.java` | Reads `config.properties` |

No build tool, no dependencies: `javac` and `java` are all you need, and the
run scripts do that for you.

## Rules of the game

1. **Keep the logging.** `InteractionLog` records your sessions to `logs/`
   and those logs are part of your submission — they are how you (and we)
   can *see* each improvement working. Log more if you like (tool calls!),
   never less. You may delete any individual session file you don't want
   to share.
2. **One improvement per commit**, with a message starting `improve:`, e.g.
   `improve: conversation memory`. Commit the "before" state of an
   experiment too if it helps tell the story.
3. **Reflect as you go.** After each improvement, add an entry to
   [REFLECTIONS.md](REFLECTIONS.md). Ten honest minutes right after it works
   beats an hour of trying to remember at the end of the course.
4. **You may change any file.** The seams we left (the `Memory` and `Tool`
   interfaces, the `SEAM:` comments) are hints, not walls.
5. **Don't upgrade the model.** The point is to make a weak model capable
   through engineering. Changing `model` to something smarter is cheating —
   and also less fun.
