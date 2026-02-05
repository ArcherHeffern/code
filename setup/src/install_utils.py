from dataclasses import dataclass, field
from re import search, Match
from pathlib import Path
from platform import system
import shutil
from subprocess import run
from sys import stderr
from typing import Optional, TextIO
from src.install_types import Platform

ANSI_UNDERLINE = "\033[4m"
ANSI_CLEAR_FORMATTING = "\033[0m"


def exists_on_path(p: str | Path) -> bool:
    return bool(shutil.which(p))


def prompt_yn(msg: str) -> bool:
    while True:
        r = input(msg)
        if r in ["y", "yes"]:
            return True
        if r in ["n", "no"]:
            return False


def get_platform() -> "Platform":
    match system():
        case "Windows":
            return Platform.WINDOWS
        case "Linux":
            return Platform.LINUX
        case "Darwin":
            return Platform.MACOS
        case _:
            return Platform.UNKNOWN


def get_effective_user_id() -> Optional[int]:
    if get_platform() != Platform.MACOS:
        return None

    a = run(["id", "-u"], capture_output=True)
    if a.returncode != 0:
        return None
    try:
        return int(a.stdout.decode())
    except:
        return None


@dataclass
class GitStatus:
    unpushed_commits: int = 0
    unstaged_new_files: list[Path] = field(default_factory=list[Path])
    unstaged_modified_files: list[Path] = field(default_factory=list[Path])
    unstaged_deleted_files: list[Path] = field(default_factory=list[Path])
    staged_new_files: list[Path] = field(default_factory=list[Path])
    staged_modified_files: list[Path] = field(default_factory=list[Path])
    staged_deleted_files: list[Path] = field(default_factory=list[Path])
    ignored_files: list[Path] = field(default_factory=list[Path])
    stash: list[str] = field(default_factory=list)

    def synced_with_remote(self) -> bool:
        return not (
            self.unpushed_commits
            or self.unstaged_new_files
            or self.unstaged_modified_files
            or self.unstaged_deleted_files
            or self.staged_new_files
            or self.staged_modified_files
            or self.staged_deleted_files
            or self.ignored_files
            or self.stash
        )

    def __str__(self) -> str:
        return f"""\
{ANSI_UNDERLINE}Unpushed commits:{ANSI_CLEAR_FORMATTING} {self.unpushed_commits}
{ANSI_UNDERLINE}Untracked files:{ANSI_CLEAR_FORMATTING}
{"\n".join(str(f) for f in self.unstaged_new_files)}
{ANSI_UNDERLINE}Modified files:{ANSI_CLEAR_FORMATTING}
{"\n".join(str(f) for f in self.unstaged_modified_files)}
{ANSI_UNDERLINE}Deleted files:{ANSI_CLEAR_FORMATTING}
{"\n".join(str(f) for f in self.unstaged_deleted_files)}
{ANSI_UNDERLINE}Staged new files:{ANSI_CLEAR_FORMATTING}
{"\n".join(str(f) for f in self.staged_new_files)}
{ANSI_UNDERLINE}Staged modified files:{ANSI_CLEAR_FORMATTING}
{"\n".join(str(f) for f in self.staged_modified_files)}
{ANSI_UNDERLINE}Staged deleted files:{ANSI_CLEAR_FORMATTING}
{"\n".join(str(f) for f in self.staged_deleted_files)}
{ANSI_UNDERLINE}Ignored files:{ANSI_CLEAR_FORMATTING}
{"\n".join(str(f) for f in self.ignored_files)}
{ANSI_UNDERLINE}Stash files:{ANSI_CLEAR_FORMATTING}
{"\n".join(str(f) for f in self.stash)}"""


def git_status(repo: Path) -> Optional[GitStatus]:
    status = GitStatus()
    completed_git_status = run(
        ["git", "status", "--porcelain=v1", "--ignored", "-b", "--show-stash"],
        capture_output=True,
        cwd=repo,
    )
    if completed_git_status.returncode == 128:  # Not a git repository
        return None
    for line in completed_git_status.stdout.decode().splitlines():
        state = line[0:2]
        filename = line[3:]
        file_path = (repo / filename).resolve()
        match state[0]:
            case "A":
                status.staged_new_files.append(file_path)
            case "M":
                status.staged_modified_files.append(file_path)
            case "D":
                status.staged_deleted_files.append(file_path)
        match state[1]:
            case "M":
                status.unstaged_modified_files.append(file_path)
            case "D":
                status.unstaged_deleted_files.append(file_path)
        match state:
            case "??":
                status.unstaged_new_files.append(file_path)
            case "!!":
                status.ignored_files.append(file_path)
            case "##":
                m: Optional[Match] = search(r"\[(?:ahead|behind) (\d)\]", filename)
                if m:
                    status.unpushed_commits = int(m.group(1))
    completed_git_stash_list = run(
        ["git", "stash", "list"], capture_output=True, cwd=repo
    )
    for line in completed_git_stash_list.stdout.decode().splitlines():
        status.stash.append(line)

    return status


def have_same_file_contents(src: Path, dest: Path) -> bool:
    with (
        open(src, "r", encoding="utf-8") as s_f,
        open(dest, "r", encoding="utf-8") as d_f,
    ):
        return _have_same_file_contents(s_f, d_f)


def _have_same_file_contents(src: TextIO, dest: TextIO) -> bool:
    try:
        for s_line, d_line in zip(src, dest, strict=True):
            if s_line != d_line:
                return False
        return True
    except ValueError:
        return False


def have_same_directory_contents(src: Path, dest: Path) -> bool:
    try:
        for a, b in zip(src.walk(), dest.walk(), strict=True):
            for dir_group_a_entry, dir_group_b_entry in zip(a[1], b[1], strict=True):
                if dir_group_a_entry != dir_group_b_entry:
                    return False
            for file_group_a_entry, file_group_b_entry in zip(a[2], b[2], strict=True):
                if file_group_a_entry != file_group_b_entry:
                    return False
                if not have_same_file_contents(
                    a[0] / file_group_a_entry, b[0] / file_group_b_entry
                ):
                    return False
    except ValueError:
        return False
    return True


def eprint(msg: str, red=False):
    if red:
        print(f"\033[91m{msg}\033[0m", file=stderr)
    else:
        print(msg, file=stderr)


if __name__ == "__main__":
    print(git_status(Path(".")))
