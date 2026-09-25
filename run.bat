@echo off
rem Run BadClaude. Needs Python 3.9 or newer on your PATH.
rem There is nothing to compile: Python reads the badclaude\ folder directly.
rem
rem The installer from python.org adds the "py" launcher, which works even if
rem you forgot to tick "Add python.exe to PATH". If py is missing (e.g. Python
rem from the Microsoft Store), we fall back to "python".
cd /d "%~dp0"
where py >nul 2>nul
if errorlevel 1 (
    python -m badclaude
) else (
    py -3 -m badclaude
)
