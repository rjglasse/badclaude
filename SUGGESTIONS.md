# Suggestions: making BadClaude less bad

Work roughly in order — each idea builds on the previous ones. Difficulty is
marked ⭐ (an afternoon) to ⭐⭐⭐ (a project week). Everything here is doable
with what you learned in CS1/CS2: classes, interfaces, lists, maps, loops,
recursion, and file I/O.

Before you build each one, ask BadClaude to do something it currently fails
at, and save that conversation. Afterwards, ask the same thing again. That
before/after pair is your evidence — and it goes in REFLECTIONS.md.

---

## 1. Short-term memory ⭐

**The problem:** BadClaude forgets each message instantly, because `NoMemory`
throws everything away. The model never "remembers" anything — chat apps
just re-send the whole conversation every time.

**The fix:** Write a `ConversationMemory` class that implements `Memory`,
stores every message in a list, and returns them all in `recall()`. Swap it
in in `Main.java`.

**Going further:** Conversations can get too long (and cost more). Keep only
the last N messages — notice this is queue behaviour. What breaks if N is
too small?

*Concepts: interfaces, `ArrayList`, queues.*

## 2. A real system prompt ⭐

**The problem:** "You are BadClaude, a helpful assistant" gives the model no
useful instructions.

**The fix:** Experiment with `SYSTEM_PROMPT` in `Harness.java`. Give it a
role, rules, output format requirements. Try to make it concise, or refuse
to answer off-topic questions, or always answer in the style of a pirate —
and observe how reliably a *weak* model actually follows instructions.

*Concepts: none new — this one teaches you about the model, not about Java.*

## 3. Tools ⭐⭐

**The problem:** The model can only produce text. It can't calculate, roll
dice, or read a file.

**The fix:** The model can't *run* a tool — but it can *ask* for one, in
text, if you teach it how. Three parts:

1. Implement the `Tool` interface. Good first tools: a **calculator**, a
   **dice roller**, a **file reader**, a **file writer**, a **clock**.
2. Tell the model about them in the system prompt, including the exact
   format to use, e.g.:
   `If you need a tool, reply with exactly one line: TOOL <name>: <input>`
3. In `Harness`, check each reply. If it starts with `TOOL`, parse out the
   name and input, find the matching tool in the `tools` list, run it, and
   send the result back to the model as a new message so it can finish its
   answer. Log the tool call!

**Watch out:** a weak model will get the format wrong sometimes. What does
your harness do then?

*Concepts: interfaces and polymorphism, string parsing, protocols.*

## 4. The agent loop ⭐⭐⭐

**The problem:** One tool call per question isn't enough for real tasks.
"Roll two dice and add them" already needs two calls (or a smarter tool).

**The fix:** Wrap step 3 in a loop: send → if the reply is a tool call, run
it, append the result, go again → stop when the model answers with plain
text. Add a maximum number of steps so a confused model can't loop forever.

This loop is the single idea that turns a chatbot into an **agent**. Every
real coding assistant is this loop with better tools.

*Concepts: while loops, termination conditions, state.*

## 5. The code runner tool ⭐⭐⭐

**The problem:** BadClaude can describe a Java program but can't build one.

**The fix:** A tool (or family of tools) that lets the model actually
develop software:

- `write_file`: save model-provided source code to a workspace folder.
- `compile`: run `javac` on the workspace using `ProcessBuilder`, capture
  the output, and return it — **including compile errors**, because feeding
  errors back is exactly how the model fixes its own bugs.
- `run`: execute the compiled program and return what it printed.

**Safety:** keep everything inside a `workspace/` folder, and put a timeout
on `run` (infinite loops happen).

With this plus the agent loop, the end-of-course request — the tested,
evaluated 2-player dice game — is within reach. Try it!

*Concepts: file I/O, processes, error handling.*

## 6. Long-term memory ⭐⭐

**The problem:** Even with Suggestion 1, everything is gone when the program
exits.

**The fix:** A `notes.txt` the harness reads at startup and includes in the
system prompt, plus a `remember` tool the model can call to append to it.
Tell BadClaude your name today; ask for it tomorrow.

*Concepts: file I/O, persistence.*

## 7. A planner ⭐⭐⭐

**The problem:** Weak models do badly when a task needs many steps at once,
but fine when each step is small.

**The fix:** For big requests, first ask the model *only* to produce a
numbered plan. Then feed the steps back one at a time, each with the results
so far. Compare against asking for everything at once.

*Concepts: problem decomposition — the same skill you use, now taught to a model.*

## 8. A self-checker ⭐⭐⭐

**The problem:** BadClaude never checks its own work.

**The fix:** After producing an answer (or code), make a second API call
asking the model to review it against the original request: "Does this
actually do what was asked? List problems." If problems are found, send them
back for a fix, a bounded number of times. Two calls to the *same* weak
model — generator and critic — beat one call surprisingly often.

*Concepts: loops, invariants, the idea of evaluation.*

## 9. Quality of life ⭐ (anytime)

Pick freely: retry an API call that fails; count tokens/cost per session and
print a total at exit; add `/commands` (like `/clear` for memory, `/tools`
to list tools); colour the output; stream long replies. Small, satisfying,
and they teach you the plumbing.

---

## The final boss

When you have (at least) suggestions 1, 3, 4 and 5, type this into your
harness:

> Code a simple Java app to play a 2-player game of roll the dice that is
> tested and evaluated.

Watch the log. Where does it fail? That failure is your next improvement.
