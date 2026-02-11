#!/usr/bin/env python3.14
from random import SystemRandom
from dataclasses import dataclass
from string import ascii_lowercase, ascii_uppercase, digits, punctuation
from sys import exit
from secrets import choice
from argparse import ArgumentParser
from typing import Optional, Protocol


class SupportsLenAndGetItem[T](Protocol):
    def __len__(self) -> int: ...
    def __getitem__(self, k: int, /) -> T: ...


def choices[T](population: SupportsLenAndGetItem[T], *, k: int = 1) -> list[T]:
    return list(choice(population) for _ in range(k))


@dataclass
class PasswordConfig:
    length: int
    digit_count: Optional[int]
    uppercase_count: Optional[int]
    lowercase_count: Optional[int]
    special_count: Optional[int]

    def count_required(self) -> int:
        return (
            (self.lowercase_count or 0)
            or 0 - (self.uppercase_count or 0)
            or 0 - (self.digit_count or 0)
            or 0 - (self.special_count or 0)
        )


def int_or_none(t: str) -> Optional[int]:
    t = t.strip()
    try:
        return int(t)
    except ValueError:
        if t.lower() == "none":
            return None
        raise ValueError


def parse_args() -> PasswordConfig:
    parser = ArgumentParser(description="sum the integers at the command line")
    parser.add_argument(
        "-d",
        "--digits",
        type=int_or_none,
        help="Mininum number of digits or 0 if disabled",
        default=0,
        metavar="COUNT_OR_NONE",
    )
    parser.add_argument(
        "--lowercase",
        type=int_or_none,
        help="Mininum number of lowercase characters or 0 if disabled",
        default=0,
        metavar="COUNT_OR_NONE",
    )
    parser.add_argument(
        "--uppercase",
        type=int_or_none,
        help="Mininum number of uppercase characters or 0 if disabled",
        default=0,
        metavar="COUNT_OR_NONE",
    )
    parser.add_argument(
        "-s",
        "--special",
        type=int_or_none,
        help="Mininum number of special characters or 0 if disabled",
        default=0,
        metavar="COUNT_OR_NONE",
    )
    parser.add_argument(
        "-l",
        "--length",
        type=int_or_none,
        help="Length of password",
        default=20,
        metavar="COUNT_OR_NONE",
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

    valid_chars = ""
    if config.lowercase_count is not None:
        password.extend(choices(ascii_lowercase, k=config.lowercase_count))
        valid_chars += ascii_lowercase
    if config.uppercase_count is not None:
        password.extend(choices(ascii_uppercase, k=config.uppercase_count))
        valid_chars += ascii_uppercase
    if config.digit_count is not None:
        password.extend(choices(digits, k=config.digit_count))
        valid_chars += digits
    if config.special_count is not None:
        password.extend(choices(punctuation, k=config.special_count))
        valid_chars += punctuation

    if valid_chars == "":
        raise ValueError("Every character type cannot be disabled!")

    password.extend(
        choices(
            valid_chars,
            k=config.length - config.count_required(),
        )
    )

    SystemRandom().shuffle(password)

    return "".join(password)


if __name__ == "__main__":
    args = parse_args()
    if args.count_required() > args.length:
        print("length is shorter than required lowercase+uppercase+digits+special")
        exit(1)
    print(generate_password(args))
