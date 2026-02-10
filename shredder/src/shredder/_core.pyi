from pathlib import Path
from typing import Optional

def shred(
    path: Path,
    n_passes: int,
    remove: bool,
    size: Optional[int],
    exact: bool,
    zero: bool,
    verbose: bool,
) -> bool: ...
