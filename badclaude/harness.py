from badclaude.message import Message

# SEAM: a better system prompt is the cheapest improvement you can make
# (Suggestion 2).
SYSTEM_PROMPT = "You are BadClaude, a helpful assistant."


class Harness:
    """
    The heart of BadClaude: read a line, send it to the model, print the reply.
    That's it. Every improvement you make plugs in here or replaces a piece
    this class uses.
    """

    def __init__(self, llm, memory, log):
        self.llm = llm
        self.memory = memory
        self.log = log

        # SEAM: register your tools here and teach the model to ask for them
        # (Suggestions 3 and 4).
        self.tools = []  # a list of Tool objects

    def run(self):
        print("BadClaude is listening. Type /quit to exit.")
        print()

        while True:
            try:
                user_input = input("you> ").strip()
            except EOFError:
                # End of input (e.g. Ctrl-D, or the end of a piped file).
                break
            if user_input == "":
                continue
            if user_input == "/quit":
                break

            self.log.record("user", user_input)
            user_message = Message.user(user_input)

            # Build the request: system prompt, whatever the memory recalls,
            # then the new user message. With NoMemory, the model sees only
            # the newest message -- try asking it about your previous one!
            request = []
            request.append(Message.system(SYSTEM_PROMPT))
            for message in self.memory.recall():
                request.append(message)
            request.append(user_message)

            try:
                reply = self.llm.chat(request)
            except Exception as e:
                # Some exceptions (e.g. "can't connect") have no message, only a type.
                problem = str(e)
                if problem == "":
                    problem = type(e).__name__
                print("[error] " + problem)
                self.log.record("error", problem)
                continue

            self.memory.add(user_message)
            self.memory.add(Message.assistant(reply))
            self.log.record("assistant", reply)

            # SEAM: this is where an agent loop would check the reply for a
            # tool request, run the tool, and go back to the model with the
            # result instead of printing straight away (Suggestion 4).
            print()
            print("badclaude> " + reply)
            print()

        self.log.close()
        print("Bye!")
