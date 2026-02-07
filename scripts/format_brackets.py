#!/usr/bin/env python
from io import TextIOBase, TextIOWrapper
from os import access, R_OK
from pathlib import Path
from sys import argv, stderr, exit
from tempfile import NamedTemporaryFile, _TemporaryFileWrapper  # type: ignore
from typing import Optional, Self, Never
from dataclasses import dataclass

USAGE = f"""
{argv[0]} [OPTIONS] files...
    
About:
formats text by balancing parenthesis and removing whitespace. Default destination is `file + .fmt` for each file in files. 

Options:
-d|-delete: Deletes file if any runtime errors occur. -strict is inferred.
-f|-force: Force overwrite destination file. Default Off
-h|-help: Displays this help text
-i|-inplace: Formats files in place. Default Off
-s|-strict: Abort formating a file if any runtime errors occur, but does not delete file. Default Off
-v|-validate: Validates file but does not produce output.

Status Codes:
If multiple errors occur. The highest status code is returned.

0: No failures
10: Argument parsing error
20. File not found error
30: Runtime error
"""

EXIT_OK = 0
EXIT_ERROR_ARGUMENT_PARSING = 10
EXIT_ERROR_FILE_NOT_FOUND = 20
EXIT_ERROR_RUNTIME_ERROR = 30

CHUNK_SIZE = 4096

PAREN_MATCHES: dict[str, str] = {
    "(": ")",
    "[": "]",
    "{": "}",
}


@dataclass
class Options:
    delete: bool
    force_overwrite: bool
    in_place: bool
    input_files: list[Path]
    strict: bool
    validate: bool

    @classmethod
    def parse_args(cls, argv: list[str]) -> Self | Never:
        global __status_code
        opt_delete = False
        opt_force_overwrite = False
        opt_in_place = False
        opt_input_files: list[Path] = []
        opt_strict: bool = False
        opt_validate: bool = False
        args = argv[1:]
        while args:
            arg = args[0]
            if arg in ["-d", "-delete"]:
                opt_delete = True
                opt_strict = True
            elif arg in ["-f", "-force"]:
                opt_force_overwrite = True
            elif arg in ["-h", "-help", "--help"]:
                EXIT(USAGE)
            elif arg in ["-i", "-inplace"]:
                opt_in_place = True
            elif arg in ["-s", "-strict"]:
                opt_strict = True
            elif arg in ["-v", "-validate"]:
                opt_validate = True
            else:
                maybe_file = Path(arg)
                if not maybe_file.is_file():
                    set_status_code(EXIT_ERROR_ARGUMENT_PARSING)
                    EXIT(f"Input file '{arg}' is not a file.")
                if not access(maybe_file, R_OK):
                    set_status_code(EXIT_ERROR_ARGUMENT_PARSING)
                    EXIT(f"Input file '{arg}' is a file, but not readable.")
                opt_input_files.append(maybe_file)
            args = args[1:]

        # Check for validate option conflicts
        if opt_validate:
            if opt_delete or opt_force_overwrite or opt_in_place or opt_strict:
                set_status_code(EXIT_ERROR_ARGUMENT_PARSING)
                EXIT(f"-validate is enabled. Other options are disallowed.")

        return cls(
            delete=opt_delete,
            input_files=opt_input_files,
            force_overwrite=opt_force_overwrite,
            in_place=opt_in_place,
            strict=opt_strict,
            validate=opt_validate,
        )


__status_code: int = EXIT_OK


def set_status_code(status_code: int):
    global __status_code
    __status_code = max(__status_code, status_code)


def EXIT(msg: str):
    if __status_code == 0:
        print(msg)
    else:
        print(msg, file=stderr)
    exit(__status_code)


class FileWriter:
    def __init__(self, file: Optional[TextIOBase]):
        self.f = file

    def write(self, s: str):
        if self.f:
            self.f.write(s)

    def close(self):
        if self.f:
            self.f.close()


def format_file(input_file: TextIOBase, output_file: FileWriter, strict: bool) -> bool:
    indentation = 0
    in_word = False
    line = 1
    char = 1
    ok = True
    while chunk := input_file.read(CHUNK_SIZE):
        i = 0
        while i != len(chunk):
            c = chunk[i]
            if c in "\"'":
                in_word = not in_word
            elif in_word:
                ...
            elif c == "\\":
                i += 1
                char += 1
                output_file.write(c)
                c = chunk[i]
                output_file.write(c)

            if c in "}])":
                indentation -= 1
                if indentation < 0:
                    print(
                        f"Parenthesis are not balanced at {line}:{char} (line:character)"
                    )
                    indentation = 0
                    ok = False
                    if strict:
                        return False
                print_next_line(output_file, indentation)

            if not c.isspace():
                output_file.write(c)

            if c in "{[(":
                # Edge case: I don't want to format () as
                # (
                #
                # )
                # Solution: Find next non space character. If closing, don't bother formatting!
                # This solution won't work if the closing paren is in the next chunk. However, the likeihood and downside makes it not worth fixing.
                temp_i = i + 1
                while temp_i < len(chunk) and chunk[temp_i].isspace():
                    temp_i += 1
                if temp_i >= len(chunk) or PAREN_MATCHES[c] != chunk[temp_i]:
                    indentation += 1
                    print_next_line(output_file, indentation)
                else:
                    i = temp_i
                    output_file.write(PAREN_MATCHES[c])
            elif c == ",":
                print_next_line(output_file, indentation)
            elif c == "\n":
                line += 1
                char = 0
            char += 1

            i += 1

        if indentation != 0:
            set_status_code(EXIT_ERROR_RUNTIME_ERROR)
            print(
                "Indentation should be 0 at end. Parenthesis are not balanced!",
                file=stderr,
            )
            return False
    return ok


def print_next_line(f: FileWriter, indentation: int):
    """Expects indentation >= 0"""
    f.write("\n")  # type: ignore
    f.write("\t" * indentation)  # type: ignore


if __name__ == "__main__":

    options = Options.parse_args(argv)

    for input_file in options.input_files:
        if options.in_place:
            out_f = NamedTemporaryFile("w", delete=False, encoding="utf-8")
        elif options.validate:
            out_f = None
        else:
            output_file = Path(input_file.name + ".fmt")
            if Path(output_file).exists() and not options.force_overwrite:
                print(
                    f"Output file '{output_file}' exists. To force overwrites use the -f|-force flag. Skipping...",
                    file=stderr,
                )
                continue
            out_f = open(output_file, "w", encoding="utf-8")

        fw = FileWriter(out_f)  # type: ignore
        with open(input_file, "r", encoding="utf-8") as in_f:
            success = format_file(in_f, fw, options.strict)  # type: ignore
        fw.close()

        if out_f is None:
            if success:
                print(f"{input_file} is well formed.")
            else:
                print(f"{input_file} is malformed.")
        elif not success and options.strict:
            print(f"Aborted formatting '{input_file}' due to error. ")
            if options.delete:
                Path(out_f.name).unlink()
        elif options.in_place and type(out_f) is _TemporaryFileWrapper:
            Path(out_f.file.name).rename(input_file)
            print(f"Formatted {input_file} in place")
        elif type(out_f) is TextIOWrapper:
            print(f"Formatted {input_file} into {output_file}")  # type: ignore
        else:
            raise Exception("Unreachable!")
    exit(__status_code)
