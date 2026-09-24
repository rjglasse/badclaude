@echo off
rem Compile and run BadClaude. Needs Java 17 or newer on your PATH.
cd /d "%~dp0"
if not exist out mkdir out
javac -d out src\badclaude\*.java
if errorlevel 1 exit /b 1
java -cp out badclaude.Main %*
