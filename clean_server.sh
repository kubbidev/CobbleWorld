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

# Function to remove extracted packages
remove_extracted_packages() {
    if [ -d "packages" ]; then
        for zipfile in packages/*.zip; do
            [ -e "$zipfile" ] || continue
            folder="$(basename "$zipfile" .zip)"
            if [ -d "$folder" ]; then
                rm -rf "$folder"
                echo "[?] Removed extracted folder: $folder"
            fi
        done
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
remove_extracted_packages
remove_server_jar
echo "[@] Cleanup complete."