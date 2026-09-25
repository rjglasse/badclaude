//! BadClaude: a deliberately minimal LLM harness.
//!
//! It works, but it is bad on purpose: it forgets everything, it can't use
//! tools, and it gives up after one reply. Your job is to make it less bad.
//! Start with SUGGESTIONS.md.

// Each `mod` line pulls in one file from src/, e.g. `mod harness;` is
// src/harness.rs.
mod config;
mod harness;
mod interaction_log;
mod llm_client;
mod memory;
mod message;
mod no_memory;
mod tool;

use config::Config;
use harness::Harness;
use interaction_log::InteractionLog;
use llm_client::LlmClient;
use memory::Memory;
use no_memory::NoMemory;

fn main() {
    let config = Config::load();
    if config.api_key.is_empty() {
        println!("No API key found.");
        println!("Set the OPENAI_API_KEY environment variable,");
        println!("or copy config.example.properties to config.properties and fill in api_key.");
        return;
    }

    let llm = LlmClient::new(
        config.base_url.clone(),
        config.api_key.clone(),
        config.model.clone(),
    );
    let log = InteractionLog::new(&config.model);

    // SEAM: BadClaude ships with no memory at all. Swap in your own
    // implementation of the Memory trait (Suggestion 1).
    let memory: Box<dyn Memory> = Box::new(NoMemory {});

    let mut harness = Harness::new(llm, memory, log);
    harness.run();
}
