from pathlib import Path
import shutil
from typing import TextIO


def exists_on_path(p: str | Path) -> bool:
    return bool(shutil.which(p))


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
