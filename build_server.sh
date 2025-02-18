#!/usr/bin/env sh

set -e  # Exit immediately if any command fails

# Ensure we are in the correct directory
cd "$(dirname "$0")" || exit 1
echo "Starting server bootstrap..."

# Function to ensure server.jar exists
ensure_server_jar() {
    if [ ! -f server.jar ]; then
        echo "[?] 'server.jar' not found, downloading it..."
        ./preload_server.sh
    fi
}

# Function to ensure the 'packages' directory exists
ensure_packages_directory() {
    if [ ! -d "packages" ]; then
        echo "[?] Creating 'packages' directory..."
        mkdir packages
    fi
}

# Function to unzip packages
unzip_packages() {
    echo "[?] Unzipping packages..."
    mkdir -p packages  # Ensure packages directory exists

    for zipfile in packages/*.zip; do
        [ -e "$zipfile" ] || continue # Skip if no .zip files exist
        folder="$(basename "$zipfile" .zip)" # Extracted folder name
        mkdir -p "$folder"
        
        echo "[?] Extracting $zipfile into $folder..."
        unzip -qo "$zipfile" -d "$folder" # Quiet extraction
    done
}


# Function to ensure mods are downloaded
ensure_mods_downloaded() {
    if [ ! -d "mods" ] || [ -z "$(ls -A mods/*.jar 2>/dev/null)" ]; then
        echo "[?] Mods folder is empty, downloading mods..."
        ./preload_mods.sh server
    else
        echo "[?] Mods already installed, skipping download."
    fi
}


# Main script execution
ensure_server_jar
ensure_packages_directory
unzip_packages
ensure_mods_downloaded

# Once everything is done, start the server
echo "[@] Server is ready!"