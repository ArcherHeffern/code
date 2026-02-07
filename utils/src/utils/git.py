from dataclasses import dataclass, field
from pathlib import Path
from re import Match, search
from subprocess import run
from typing import Optional

from utils.constants import ANSI_CLEAR_FORMATTING, ANSI_UNDERLINE


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
    stash: list[str] = field(default_factory=list[str])

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


def directory_has_changes(path: Path) -> bool: ...


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
        match state[0]:  # type: ignore
            case "A":
                status.staged_new_files.append(file_path)
            case "M":
                status.staged_modified_files.append(file_path)
            case "D":
                status.staged_deleted_files.append(file_path)
        match state[1]:  # type: ignore
            case "M":
                status.unstaged_modified_files.append(file_path)
            case "D":
                status.unstaged_deleted_files.append(file_path)
        match state:  # type: ignore
            case "??":
                status.unstaged_new_files.append(file_path)
            case "!!":
                status.ignored_files.append(file_path)
            case "##":
                m: Optional[Match[str]] = search(r"\[(?:ahead|behind) (\d)\]", filename)
                if m:
                    status.unpushed_commits = int(m.group(1))
    completed_git_stash_list = run(
        ["git", "stash", "list"], capture_output=True, cwd=repo
    )
    for line in completed_git_stash_list.stdout.decode().splitlines():
        status.stash.append(line)

    return status
