#!/bin/sh
# Build (if needed) and run BadClaude. Needs Rust (stable, via rustup) on your PATH.
cd "$(dirname "$0")" || exit 1
cargo run --quiet
