use crate::interaction_log::InteractionLog;
use crate::llm_client::LlmClient;
use crate::memory::Memory;
use crate::message::Message;
use crate::tool::Tool;
use std::io;
use std::io::Write;

// SEAM: a better system prompt is the cheapest improvement you can make
// (Suggestion 2).
const SYSTEM_PROMPT: &str = "You are BadClaude, a helpful assistant.";

/// The heart of BadClaude: read a line, send it to the model, print the reply.
/// That's it. Every improvement you make plugs in here or replaces a piece
/// this struct uses.
pub struct Harness {
    llm: LlmClient,
    memory: Box<dyn Memory>,
    log: InteractionLog,

    // SEAM: register your tools here and teach the model to ask for them
    // (Suggestions 3 and 4).
    //
    // (Rust warns about anything that is never used. Nothing uses this list
    // yet -- that's your job -- so we tell the compiler that's on purpose.
    // Delete the #[allow] line once your harness uses the tools.)
    #[allow(dead_code)]
    tools: Vec<Box<dyn Tool>>,
}

impl Harness {
    pub fn new(llm: LlmClient, memory: Box<dyn Memory>, log: InteractionLog) -> Harness {
        Harness {
            llm,
            memory,
            log,
            tools: Vec::new(),
        }
    }

    pub fn run(&mut self) {
        println!("BadClaude is listening. Type /quit to exit.");
        println!();

        let stdin = io::stdin();
        loop {
            print!("you> ");
            // print! doesn't show anything until the line ends, unless we
            // flush it ourselves.
            let _ = io::stdout().flush();

            let mut line = String::new();
            match stdin.read_line(&mut line) {
                Ok(0) => break,  // end of input
                Err(_) => break, // can't read input
                Ok(_) => {}
            }
            let input = line.trim().to_string();
            if input.is_empty() {
                continue;
            }
            if input == "/quit" {
                break;
            }

            self.log.record("user", &input);
            let user_message = Message::user(&input);

            // Build the request: system prompt, whatever the memory recalls,
            // then the new user message. With NoMemory, the model sees only
            // the newest message -- try asking it about your previous one!
            let mut request = Vec::new();
            request.push(Message::system(SYSTEM_PROMPT));
            for message in self.memory.recall() {
                request.push(message);
            }
            request.push(user_message.clone());

            let reply = match self.llm.chat(&request) {
                Ok(reply) => reply,
                Err(problem) => {
                    println!("[error] {}", problem);
                    self.log.record("error", &problem);
                    continue;
                }
            };

            self.memory.add(user_message);
            self.memory.add(Message::assistant(&reply));
            self.log.record("assistant", &reply);

            // SEAM: this is where an agent loop would check the reply for a
            // tool request, run the tool, and go back to the model with the
            // result instead of printing straight away (Suggestion 4).
            println!();
            println!("badclaude> {}", reply);
            println!();
        }

        self.log.close();
        println!("Bye!");
    }
}
