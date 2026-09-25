# Language branches: parity checklist

BadClaude exists in several languages, one branch each:

| Branch | Language | Run with | Dependencies |
| --- | --- | --- | --- |
| `main` | Java 17+ | `./run.sh` (`javac` + `java`) | none (hand-rolled `Json.java`) |
| `go` | Go 1.21+ | `./run.sh` (`go run .`) | none (stdlib `net/http`, `encoding/json`) |
| `python` | Python 3.9+ | `./run.sh` (`python3 -m badclaude`) | none (stdlib `urllib`, `json`) |
| `rust` | Rust stable | `./run.sh` (`cargo run`) | `ureq` + `serde_json` (std has no HTTP/TLS) |

`main` is the reference. The other branches are **ports, not merges**: when
something changes on `main`, port it by hand to each branch and check the
branch against this list. Students on any branch must get the same course,
so the study can compare them.

## Must match exactly

- **Behaviour of the chat loop.** Greeting `BadClaude is listening. Type /quit
  to exit.` then a blank line; prompt `you> `; blank input is skipped; `/quit`
  or end of input ends the session and prints `Bye!`; reply printed as a blank
  line, `badclaude> <reply>`, blank line; errors printed as `[error] <problem>`
  and the loop continues.
- **Bad on purpose.** No memory (`NoMemory` is the default), no tools
  registered, one reply per turn, the same one-line system prompt
  `You are BadClaude, a helpful assistant.`, no request timeout (tracked
  separately as badclaude-lao; add it to `main` first).
- **Seams.** The same three `SEAM:` comments in the same places: memory choice
  in main, system prompt and tools list in the harness, the agent-loop spot
  after the reply.
- **The same abstractions.** `Message` (role + content, constructors for
  system / user / assistant), `Memory` (`add`, `recall`), `NoMemory`, `Tool`
  (`name`, `description`, `run(input) -> text`), `LlmClient` (one `chat`
  method), `InteractionLog` (`record`, `close`), `Config` (`load`). Named in
  each language's style (`add` / `Add`, `llm_client.py`, ...).
- **Config.** File `config.properties` (key=value, `#` comments) with keys
  `base_url` (default `https://api.openai.com/v1`, trailing `/` stripped),
  `model` (default `gpt-4o-mini`), `api_key`. The `OPENAI_API_KEY` environment
  variable wins over `api_key`. Missing key: print the same three lines and
  exit. The same `config.example.properties`.
- **API use.** POST `<base_url>/chat/completions` with `{"model", "messages":
  [{"role", "content"}]}` and `Authorization: Bearer <key>`; the reply is
  `choices[0].message.content`. Non-200: `API returned <status>: <body>`, with
  the two hints: empty body -> `(empty response -- is base_url right? It
  usually ends in /v1)`; body mentions `role 'tool'` -> the Suggestion 3 hint.
  No reply found: `Could not find a reply in: <body>`.
- **Tool results go back as user messages**, never role `tool`. `Message`
  carries the same explanation.
- **Interaction log.** One file per session, `logs/session-<UTC ISO time with
  : replaced by ->.jsonl`; one JSON object per line, exactly the keys `time`
  (UTC ISO 8601), `role`, `content`; first event `session_start` with content
  `model=<model>`, last `session_end` with content `""`; user, assistant and
  error events as in `main`. A log that can't be opened prints `[warn] Could
  not open log file: ...` and the harness runs without logging. The same
  "PLEASE LEAVE THE LOGGING IN PLACE" comment. `logs/` is tracked (with
  `.gitkeep`) and never ignored.
- **Student documents.** `README.md`, `SUGGESTIONS.md` and `REFLECTIONS.md`
  have the same sections, steps, order, difficulty stars, checkpoints, rules
  and jokes, plus the same "Pick your language" section (only the "you are
  here" marker moves). Only the language changes: code names, commands, concept lists,
  example errors, the compile/run tools of Suggestion 5, and the final-boss
  request (*"Code a simple <language> app to play a 2-player game of roll the
  dice that is tested and evaluated."*).
- **Scripts.** `run.sh` and `run.bat` that build (if needed) and run from the
  repository folder, with the toolchain version in a comment.
- **Repository furniture.** `LICENSE` (MIT), the license badge, a `.gitignore`
  that ignores build output and `config.properties` and explains why `logs/`
  isn't ignored, and this file.

## May differ

- File layout and naming, following the language's conventions.
- JSON: Go and Python use the standard library; Rust uses `serde_json`; only
  Java hand-rolls it.
- Error types: exceptions in Java and Python, `error` values in Go, `Result`
  in Rust. The text shown to the student is the same.

## Keep it first-year friendly

The same limits as `main`, translated: plain classes/structs, interfaces or
traits, lists, maps, loops, file I/O. Avoid what a first-year hasn't met yet:
Java streams and lambdas; Python comprehensions, decorators beyond
`@abstractmethod`, and type-hint gymnastics; Go goroutines and channels; Rust
lifetimes, generics, closures and iterator chains where a loop will do.
Comments are written for first-years.

## Porting a change

1. Make and test the change on `main`.
2. For each branch: `git checkout <branch>`, port it by hand, run it against
   a real model (`/quit` straight away is enough for plumbing changes), and
   delete any session logs the test made before committing.
3. Commit on each branch with the same message as on `main`.
