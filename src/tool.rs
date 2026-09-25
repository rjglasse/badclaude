/// Something the model can ask the harness to do in the real world:
/// calculate, roll dice, read a file, compile some Rust...
///
/// The model itself can only produce text, so you also need a protocol:
/// tell it (in the system prompt) how to ask for a tool, spot that request
/// in its reply, run the tool, and send the result back. See Suggestion 3.
//
// (Nothing calls these methods yet, and Rust warns about unused code. The
// #[allow] line says that's on purpose; delete it once your harness runs tools.)
#[allow(dead_code)]
pub trait Tool {
    /// Short name the model uses to call this tool, e.g. "calculator".
    fn name(&self) -> String;

    /// One line telling the model what this tool does and what input it wants.
    fn description(&self) -> String;

    /// Runs the tool on the given input and returns the result as text.
    fn run(&self, input: &str) -> String;
}
