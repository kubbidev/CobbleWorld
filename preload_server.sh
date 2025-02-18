#!/usr/bin/env sh

# Remove server.jar if it exists
rm -f server.jar

# Download the new server.jar
curl -o server.jar https://meta.fabricmc.net/v2/versions/loader/1.20.1/0.16.9/1.0.1/server/jar