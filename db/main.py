from pathlib import Path
from shutil import make_archive, unpack_archive
from typing import Optional
import secret_loader




def shred(p: Path):
    p.unlink()


def encrypt_dir(dir: Path, secret_name: str) -> Optional[Path]:
    """
    Encrypts `dir` using secret `secret_name` into file called `dir`.enc

    This keeps the original directory, and shreds all intermediary data created

    Returns
    - Encrypted archive path on success
    - None on error
    """
    if not dir.is_dir():
        return None
    p = Path(make_archive(dir.name, "zip", Path(dir)))

    sym = secret_loader.get_or_create_secret(secret_name, secret_loader.SymmetricKey)

    np = p.with_suffix(".enc")
    np.write_bytes(sym.encrypt(p.read_bytes()))

    shred(p)


def decrypt_dir(dir: Path, secret_name: str) -> Optional[Path]:
    if not str(dir).endswith(".enc"):
        return None
    sec = secret_loader.get_secret(secret_name, secret_loader.SymmetricKey)
    if not sec:
        return None
    zipfile = dir.with_suffix(".zip")
    zipfile.write_bytes(sec.decrypt(secret_loader.SymEncrypted(dir.read_bytes())))
    unpack_archive(zipfile, None, "zip")
    shred(zipfile)

    return dir.with_suffix("")


def main():
    with create_directory_with_data() as d:
        edir = encrypt_dir(d, "db")
    if edir:
        decrypt_dir(edir, "db")


if __name__ == "__main__":
    main()
