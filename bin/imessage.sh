#!/usr/bin/env bash

set -euo pipefail

USAGE=$(cat << EOF

Usage: $0 recipient message

About:
Sends an iMessage to recipient

Example:
$0 a@a 'Hello Frog!'

Notes:
- Emails are case insensitive
- Best bet is sending to iCloud email
- Only works on MacOS/IOS
- (Verify) Make sure email is in your contacts
EOF
)

RECIPIENT="${1:-}"
MESSAGE="${2:-}"

[[ -z $RECIPIENT ]] && { echo >&2 "$USAGE"; exit 1; };
[[ -z $MESSAGE ]] && { echo >&2 "$USAGE"; exit 1; };

osascript -e "tell application \"Messages\" to send \"${MESSAGE}\" to buddy \"${RECIPIENT}\""

