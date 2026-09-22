#!/usr/bin/env python3.14
from dataclasses import dataclass
from pathlib import Path
from secrets import choice

WORDS_FILE = Path("~/code/scripts/words.txt").expanduser()


@dataclass
class Config:
    min_word_length: int = 3
    min_words: int = 5
    min_length: int = 20


words = WORDS_FILE.read_text().splitlines()


def generate_passphrase(config: Config):
    length = -1
    passphrase: list[str] = []
    while len(passphrase) < config.min_words and length < config.min_length:
        word = ""
        while len(word) < config.min_word_length:
            word = choice(words)
        length += len(word) + 1
        passphrase.append(word)
    return " ".join(passphrase)


def parse_args() -> Config:
    return Config()


def main():
    config = parse_args()
    print(generate_passphrase(config))


if __name__ == "__main__":
    main()
