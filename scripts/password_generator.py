#!/usr/bin/env python3.14
from dataclasses import dataclass
from string import ascii_lowercase, ascii_uppercase, digits, punctuation
from sys import exit
from random import choices, shuffle
from pathlib import Path
from random import choices
from argparse import ArgumentParser

WORDS_LIST_FILE = Path("/usr/share/dict/words")


@dataclass
class PasswordConfig:
    length: int
    digit_count: int
    uppercase_count: int
    lowercase_count: int
    special_count: int


def parse_args() -> PasswordConfig:
    parser = ArgumentParser(description="sum the integers at the command line")
    parser.add_argument(
        "-d",
        "--digits",
        type=int,
        help="Mininum number of digits or 0 if disabled",
        default=4,
        metavar="COUNT",
    )
    parser.add_argument(
        "--lowercase",
        type=int,
        help="Mininum number of lowercase characters or 0 if disabled",
        default=4,
        metavar="COUNT",
    )
    parser.add_argument(
        "--uppercase",
        type=int,
        help="Mininum number of uppercase characters or 0 if disabled",
        default=4,
        metavar="COUNT",
    )
    parser.add_argument(
        "-s",
        "--special",
        type=int,
        help="Mininum number of special characters or 0 if disabled",
        default=8,
        metavar="COUNT",
    )
    parser.add_argument(
        "-l",
        "--length",
        type=int,
        help="Length of password",
        default=20,
        metavar="COUNT",
    )
    args = parser.parse_args()
    return PasswordConfig(
        length=args.length,
        digit_count=args.digits,
        lowercase_count=args.lowercase,
        uppercase_count=args.uppercase,
        special_count=args.special,
    )


def generate_password(config: PasswordConfig) -> str:
    password: list[str] = []

    password.extend(choices(ascii_lowercase, k=config.lowercase_count))
    password.extend(choices(ascii_uppercase, k=config.uppercase_count))
    password.extend(choices(digits, k=config.digit_count))
    password.extend(choices(punctuation, k=config.special_count))

    valid_chars = ""
    if config.lowercase_count:
        valid_chars += ascii_lowercase
    if config.uppercase_count:
        valid_chars += ascii_uppercase
    if config.digit_count:
        valid_chars += digits
    if config.special_count:
        valid_chars += punctuation
    password.extend(
        choices(
            valid_chars,
            k=config.length
            - config.lowercase_count
            - config.uppercase_count
            - config.digit_count
            - config.special_count,
        )
    )
    shuffle(password)

    return "".join(password)


if __name__ == "__main__":
    args = parse_args()
    if (
        args.lowercase_count
        + args.uppercase_count
        + args.digit_count
        + args.special_count
        > args.length
    ):
        print("length is shorter than required lowercase+uppercase+digits+special")
        exit(1)
    print(generate_password(args))
