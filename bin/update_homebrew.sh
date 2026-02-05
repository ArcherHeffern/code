#!/usr/bin/env bash

set -euo pipefail

BREW="/opt/homebrew/bin/brew"
command -v $BREW &> /dev/null && { $BREW update && $BREW upgrade; }

