@echo off
rem Build (if needed) and run BadClaude. Needs Rust (stable, via rustup) on your PATH.
cd /d "%~dp0"
cargo run --quiet
