from pathlib import Path
from os import walk
from typing import Optional
from shredder._core import shred as _shred, shred_dir as _shred_dir
from os.path import join


def shred(
    path: Path,
    n_passes: int = 10,
    remove: bool = True,
    size: Optional[int] = None,
    exact: bool = False,
    zero: bool = True,
    verbose: bool = False,
):
    """
    # About
    Shreds a file's contents and name by overwriting it in multiple passes with random bytes and zeros.

    :param path: Which file we are shredding
    :type path: Path
    :param n_passes: How many times we overwrite the target with zero's or random bytes
    :type n_passes: int
    :param remove: Is the file deleted after shredding
    :type remove: bool
    :param size: Specifies how many bytes we want to shred. Otherwise the entire file is shredded.
    :type size: Optional[int]
    :param exact: If true, delete exactly size bytes, otherwise delete size rounded up to the next page table boundary
    :type exact: bool
    :param zero: Do we do an additional pass to zero out the file.
    :type zero: bool
    :param verbose: Does the util print information while executing
    :type verbose: bool
    """
    _shred(path, n_passes, remove, size, exact, zero, verbose)


def shred_dir(
    path: Path,
    n_passes: int = 10,
    remove: bool = True,
    size: Optional[int] = None,
    exact: bool = False,
    zero: bool = True,
    verbose: bool = False,
):
    for dir_path, _, filenames in walk(path, topdown=False):
        for file in filenames:
            f = Path(join(dir_path, file))
            if f.is_dir():
                _shred_dir(f, verbose)
            else:
                shred(
                    f,
                    n_passes,
                    remove,
                    size,
                    exact,
                    zero,
                    verbose,
                )
