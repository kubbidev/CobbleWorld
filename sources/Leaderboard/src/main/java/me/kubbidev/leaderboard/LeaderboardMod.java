package me.kubbidev.leaderboard;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@Environment(EnvType.SERVER)
public class LeaderboardMod implements DedicatedServerModInitializer {

    // lifecycle

    @Override
    public void onInitializeServer() {
        // Register the Server startup/shutdown events now
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {

        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {

        });
    }
}
