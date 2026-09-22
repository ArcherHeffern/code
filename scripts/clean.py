from pathlib import Path
from shredder import shred
from glob import glob
from pprint import pprint

FILES = [
    "~/.bash_history*",
    "~/.zsh_history*",
    "~/.bash_sessions/",
    "~/.Trash",
    "~/.sqlite_history*",
    "~/.python_history*",
]

for file in FILES:
    file = Path(file).expanduser()
    pprint(glob(str(file)))
