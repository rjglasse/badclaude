#!/bin/sh
# Compile and run BadClaude. Needs Java 17 or newer on your PATH.
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -d out src/badclaude/*.java || exit 1
java -cp out badclaude.Main "$@"
