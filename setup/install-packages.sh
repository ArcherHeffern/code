#!/usr/bin/env bash

set -euo pipefail

CROSS_PLATFORM_PACKAGES=('sl' 'bat' 'tree' 'jq' 'fzf' 'cloc' 'ffmpeg' 'imagemagick' 'yt-dlp' 'neofetch' 'zoxide' 'tmux' 'npm')
MACOS_PACKAGES=('fd' 'ggrep' 'openjdk')
LINUX_PACKAGES=('fd-find' 'net-tools' 'zip' 'zoxide' 'default-jre' 'default-jdk' 'presenterm')

if [[ "$(uname -s)" == "Darwin" ]]; then
	# TODO: Check if brew is installed and install if not
	brew install "${CROSS_PLATFORM_PACKAGES[@]}"
	brew install "${MACOS_PACKAGES[@]}"
else
	sudo apt-get update -y && sudo apt-get upgrade -y
	sudo apt install "${CROSS_PLATFORM_PACKAGES[@]}" -y
	sudo apt install "${LINUX_PACKAGES[@]}" -y
fi

# ============
# Special Installations
# ============

# Vundle
if [[ ! -d ~/.vim/bundle/Vundle.vim ]]; then
	git clone https://github.com/VundleVim/Vundle.vim.git ~/.vim/bundle/Vundle.vim
fi 

# ============
# Setup
# ============
git config --global user.email "heffernarcher@gmail.com"
git config --global user.name "Archer Heffern"
