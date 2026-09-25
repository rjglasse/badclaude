use crate::memory::Memory;
use crate::message::Message;

/// BadClaude's default memory: none. Every message is a fresh start and the
/// model has no idea what was said one turn ago.
///
/// Replacing this struct is Suggestion 1 -- and probably the single biggest
/// improvement you will ever make to BadClaude.
pub struct NoMemory {}

impl Memory for NoMemory {
    fn add(&mut self, _message: Message) {
        // Forget it immediately.
    }

    fn recall(&self) -> Vec<Message> {
        Vec::new()
    }
}
