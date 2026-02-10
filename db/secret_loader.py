from typing import NewType, Optional, Type
from cryptography.hazmat.backends import default_backend
from cryptography.fernet import Fernet
from cryptography.hazmat.primitives import serialization
from pathlib import Path
from utils.crypto import (
    EncryptionPublicKey,
    EncryptionPrivateKey,
    SigningPrivateKey,
    SigningPublicKey,
)
from utils.crypto import (
    SigningKeys,
    EncryptionKeys,
    create_key_pair_for_encryption,
    create_key_pair_for_signing,
)
from cryptography.hazmat.primitives.serialization import load_pem_private_key
from cryptography.hazmat.primitives.asymmetric import rsa


SymEncrypted = NewType("SymEncrypted", bytes)


class SymmetricKey:
    @staticmethod
    def create_symmetric_key() -> SymmetricKey:
        k = Fernet.generate_key()
        return SymmetricKey(k)

    def __init__(self, k: bytes):
        """
        Use create_symmetric_key to instantiate this class
        """
        self.k = k
        self.key = Fernet(k)

    def encrypt(self, b: bytes) -> SymEncrypted:
        return SymEncrypted(self.key.encrypt(b))

    def decrypt(self, msg: SymEncrypted) -> bytes:
        return self.key.decrypt(msg)


SECRETS_PATH = Path("~/code/secrets/").expanduser()
SECRETS_PATH.mkdir(parents=True, exist_ok=True)

KeyTypes = SymmetricKey | SigningKeys | EncryptionKeys
secrets: dict[tuple[str, type[KeyTypes]], KeyTypes] = {}

SYMMETRIC_KEY_EXTENSION = ".symmetric"
ENCRYPTION_KEY_EXTENSION = ".encryption"
SIGNING_KEY_EXTENSION = ".signing"


def __type_to_extension(t: type[KeyTypes]) -> Optional[str]:
    if t is SymmetricKey:
        return SYMMETRIC_KEY_EXTENSION
    elif t is EncryptionKeys:
        return ENCRYPTION_KEY_EXTENSION
    elif t is SigningKeys:
        return SIGNING_KEY_EXTENSION
    else:
        return None


def create_secret(name: str, t: KeyTypes) -> bool:
    """
    Returns if the key was created
    """
    if (name, type(t)) in secrets:
        return False
    secrets[name, type(t)] = t
    ext = __type_to_extension(type(t))
    if not ext:
        return False
    f = SECRETS_PATH / (name + ext)
    if isinstance(t, SymmetricKey):
        f.touch()
        f.write_bytes(t.k)
        return True
    f.write_bytes(
        t.priv.private_bytes(
            serialization.Encoding.PEM,
            format=serialization.PrivateFormat.TraditionalOpenSSL,
            encryption_algorithm=serialization.NoEncryption(),
        )
    )
    return True


def delete_secret(name: str, t: type[KeyTypes]):
    if (name, t) not in secrets:
        return False
    ext = __type_to_extension(t)
    if not ext:
        return False
    p = SECRETS_PATH / (name + ext)
    shred(p)
    del secrets[(name, t)]


def __load_secret_by_path(p: Path) -> bool:
    if not p.is_file():
        return False
    elif p.suffix == SYMMETRIC_KEY_EXTENSION:
        secrets[(p.stem, SymmetricKey)] = SymmetricKey(p.read_bytes())
        return True
    elif p.suffix not in [ENCRYPTION_KEY_EXTENSION, SIGNING_KEY_EXTENSION]:
        return False

    priv = load_pem_private_key(p.read_bytes(), None, default_backend())

    if not isinstance(priv, rsa.RSAPrivateKey):
        return False

    if p.suffix == ENCRYPTION_KEY_EXTENSION:
        k = EncryptionKeys(
            EncryptionPublicKey(priv.public_key()), EncryptionPrivateKey(priv)
        )
        secrets[(p.stem, EncryptionKeys)] = k
        return True
    elif p.suffix == SIGNING_KEY_EXTENSION:
        k = SigningKeys(SigningPublicKey(priv.public_key()), SigningPrivateKey(priv))
        secrets[(p.stem, SigningKeys)] = k
        return True
    raise ValueError("Unreachable")


def get_secret[T: KeyTypes](name: str, t: Type[T]) -> Optional[T]:
    """
    Attempts to fetch secret with matching name and type. Returns None if it doesn't exist
    """
    key = secrets.get((name, t))
    if key is None:
        return None
    if not isinstance(key, t):
        return None
    return key


def get_or_create_secret[T: KeyTypes](name: str, t: Type[T]) -> T:
    """
    Attempts to fetch secret with matching name and type. Creates the secret if it doesn't already exist
    """
    if s := get_secret(name, t):
        return s
    if t is SymmetricKey:
        sec = SymmetricKey.create_symmetric_key()
    elif t is EncryptionKeys:
        sec = create_key_pair_for_encryption()
    elif t is SigningKeys:
        sec = create_key_pair_for_signing()
    else:
        raise ValueError(f"get_or_create_secret: Unexpected secret type {t}")

    if not create_secret(name, sec):
        raise Exception(f"get_or_create_secret: Failed to create secret")
    return sec  # type: ignore


# ============
# Load Keys from secrets path
# ============

for p in SECRETS_PATH.iterdir():
    __load_secret_by_path(p)

if __name__ == "__main__":
    sym = SymmetricKey.create_symmetric_key()
    enc = create_key_pair_for_encryption()
    sign = create_key_pair_for_signing()
    create_secret("Si", enc)
    print(get_secret("Si", SigningKeys))
    print(get_secret("Si", EncryptionKeys))
