#!/bin/sh
# Build and run BadClaude. Needs Go 1.21 or newer on your PATH.
cd "$(dirname "$0")" || exit 1
go run .
