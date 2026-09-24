# Suggestions: making BadClaude less bad

Work roughly in order — each idea builds on the previous ones. Difficulty is
marked ⭐ (an afternoon) to ⭐⭐⭐ (a project week). Everything here is doable
with what you learned in CS1/CS2: classes, interfaces, lists, maps, loops,
recursion, and file I/O.

Before you build each one, ask BadClaude to do something it currently fails
at, and save that conversation. Afterwards, ask the same thing again. That
before/after pair is your evidence — and it goes in REFLECTIONS.md.

**It's your code now.** The starter code is a starting point, not a framework
you have to fit into. Split methods, add classes, rename things, move code
around, delete what you don't need: all fine, and all expected. Every
improvement adds code, and if you only ever add, `Harness.run()` turns into a
wall of nested `if`s that nobody (including you) can follow. So several steps
below start with **Tidy up first**: reorganise what you have, check that
nothing changed on the outside, commit, and *then* add the new feature.
Commit tidy-ups on their own with a message starting `refactor:`, so your
history shows them.

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
   ask the model again so it can finish its answer. Log the tool call!
   The model remembers nothing, so the second request must contain the whole
   story: add the model's own reply as an **assistant** message, then the
   result as a **user** message, e.g. `TOOL RESULT dice: 4`. The request goes
   from 2 messages (system, user) to 4.

   **Don't use the role `"tool"`** for the result. The API has one, but only
   for its built-in function calling, which we don't use, and it rejects
   BadClaude's request with a 400 error mentioning `tool_calls`. In your
   *log* you can call it whatever you like.

**Watch out:** a weak model will get the format wrong sometimes. What does
your harness do then?

*Concepts: interfaces and polymorphism, string parsing, protocols.*

## 4. The agent loop ⭐⭐⭐ (in four small steps)

**The problem:** One tool call per question isn't enough for real tasks.
"Roll two dice and add them" already needs two calls (or a smarter tool).

**The idea:** You already built one *lap* in step 3: send → the model asks
for a tool → run it → send the result back → print the answer. An agent just
keeps doing laps until the model stops asking for tools. That loop is the
single idea that turns a chatbot into an **agent**. Every real coding
assistant is this loop with better tools.

It is a small change on paper — an `if` becomes a `while` — but it touches
everything you've built so far, so take it in four steps and check each one
before moving on.

### What the loop looks like

Here is one turn, "Roll two dice and add them", lap by lap. `messages` is the
list you send to the model; it grows during the turn and the **whole** list
goes out every lap:

| Lap | You send (new at the end of `messages`) | The model replies | Your harness does |
| --- | --- | --- | --- |
| 1 | `user: Roll two dice and add them` | `TOOL dice: 1d6` | runs dice → `4` |
| 2 | `assistant: TOOL dice: 1d6`<br>`user: TOOL RESULT dice: 4` | `TOOL dice: 1d6` | runs dice → `3` |
| 3 | `assistant: TOOL dice: 1d6`<br>`user: TOOL RESULT dice: 3` | `4 + 3 = 7` | not a tool call → print it, turn over |

