from os import mkdir, walk
from pathlib import Path
from shutil import copy
from subprocess import run
from src.install_types import (
    ErrorMsg,
    GitRepo,
    MoveFile,
    Setting,
    Platform,
)
from src.install_utils import get_effective_user_id, exists_on_path, prompt_yn

# TODO: Check if self is synced with remote
CHOME = Path("~/code").expanduser()

# Util to only compile if there are differences
# - Check if there are current changed files in a directory
# - Check if there were any changes to target dir in diff history within a timezone
# - We will need to keep track of instances of recompiling


def create_neo4j(_: Setting) -> ErrorMsg: 
    symlink_data = "ln -s /opt/homebrew/var/neo4j/data /Users/archerheffern/code/db/database/data"
    ...


def build_client(_: Setting) -> ErrorMsg:
    if prompt_yn("Compile Client? "):
        src = Path("../client").resolve()
        run(["npm", "run", "build"], cwd=src)


def create_bunnylol_daemon(_: Setting) -> ErrorMsg:
    src = Path("../bunnylol.rs/target/release/bunnylol")
    dest = Path(CHOME / "bin" / "generated" / "bunnylol").expanduser()
    # Is rustc downloaded
    if not exists_on_path("cargo"):
        return "Count not find cargo in PATH"

    if src.is_file():
        if prompt_yn("Recompile bunnylol? "):
            # Compile bunnylol
            run(
                [
                    "cargo",
                    "install",
                    "--path",
                    ".",
                    "--features",
                    "server",
                    "--no-default-features",
                ],
                cwd="../bunnylol.rs",
            )

    # Copy bunnylol to destination
    if dest.is_file():
        dest.unlink()
    elif dest.exists():
        return f"{dest} exists and isn't a file"
    copy(src, dest)

    bunnylol_job_path = Path("~/Library/LaunchAgents/archer.bunnylol.daemon.plist")
    return run_launch_agent(bunnylol_job_path)


def create_file_hosting_daemon(_: Setting) -> ErrorMsg:
    file_hosting_job_path = Path("~/Library/LaunchAgents/archer.filehost.daemon.plist")
    return run_launch_agent(file_hosting_job_path)


def start_update_homebrew_cron_job(_: Setting) -> ErrorMsg:
    homebrew_job_path = Path("~/Library/LaunchAgents/archer.homebrew.update.plist")
    return run_launch_agent(homebrew_job_path)


def start_server_backend_daemon(_: Setting) -> ErrorMsg:
    mtapi_job_path = Path("~/Library/LaunchAgents/archer.server.daemon.plist")
    return run_launch_agent(mtapi_job_path)


def postprocess_daemons(_: Setting) -> ErrorMsg:
    d = CHOME / "setup" / "daemon_configs"
    for dp, dn, fn in walk(d):
        for f in fn:
            src = Path(dp) / f
            dest = Path(d) / "generated" / f
            dn[:] = [d for d in dn if d != "generated"]
            print(src, dest)
            with open(src, "r") as rd, open(dest, "w") as wt:
                s = rd.read()
                s = s.replace("$CHOME", str(CHOME))
                s = s.replace("$HOME", str(Path.home()))
                wt.write(s)
    return None


def run_launch_agent(p: Path) -> ErrorMsg:
    p = p.expanduser()

    # Validate Config
    if run(["plutil", "-lint", p]).returncode != 0:
        return f"{p} is an invalid config"

    # Turn agent off if it's on
    effective_user_id = get_effective_user_id()
    if effective_user_id is None:
        return "Failed to get effective user id"
    run(
        [
            "launchctl",
            "bootout",
            f"gui/{effective_user_id}",
            str(p.expanduser()),
        ]
    )  # Don't check if success

    # Run agent
    if (
        run(
            [
                "launchctl",
                "bootstrap",
                f"gui/{effective_user_id}",
                str(p.expanduser()),
            ]
        ).returncode
        != 0
    ):
        return "Failed to bootstrap."


CODE_HOME = Path("~/code").expanduser()
BUILD_DIR = CODE_HOME / "setup"


def create_build_directories(_: Setting):
    cloned_dir = BUILD_DIR / "cloned"
    generated_dir = BUILD_DIR / "generated"
    generated_binaries = CODE_HOME / "bin" / "generated"
    generated_daemon_configs = BUILD_DIR / "daemon_configs" / "generated"

    for path in [
        cloned_dir,
        generated_dir,
        generated_binaries,
        generated_daemon_configs,
    ]:
        f = path.expanduser()
        if f.exists():
            continue
        mkdir(f, mode=0o777)


settings: list[Setting] = [
    Setting(
        "Create build directories",
        [],
        create_build_directories,
    ),
    Setting(
        "Postprocess daemons",
        [],
        postprocess_daemons,
    ),
    Setting(
        "vimrc",
        [
            MoveFile(Path("./dotfiles/.vimrc"), Path("~/.vimrc")),
            MoveFile(Path("./colors"), Path("~/.vim/colors/")),
        ],
        final_message="Launch vim and run :PluginInstall",
    ),
    Setting(
        "zsh and bash",
        [
            MoveFile(
                Path("dotfiles/.archer_profile"),
                Path("~/.archer_profile"),
            ),
            MoveFile(
                Path("dotfiles/.zprofile"),
                Path("~/.zprofile"),
            ),
            MoveFile(
                Path("dotfiles/.bash_profile"),
                Path("~/.bash_profile"),
            ),
        ],
        final_message="restart your terminal to run .profile files.",
    ),
    Setting(
        "tmux",
        [
            MoveFile(
                Path("dotfiles/.tmux.conf"),
                Path("~/.tmux.conf"),
            )
        ],
    ),
    Setting(
        "AScripts",
        [
            MoveFile(
                GitRepo("https://github.com/archerheffern/AScripts"),
                Path("~/code/AScripts/"),
            )
        ],
    ),
    Setting(
        "Intellij Idea Vim Config",
        [
            MoveFile(
                Path("dotfiles/.ideavimrc"),
                Path("~/.ideavimrc"),
            )
        ],
    ),
    Setting(
        "Update Homebrew Cron Job",
        [
            MoveFile(
                Path("daemon_configs/generated/archer.homebrew.update.plist"),
                Path("~/Library/LaunchAgents/archer.homebrew.update.plist"),
            ),
        ],
        start_update_homebrew_cron_job,
        [Platform.MACOS],
    ),
    Setting(
        "Run bunnylol Daemon",
        [
            MoveFile(
                Path("daemon_configs/generated/archer.bunnylol.daemon.plist"),
                Path("~/Library/LaunchAgents/archer.bunnylol.daemon.plist"),
                skip_callback_if_no_change=False,
            ),
        ],
        create_bunnylol_daemon,
        [Platform.MACOS],
    ),
    Setting(
        "Build Client",
        [],
        build_client,
    ),
    Setting(
        "Start Server Daemon",
        [
            MoveFile(
                Path("daemon_configs/generated/archer.server.daemon.plist"),
                Path("~/Library/LaunchAgents/archer.server.daemon.plist"),
                skip_callback_if_no_change=False,
            ),
        ],
        start_server_backend_daemon,
        [Platform.MACOS],
    ),
    Setting(
        "Run file hosting daemon",
        [
            MoveFile(
                Path("daemon_configs/generated/archer.filehost.daemon.plist"),
                Path("~/Library/LaunchAgents/archer.filehost.daemon.plist"),
                skip_callback_if_no_change=False,
            ),
        ],
        create_file_hosting_daemon,
        [Platform.MACOS],
    ),
    Setting(
        "Setup neo4j",
        [],
        create_neo4j,
    ),
]
