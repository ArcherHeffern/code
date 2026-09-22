
from contextlib import contextmanager
from pathlib import Path
from shutil import rmtree


@contextmanager
def create_directory_with_data(path: Path = Path("./test_dir")):
    """
    TODO: Does not handle path already existing well
    """
    try:
        if path.exists():
            raise FileExistsError(f"{str(path)} exists.")
        path.mkdir(parents=True)
        (path / "a").mkdir()
        (path / "a" / "b").touch()
        (path / "b").touch()
        (path / "c").mkdir()
        (path / "c" / "b").mkdir()
        (path / "c" / "b" / "a").touch()

        with open((path / "c" / "b" / "a"), "w") as f:
            f.write("Hello world how are you doing")
        with open(path / "a" / "b", "wb") as f:
            f.write(b"Goodbye world!")
        yield path
    finally:
        rmtree(path)