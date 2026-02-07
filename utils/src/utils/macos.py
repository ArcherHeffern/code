from subprocess import run
from typing import Optional

from utils.common import Platform, get_platform


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
