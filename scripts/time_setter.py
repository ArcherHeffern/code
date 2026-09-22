from datetime import datetime
import os
from pathlib import Path
from sys import argv


def set_time(f: Path):
    times = f.name.split("_", maxsplit=1)[0]
    day, month, year = times.split(":")
    day, month, year = int(day), int(month), int(year)
    set_macos_creation_date(f, datetime(year=year, month=month, day=day))


def set_macos_creation_date(file_path: Path, target_date: datetime):
    timestamp = target_date.timestamp()
    os.utime(file_path, (timestamp, timestamp))


if __name__ == "__main__":
    file = Path(argv[1])
    set_time(file)
