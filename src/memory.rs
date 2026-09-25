use crate::message::Message;

/// What the harness remembers between turns.
///
/// The harness calls add() after every exchange and recall() before every
/// request. What you store, how much of it, and what you give back is
/// entirely up to your implementation.
pub trait Memory {
    /// Called after each message so the memory can store it (or not).
    fn add(&mut self, message: Message);

    /// Returns the messages to include before the newest user message.
    fn recall(&self) -> Vec<Message>;
}
