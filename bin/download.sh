#!/usr/bin/env bash

if [[ -z $1 ]]; then
	echo "Usage ${0} youtube_video"
	exit 1
fi 

yt-dlp -x --audio-format wav --no-keep-video --audio-quality 0 "$1"
