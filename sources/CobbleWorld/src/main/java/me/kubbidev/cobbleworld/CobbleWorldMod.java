package me.kubbidev.cobbleworld;

import com.mojang.brigadier.CommandDispatcher;
import me.kubbidev.cobbleworld.commands.MainCommand;
import me.kubbidev.cobbleworld.pokemon.CaughtPokemonModule;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.SERVER)
public final class CobbleWorldMod implements DedicatedServerModInitializer {
    private static final String MOD_ID = "cobbleworld";

    /**
     * The static mod logger instance.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * The Pokemon leaderboard instance
     */
    private CaughtPokemonModule caughtPokemonModule;

    // lifecycle

    @Override
    public void onInitializeServer() {
        // Register the Server startup/shutdown events now
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        registerListeners();
    }

    private void onServerStarting(MinecraftServer server) {
        long current = System.currentTimeMillis();

        // successfully print the time taken when loading the mod!
        long took = System.currentTimeMillis() - current;
        LOGGER.info("Successfully enabled. (took {}ms)", took);
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("Starting shutdown process...");

        this.caughtPokemonModule.close();
        LOGGER.info("Goodbye!");
    }

    private void registerCommands(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess access,
            CommandManager.RegistrationEnvironment environment
    ) {
        MainCommand.register(this, dispatcher);
    }

    private void registerListeners() {
        this.caughtPokemonModule = new CaughtPokemonModule();
        this.caughtPokemonModule.registerListeners();

        CommandRegistrationCallback.EVENT.register(this::registerCommands);
    }

    public CaughtPokemonModule getCaughtPokemonModule() {
        return this.caughtPokemonModule;
    }
}
