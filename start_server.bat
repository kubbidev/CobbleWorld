@echo off
title Minecraft Server - CobbleWorld

java -Xms1024M -Xmx16384M -jar server.jar nogui
:: Wait for user input before closing the window
pause