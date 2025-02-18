# CobbleWorld

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)
[![discord-banner](https://img.shields.io/discord/1258062506270654515?label=discord&style=for-the-badge&color=7289da)](https://discord.kubbidev.me)

Welcome to the CobbleWorld Minecraft server template! This repository contains everything you need to set up a
Minecraft  
server running the Cobblemon mod, along with various mods and datapacks to enhance the experience.

## Table of contents

- [Structure](#structure)
- [Getting Started](#getting-started)
- [Explanation](#explanation)

## Structure

This repository is structured as follows:

```
├─ client_resourcepacks    # Contains various resource packs used by clients
│ ├─ <resource_pack_files>
├─ packages                # Contains all server data (e.g. configurations)
│ ├─ <package_files>
├─ .gitignore              # Git ignore file to avoid unnecessary files in version control
├─ banned-ips.json         # List of banned ip addresses
├─ banned-players.json     # List of banned players
├─ build_client.sh         # A script used to build the client packages
├─ build_server.sh         # A script used to build the server packages
├─ clean.sh                # A script used to clean the server packages
├─ eula.txt                # Minecraft's End User License Agreement acceptance
├─ manifest.json           # Minecraft's modpack manifest
├─ modlist.json            # JSON file listing the mods for the server
├─ notes.txt               # A text file containing useful notes about the server
├─ ops.json                # List of operator (admin) players
├─ preload_mods.sh         # Script to download necessary mods
├─ preload_server.sh       # Script to download the server.jar file
├─ README.md               # Documentation on how to use the software
├─ reset.sh                # Script used to reset everything, use it carefully
├─ server.properties       # Configuration file for Minecraft server properties
├─ start_server.bat        # Script to start the Minecraft server
└─ whitelist.json          # List of players allowed to join the server
```  

## Getting Started

To set up the server, follow the steps below.

#### Requirements

* Java 21 JDK or newer
* Git
* Jq
* Wget

#### Building from source

```sh
git clone https://github.com/kubbidev/CobbleWorld.git  
cd CobbleWorld/
./build_server.sh
```

Make sure to read `notes.txt` before starting the server with `start_server.bat`.

## Explanation

### `preload_mods.sh`

This script downloads all necessary mods for the server. It accepts an argument (server or client) to download the
correct set of mods based on the server type.

> This README provides a comprehensive guide to set up and use the CobbleWorld server, along with explanations for each
> of the scripts and key configuration files. If you encounter any issues, feel free to open an issue in the repository or
> reach out for support.