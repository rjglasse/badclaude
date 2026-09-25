# BadClaude 🤖

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

BadClaude is a working — but deliberately terrible — harness around a large
language model. It can chat, and that is *all* it can do:

- It **forgets everything** the moment you press enter. Ask it your name one
  message after telling it, and see what happens.
- It **can't calculate** reliably. Ask it for `348213 * 917 - 44` and check
  the answer with a real calculator.
- It **can't touch the world**. It can describe a Python program beautifully,
  but it cannot save a file, run anything, or run a test.
- It **gives up after one reply**. No planning, no retrying, no checking its
  own work.

Your mission this course: **make BadClaude less bad.** The model stays the
same cheap model all semester — every capability it gains comes from *your*
code around it. That surrounding code is called a **harness**, and it is the
same idea that powers real coding assistants.

The end-of-course goal is that your harness can handle a request like:

> *"Code a simple Python app to play a 2-player game of roll the dice that is
> tested and evaluated."*

...by actually writing the files, running them, running the tests, and
telling you how it went.

## Getting started

1. Install Python 3.9 or newer (`python3 --version` to check; on Windows,
   `py -3 --version` or `python --version`).
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

## Pick your language 🗣️

BadClaude speaks four languages. Same course, same steps, same terrible
model, different syntax. Each one lives on its own branch of this repository:

| Branch | Language | You'll need | Pick it if you... |
| --- | --- | --- | --- |
| `main` | ☕ Java | Java 17+ | ...like your types spelled out and your braces plentiful |
| `go` | 🐹 Go | Go 1.21+ | ...like small languages, fast builds and `if err != nil` |
| `python` 📍 *you are here* | 🐍 Python | Python 3.9+ | ...want the least code between you and a working agent |
| `rust` | 🦀 Rust | Rust (stable) | ...enjoy arguing with a compiler that is usually right |

To switch, check out the branch, e.g. `git checkout python` (Java is on
`main`). Each branch has its own README, SUGGESTIONS and REFLECTIONS, written
for that language.

- **Pick early and stick with it.** Your improvements live on the branch you
  commit them to. Switching halfway means porting your own work by hand,
  which, to be fair, is a brilliant way to learn a second language.
- **No language is easy mode.** The model is just as bad in all four, and the
  steps, the checkpoints and the final boss are the same everywhere.

## Infrastructure choices

BadClaude doesn't care *where* its model lives. It talks to anything that
speaks the OpenAI chat API, so you're welcome to bring your own infra. Most
of the AI industry is people bolting things onto a model someone else runs.
Here you get to choose who that someone is.

Pick your adventure:

### 1. ☁️ **The course default**
- Use the provider and key your teacher hands out.
- **Why**: Zero setup, and it works on day one. Boring, but honestly the
  right choice for most people.

### 2. 🔑 **Your own API provider**
- Any OpenAI-compatible provider works: OpenRouter, Groq, Together, Mistral,
  and friends. Change `base_url` and `model` in `config.properties`, and put
  your key in `OPENAI_API_KEY`.
- **Why**: You learn that "the API" is really a *protocol*, and that providers
  differ in speed, price, and how creatively they read the spec.
- **Watch out**: This is your money. Set a spending limit *before* your agent
  loop gets stuck in an infinite loop at 3 a.m.

### 3. 💻 **Your own hardware**
- Run a small model on your laptop with [Ollama](https://ollama.com) or
  [LM Studio](https://lmstudio.ai). Both expose an OpenAI-compatible
  endpoint, e.g. `base_url=http://localhost:11434/v1` for Ollama.
- BadClaude refuses to start without a key, and local servers don't need
  one, so set `api_key=local` (any text will do).
- **Why**: Free, private, and works offline. Your fan will sound like a small
  jet engine. That's normal. That's *learning*.

### 4. 🖥️ **A GPU cluster at KTH**
- Serve an open-weights model on university GPUs (for example with
  [vLLM](https://docs.vllm.ai), which speaks the same API), then point
  `base_url` at it. Ask your teacher about access before you start.
- **Why**: This is how real AI infrastructure works: queues, job scripts,
  SSH tunnels, and the special joy of your job starting just as you leave
  for lunch.

### ⚠️ The one rule: keep it bad

Whatever you choose, **the model must stay weak.** The goal is to take a bad
model and make it a better *agent*, not to swap in a smarter brain and call it
a day. Rough guide: something in the small/cheap tier (e.g. `gpt-4o-mini`), or
an open model of about **8B parameters or fewer**. If your model aces the
roll-the-dice task with no harness at all, it's too good. Swap it for a
dumber one. 🥔

Changing infrastructure is a great improvement to write up in
[REFLECTIONS.md](REFLECTIONS.md): what did you have to change, and what broke?
Record which provider and model you used (the logs already save the model
name), so your improvements can be compared fairly.

## What's in the box

| File | What it does |
| --- | --- |
| `badclaude/main.py` | Wires everything together and starts the loop |
| `badclaude/__main__.py` | Lets you start it with `python3 -m badclaude` |
| `badclaude/harness.py` | The chat loop: read → send → print |
| `badclaude/llm_client.py` | Sends messages to the API, returns the reply |
| `badclaude/message.py` | One chat message (role + content) |
| `badclaude/memory.py` | Abstract base class: what the harness remembers |
| `badclaude/no_memory.py` | The default memory: none at all |
| `badclaude/tool.py` | Abstract base class: things the model can ask the harness to do |
| `badclaude/interaction_log.py` | Logs every session to `logs/*.jsonl` |
| `badclaude/config.py` | Reads `config.properties` |

No build tool, no dependencies, nothing to `pip install`: Python's standard
library (`urllib` for the web, `json` for JSON) is all you need, and the run
scripts start it for you.

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
4. **Change anything, and keep it tidy.** The starter code is yours: split
   `run()` into methods, add classes, rename, move and delete. The seams we
   left (the `Memory` and `Tool` abstract base classes, the `SEAM:`
   comments) are hints, not walls. When a new feature would make the code
   messy, reorganise first (SUGGESTIONS.md marks the moments with **Tidy up
   first**) and commit that on its own with a message starting `refactor:`.
5. **Don't upgrade the model.** The point is to make a weak model capable
   through engineering. Changing `model` to something smarter is cheating —
   and also less fun. Bringing your own infrastructure is fine, as long as
   the model stays bad (see [Infrastructure choices](#infrastructure-choices)).

## Designed by

- Alexander Baltatzis <alba@kth.se>
- Ric Glassey <glassey@kth.se>
