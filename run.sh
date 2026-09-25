#!/bin/sh
# Run BadClaude. Needs Python 3.9 or newer on your PATH.
# There is nothing to compile: Python reads the badclaude/ folder directly.
cd "$(dirname "$0")" || exit 1
python3 -m badclaude
