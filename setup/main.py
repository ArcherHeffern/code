from os import getcwd
from pathlib import Path
from src.install import install


if __name__ == "__main__":
    # Verify this has been cloned into the correct directory
    whereami = Path("~").expanduser() / "code" / "setup"
    if Path(getcwd()) != whereami:
        raise Exception(
            f"`setup` expects to be at {str(getcwd())} but found {str(whereami)}"
        )
    install()
