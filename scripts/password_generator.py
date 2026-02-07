#!/usr/bin/env python
from string import ascii_lowercase, ascii_uppercase, digits, punctuation
from sys import argv, exit
from pathlib import Path
from random import choices
from typing import Iterable, Optional

USAGE = """
password_generator.py [password_length]

About: 
Generates a password. Requires at least one lowercase, uppercase, digit, and punctuation each.
- Default length = 25
- Regenerates password if a word in your local unix words list strictly longer than 4 characters appears
- password_length MUST be greater or equal to 4
- valid characters := ascii_letters + digits + punctuation
"""
valid_characters = ascii_lowercase + ascii_uppercase + digits + punctuation
WORDS_LIST_FILE = Path("/usr/share/dict/words")
MIN_PASSWORD_LENGTH = 4  # Must be min 4 because we need 1 lowercase, 1 uppercase, 1 digit, and 1 punctuation character


def has_intersection[T](s: set[T], i: Iterable[T]) -> bool:
    return len(s.intersection(i)) > 0


def read_words_file(filename: Path, filter_length: Optional[int]) -> list[str]:
    """
    Args:
        filename (Path): Words list file
        filter_length (Optional[int]): Remove words where len(word) <= filter_length

    Returns:
        list[str]: Words in words list
    """
    words: list[str] = []
    if not filename.is_file():
        return words
    with open(filename) as f:
        for line in f:
            cleaned_line = line.strip()
            if filter_length and len(cleaned_line) <= filter_length:
                continue
            words.append(cleaned_line)
    return words


def generate_password(length: int) -> str:
    return "".join(choices(valid_characters, k=length))


def valid_password(password: str) -> bool:
    password_set: set[str] = set(password)
    if not has_intersection(password_set, ascii_lowercase):
        return False
    if not has_intersection(password_set, ascii_uppercase):
        return False
    if not has_intersection(password_set, digits):
        return False
    if not has_intersection(password_set, punctuation):
        return False
    return True


def password_contains_word(password: str, words: list[str]) -> bool:
    for word in words:
        if word in password:
            return True
    return False


if __name__ == "__main__":
    opt_password_length = 25
    if len(argv) == 2:
        if argv[1] in ["-h", "-help", "--help"]:
            print(USAGE)
            exit(0)
        if not argv[1].isdigit():
            print(f"Expected password length to be digit but found '{argv[1]}'.")
            exit(1)
        opt_password_length = int(argv[1])
    if opt_password_length < 4:
        print(f"Expected password_length >= 4")
        exit(1)
    words = read_words_file(WORDS_LIST_FILE, 4)
    while True:
        password = generate_password(opt_password_length)
        if valid_password(password) and not password_contains_word(password, words):
            break
    print(password)
