from abc import ABC
from dataclasses import dataclass
from enum import Enum, auto
from pathlib import Path
from typing import Callable, Iterable, Optional, TypeAlias


ErrorMsg: TypeAlias = str | None


class GitRepo(str): ...


Src: TypeAlias = Path | GitRepo
Callback: TypeAlias = Callable[["Setting"], ErrorMsg]
Dest: TypeAlias = Path


class Platform(Enum):
    MACOS = "MacOS"
    WINDOWS = "Windows"
    LINUX = "Linux"
    UNKNOWN = "Unknown"


@dataclass
class Movable(ABC):
    src: Src
    dest: Dest


@dataclass
class MoveFile(Movable):
    make_executable: bool = False
    skip_callback_if_no_change: bool = True

    def __post_init__(self):
        if type(self.src) is Path:
            self.src = self.src.expanduser()
        self.dest = self.dest.expanduser()


class DirMoveSetting(Enum):
    GIVE_UP_IF_EXISTS = auto()
    TRY_COMBINE = auto()
    FORCE_COMBINE__DANGEROUS = auto()
    REPLACE__DANGEROUS = auto()


@dataclass
class MoveDir(Movable):
    interpolate: DirMoveSetting = DirMoveSetting.TRY_COMBINE


@dataclass
class Setting:
    name: str
    src_dest_pairs: list[MoveFile | MoveDir]
    callback: Optional[Callback] = None
    platform: Optional[Iterable[Platform]] = None
    final_message: str | None = None
