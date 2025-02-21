#!/usr/bin/env sh

# Array of folders to keep
KEEP_FOLDERS=(".git" ".idea" "binaries" "sources")

# Ensure we are in the correct directory
cd "$(dirname "$0")" || exit 1

# Function to check for the --confirm argument
check_confirmation() {
    if [ "$1" != "--confirm" ]; then
        echo "[!] This script will remove all folders."
        echo "[!] To proceed, execute the script with '--confirm'."
        exit 1
    fi
}

# Function to remove all folders except those in KEEP_FOLDERS
clean_folders() {
    echo "[@] Proceeding with folder cleanup..."
    
    find . -mindepth 1 -maxdepth 1 -type d | while read -r folder; do
        folder="${folder#./}"  # Remove leading "./"
        if is_folder_protected "$folder"; then
            echo "[?] Skipping: $folder"
        else
            echo "[?] Removing: $folder"
            rm -rf "$folder"
        fi
    done

    echo "[@] Cleanup complete."
}

# Function to check if a folder is in the KEEP_FOLDERS array
is_folder_protected() {
    for keep in "${KEEP_FOLDERS[@]}"; do
        if [ "$keep" = "$1" ]; then
            return 0  # Folder is protected
        fi
    done
    return 1  # Folder should be removed
}

# Main script execution
check_confirmation "$1"
echo "[@] Resetting the server..."
./clean.sh

# Remove all folders except those in the table
clean_folders
echo "[@] Reset complete."