#!/usr/bin/env sh

MODLIST="modlist.json"

# Ensure we are in the correct directory
cd "$(dirname "$0")" || exit 1

# Function to check if jq is installed
check_jq_installed() {
    if ! command -v jq &> /dev/null; then
        echo "[!] Error: jq is not installed. Install it with 'sudo apt install jq' (Linux)."
        exit 1
    fi
}

# Function to validate the argument
validate_argument() {
    if [[ "$1" != "client" && "$1" != "server" && "$1" != "both" ]]; then
        echo "[!] Usage: $0 <client|server|both>"
        exit 1
    fi
}

# Function to ensure modlist file exists
check_modlist_exists() {
    if [[ ! -f "$MODLIST" ]]; then
        echo "[!] Error: modlist.json file not found!"
        exit 1
    fi
}

# Function to create mods directory and clear old mods
prepare_mods_directory() {
    mkdir -p "mods"
    echo "[?] Clearing old mods..."
    find "mods" -type f -name "*.jar" -exec rm -f {} \;
}

# Function to get total number of mods for selected side
get_total_mods() {
    total_mods=$(jq "[.[] | select(\"$SIDE\" == \"both\" or .side == \"$SIDE\" or .side == \"both\")] | length" "$MODLIST")
    echo "$total_mods"
}

# Function to download mods
download_mods() {
    current_mod=0
    jq -c '.[]' "$MODLIST" | while read -r mod; do
        name=$(echo "$mod" | jq -r '.name')
        side=$(echo "$mod" | jq -r '.side')
        url=$(echo "$mod" | jq -r '.url')

        if [[ "$SIDE" == "both" || "$side" == "$SIDE" || "$side" == "both" ]]; then
            current_mod=$((current_mod + 1))
            echo "[${current_mod}/${total_mods}] Downloading: $name ($url)"
            (
                wget --content-disposition -P "mods" $url -q || echo "Failed to download: $name"
            ) &
        fi
    done

    wait
    echo "[@] All mods have been downloaded."
}

# Main script execution
check_jq_installed
validate_argument "$1"
SIDE=$1  # Store the argument (client/server/both)
echo "[?] Downloading mods for: $SIDE"
check_modlist_exists
prepare_mods_directory
total_mods=$(get_total_mods)
download_mods