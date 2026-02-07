#!/usr/bin/env bash

# Encrypt and decrypt database
# Create a git hook

set -euo pipefail 

P="$HOMEBREW_PREFIX/opt/neo4j/libexec/conf/neo4j.conf"
LINE="server.directories.data=$HOME/code/db/generated/database"

sed -i .bak 's|^\w*\(server.directories.data=\).*|'"$LINE|" $P

if grep -q $LINE $P;
then
    echo "Replaced inline"
else
    echo "$LINE" >> $P
    echo "Appended to file"
fi

brew services restart neo4j
