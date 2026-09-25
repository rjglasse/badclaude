import os


class Config:
    """
    Settings for the harness, read from config.properties in the folder you
    run from. The API key can also come from the OPENAI_API_KEY environment
    variable, which takes priority -- and keeps keys out of git.
    """

    def __init__(self, base_url, model, api_key):
        self.base_url = base_url
        self.model = model
        self.api_key = api_key

    @staticmethod
    def load():
        props = Config.read_properties("config.properties")

        base_url = props.get("base_url", "https://api.openai.com/v1").strip()
        if base_url.endswith("/"):
            base_url = base_url[:-1]
        model = props.get("model", "gpt-4o-mini").strip()

        api_key = os.environ.get("OPENAI_API_KEY")
        if api_key is None or api_key.strip() == "":
            api_key = props.get("api_key", "")

        return Config(base_url, model, api_key.strip())

    @staticmethod
    def read_properties(filename):
        """
        Reads a file of key=value lines into a dictionary. Blank lines and
        lines starting with # are skipped.
        """
        props = {}
        try:
            with open(filename, encoding="utf-8") as file:
                for line in file:
                    line = line.strip()
                    if line == "" or line.startswith("#"):
                        continue
                    if "=" not in line:
                        continue
                    parts = line.split("=", 1)
                    props[parts[0].strip()] = parts[1].strip()
        except OSError:
            # No config file is fine if the environment provides the key.
            pass
        return props
