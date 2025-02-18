#!/usr/bin/env sh

# Ensure we are in the correct directory
cd "$(dirname "$0")" || exit 1

# Function to remove the mods folder
remove_mods_folder() {
    if [ -d "mods" ]; then
        rm -rf mods
        echo "[?] Removed mods folder"
    fi
}

# Function to remove the server jar
remove_server_jar() {
    if [ -f "server.jar" ]; then
        rm server.jar
        echo "[?] Removed server.jar"
    fi
}

# Main script execution
echo "[@] Cleaning up server files..."
remove_mods_folder
remove_server_jar
echo "[@] Cleanup complete."