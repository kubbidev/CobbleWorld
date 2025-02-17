#!/usr/bin/env sh

MODLIST="modlist.json"

# Ensure jq (JSON parser) is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is not installed. Install it with 'sudo apt install jq' (Linux)."
    exit 1
fi

# Check for a valid argument (client, server, or both)
if [[ "$1" != "client" && "$1" != "server" && "$1" != "both" ]]; then
    echo "Usage: $0 <client|server|both>"
    exit 1
fi

SIDE=$1  # Store the argument (client/server/both)
echo "Downloading mods for: $SIDE"

# Ensure modlist file exists
if [[ ! -f "$MODLIST" ]]; then
    echo "Error: modlist.json file not found!"
    exit 1
fi

# Create mods directory if it doesn't exist
mkdir -p "mods"

# Clear all existing mods before downloading new ones
echo "Clearing old mods..."
find "mods" -type f -name "*.jar" -exec rm -f {} \;

# Parse JSON and download mods matching the selected side
jq -c '.[]' "$MODLIST" | while read -r mod; do
    name=$(echo "$mod" | jq -r '.name')
    side=$(echo "$mod" | jq -r '.side')
    url=$(echo "$mod" | jq -r '.url')

    # If 'both' is selected, download every mod
    if [[ "$SIDE" == "both" || "$side" == "$SIDE" || "$side" == "both" ]]; then
        echo "Downloading: $name ($url)"
        curl -L "$url" -o "mods/$(basename "$url")" || echo "Failed to download: $name"
    fi
done

echo "All mods downloaded. Starting the Minecraft server..."