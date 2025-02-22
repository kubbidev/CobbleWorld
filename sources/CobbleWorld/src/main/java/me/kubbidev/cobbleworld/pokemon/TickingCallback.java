package me.kubbidev.cobbleworld.pokemon;

import net.minecraft.server.MinecraftServer;

@FunctionalInterface
public interface TickingCallback {
    void accept(MinecraftServer server, long ticks);
}