(`TOOL RESULT ...` is just the format we picked for sending results back; use
whatever you chose in step 3, as long as it's the same every time.)

### 4a. Tidy up ⭐

This is the first **Tidy up first** of the course. Before you can loop, pull
your step 3 code out of `run()` into methods, e.g.
one that recognises a tool call and splits it into name and input (and says
"not a tool call" otherwise), and one that finds the tool and runs it.

**Checkpoint:** nothing changes on the outside. Your step 3 before/after
conversation still works exactly the same.

### 4b. One turn, many messages ⭐

Inside a turn, keep a `messages` list that starts as system prompt + memory +
the user's message, and add the model's tool request and the tool result to
it, as in the table. Print a trace line for every lap, e.g.
`[lap 1] dice: 1d6 -> 4` — you'll want it when things go wrong.

**Checkpoint:** ask "Roll two dice and add them". You should see lap 1
traced, and then BadClaude most likely prints another `TOOL dice: 1d6` *as its
answer* (or makes up the second roll). That's the bug the next step fixes:
your harness still stops after one tool call. The model is different every
run, so try it a few times.

### 4c. The `if` becomes a `while` ⭐

Keep going round while the reply is a tool call. Add a limit (say
`MAX_LAPS = 5`) and stop with a clear message when you hit it, so a confused
model can't loop forever — or spend all your credit.

**Checkpoint:**
- "Roll two dice and add them": two laps traced, then a final answer whose
  sum matches the rolls in your trace.
- Check the limit: ask for something that needs more laps than `MAX_LAPS`
  (e.g. "roll ten dice, one at a time, and add them"), or lower `MAX_LAPS`
  to 1 for a moment. Your harness should stop with your message.

### 4d. When the model gets it wrong ⭐⭐

Weak models break the format, and they will. Things we've seen a cheap model
do on "roll two dice and add them":

- a missing colon, or a full stop at the end: `TOOL dice: 1d6.`
- **two tool calls in one reply**, one per line;
- a tool that doesn't exist, or an input your tool can't handle.

Don't crash, and don't silently give up. Send the problem back as the tool
result and let the model try again. That self-correction is a big part of
what makes agents work, **but only if the error is written for the model**.
A raw Java exception like `NumberFormatException: For input string: "6."`
teaches it nothing, and it will just repeat itself until your lap limit
stops it. Say what was wrong and what right looks like:
`TOOL RESULT error: dice wants input like 2d6 (no full stop)`, or
`TOOL RESULT error: no tool called teleport; tools are dice, calculator`.

For two calls in one reply you have to decide: run just the first, run them
all, or send back an error asking for one at a time. Any of these can work;
say in your reflection which you picked and why.

**Checkpoint:** provoke it. Ask it to use a tool you don't have ("use the
teleport tool to go to the moon"), ask for dice in a weird way ("roll a
twenty-sided die"), and run "roll two dice and add them" a handful of times.
Every run should end with an answer or your lap-limit message, never a crash.
Then read your log: find a lap where the model got the format wrong, and
check that your error message helped it recover.

### Then push it

Ask for something that needs several different tools. Watch your trace. How
often does the model use the format correctly, and what does it do when it
doesn't?

**Going further:** what should go into `Memory` after a turn — every lap, or
just the question and the final answer? Try both and ask a follow-up question
each way.

*Concepts: refactoring into methods, while loops, termination conditions,
state that grows, error handling as feedback.*

## 5. The code runner tool ⭐⭐⭐

**The problem:** BadClaude can describe a Java program but can't build one.

**The fix:** A tool (or family of tools) that lets the model actually
develop software:

- `write_file`: save model-provided source code to a workspace folder.
- `compile`: run `javac` on the workspace using `ProcessBuilder`, capture
  the output, and return it — **including compile errors**, because feeding
  errors back is exactly how the model fixes its own bugs.
- `run`: execute the compiled program and return what it printed.

**Tidy up first ⭐:** you're about to add three or more tools, and they need
to be listed in the system prompt, looked up by name, and run. If that lives
in `Harness`, it gets crowded. Consider a class of its own (a "toolbox") that
holds the tools, finds one by name, and writes the tool list for the system
prompt, so adding a tool means one new line, not edits in three places.
*Checkpoint:* your step 4 conversations behave exactly as before.

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

**Tidy up first ⭐:** a planner handles one request as *several* small ones,
so "handle one request" needs to be something you can call. Separate the
conversation with the user (read a line, print the answer) from answering one
request (the agent loop from step 4), e.g. a method that takes a request and
returns the final answer. *Checkpoint:* nothing changes on the outside.

**The fix:** For big requests, first ask the model *only* to produce a
numbered plan. Then feed the steps back one at a time, each with the results
so far. Compare against asking for everything at once.

*Concepts: problem decomposition — the same skill you use, now taught to a model.*

## 8. A self-checker ⭐⭐⭐

**The problem:** BadClaude never checks its own work.

**Tidy up first ⭐:** the critic is the same model with a different system
prompt and different instructions. If the code that builds a request and
calls the model is spread around, you'll end up copying it. Gather it into
one place that takes a system prompt and messages, so the generator and the
critic both use it. *Checkpoint:* nothing changes on the outside.

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
