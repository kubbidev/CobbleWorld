package me.kubbidev.cobbleworld;

import com.mojang.brigadier.CommandDispatcher;
import me.kubbidev.cobbleworld.application.ApplicationClient;
import me.kubbidev.cobbleworld.commands.MainCommand;
import me.kubbidev.cobbleworld.config.CobbleWorldConfiguration;
import me.kubbidev.cobbleworld.config.ConfigKeys;
import me.kubbidev.cobbleworld.config.generic.adapter.*;
import me.kubbidev.cobbleworld.pokemon.CaughtPokemonModule;
import me.kubbidev.cobbleworld.scheduler.SchedulerAdapter;
import me.kubbidev.cobbleworld.scheduler.StandardScheduler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Environment(EnvType.SERVER)
public final class CobbleWorldMod implements DedicatedServerModInitializer {
    private static final String MOD_ID = "cobbleworld";

    /** The static mod logger instance. */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** The mod container */
    private final ModContainer modContainer;

    /** A scheduler adapter for the platform */
    private final SchedulerAdapter schedulerAdapter;

    /** The configuration file instance */
    private CobbleWorldConfiguration configuration;

    /** The Discord application client instance */
    private ApplicationClient applicationClient;

    /** The Pokemon leaderboard instance */
    private CaughtPokemonModule caughtPokemonModule;

    /** The Minecraft server instance */
    private MinecraftServer server;

    public CobbleWorldMod() {
        this.modContainer = FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow(() -> new RuntimeException("Could not get the CobbleWorld mod container."));
        this.schedulerAdapter = new StandardScheduler(this);
    }

    // lifecycle

    @Override
    public void onInitializeServer() {
        // Register the Server startup/shutdown events now
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        registerListeners();
    }

    private void onServerStarting(MinecraftServer server) {
        this.server = server;
        long current = System.currentTimeMillis();

        // load configuration
        LOGGER.info("Loading configuration...");
        ConfigurationAdapter configFileAdapter = provideConfigurationAdapter();
        this.configuration = new CobbleWorldConfiguration(this, new MultiConfigurationAdapter(this,
                new SystemPropertyConfigAdapter(this),
                new EnvironmentVariableConfigAdapter(this),
                configFileAdapter
        ));

        // establish the connection
        this.applicationClient = new ApplicationClient(this);
        this.applicationClient.connect(this.configuration.get(ConfigKeys.AUTHENTICATION_TOKEN));

        // successfully print the time taken when loading the mod!
        long took = System.currentTimeMillis() - current;
        LOGGER.info("Successfully enabled. (took {}ms)", took);
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("Starting shutdown process...");

        // cancel delayed/repeating tasks
        this.schedulerAdapter.shutdownScheduler();
        this.applicationClient.close();

        this.caughtPokemonModule.close();

        // shutdown async executor pool
        this.schedulerAdapter.shutdownExecutor();
        this.server = null;
        LOGGER.info("Goodbye!");
    }

    // MinecraftServer singleton getter

    public Optional<MinecraftServer> getServer() {
        return Optional.ofNullable(this.server);
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

    public SchedulerAdapter getScheduler() {
        return this.schedulerAdapter;
    }

    public CobbleWorldConfiguration getConfiguration() {
        return this.configuration;
    }

    public ApplicationClient getApplicationClient() {
        return this.applicationClient;
    }

    private ConfigurationAdapter provideConfigurationAdapter() {
        return new HoconConfigAdapter(this, resolveConfig("cobbleworld.conf"));
    }

    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    }

    @SuppressWarnings("SameParameterValue")
    private Path resolveConfig(String fileName) {
        Path configFile = getConfigDirectory().resolve(fileName);

        // if the config doesn't exist, create it based on the template in the resources dir
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFile.getParent());
            } catch (IOException e) {
                // ignore
            }

            try (InputStream is = getResourceStream(fileName)) {
                Files.copy(is, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return configFile;
    }

    public InputStream getResourceStream(String path) {
        return this.modContainer.findPath(path).map(found -> {
            try {
                return Files.newInputStream(found);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).orElseThrow(() -> new RuntimeException("Could not find resource: " + path));
    }
}
