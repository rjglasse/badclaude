# Python runs this file when you type: python3 -m badclaude
from badclaude.main import main

try:
    main()
except KeyboardInterrupt:
    # Ctrl-C: stop quietly instead of printing a scary traceback.
    print()
