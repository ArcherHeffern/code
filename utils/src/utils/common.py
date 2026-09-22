from pathlib import Path
from platform import system
from sys import exit, stderr
from typing import NoReturn, Optional
from enum import Enum, auto
from dotenv import dotenv_values

GLOBAL_DOTENV = dotenv_values(Path("~/code/.env").expanduser())


def eprint(msg: str, red: bool = False):
    if red:
        print(f"\033[91m{msg}\033[0m", file=stderr)
    else:
        print(msg, file=stderr)


def EXIT(msg: str, status_code: int) -> NoReturn:
    if status_code == 0:
        print(msg)
    else:
        print(msg, file=stderr)
    exit(status_code)


def prompt_yn(msg: str) -> bool:
    while True:
        res = input(msg)
        if res in ["y", "yes"]:
            return True
        if res in ["n", "no"]:
            return False


class Platform(Enum):
    MACOS = auto()
    WINDOWS = auto()
    LINUX = auto()
    UNKNOWN = auto()


def get_platform() -> Platform:
    match system():
        case "Windows":
            return Platform.WINDOWS
        case "Linux":
            return Platform.LINUX
        case "Darwin":
            return Platform.MACOS
        case _:
            return Platform.UNKNOWN


def force_get_env(var: str) -> str:
    v = get_env(var)
    if v is None:
        raise ValueError(f"Could not find .env value {var}")
    return v


def get_env(var: str) -> Optional[str]:
    return GLOBAL_DOTENV.get(var)
