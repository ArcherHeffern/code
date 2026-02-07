from pathlib import Path
from shutil import rmtree
from subprocess import run, CompletedProcess
from utils.common import prompt_yn

HOTSPOTS: list[str] = [
    "/Library/Caches",
    "~/Library/Caches",
    "~/Library/Containers/com.apple.mediaanalysisd/Data/Library/Caches",
]

if __name__ == "__main__":
    resolved_hotspots: list[Path] = [Path(p).expanduser() for p in HOTSPOTS]

    for resolved_hotspot in resolved_hotspots:
        if not resolved_hotspot.exists():
            continue
        completed_process: CompletedProcess[bytes] = run(
            ["du", "-s", "--si", resolved_hotspot], capture_output=True
        )
        stdout = completed_process.stdout.decode().replace("\n", "")
        print(stdout)
        if not prompt_yn("Delete? (y/n) "):
            continue
        rmtree(resolved_hotspot, ignore_errors=True)
        print(f"deleted {resolved_hotspot}")
        print()
