package me.kubbidev.cobbleworld.pokemon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kubbidev.cobbleworld.hologram.Hologram;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static me.kubbidev.cobbleworld.CobbleWorldMod.LOGGER;

public class CaughtPokemonModule implements AutoCloseable {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String PLAYER_FOLDER = "cobblemonplayerdata";
    private static final String CAUGHT_STATUS = "CAUGHT";

    private static final Vec3i BOARD_POSITION_1 = new Vec3i(-73, 100, -350);
    private static final Vec3i BOARD_POSITION_2 = new Vec3i(-73, 100, -340);

    private final CaughtPokemonTicker ticker;
    private final Set<Entry> userPokemonCache;

    public record Entry(String username, int caught) {
    }

    private final List<CaughtPokemonBoard> pokemonBoards = new ArrayList<>();

    public CaughtPokemonModule() {
        this.ticker = new CaughtPokemonTicker(
                this::tickingCallback,
                this::triggerCallback);

        this.ticker.registerListeners();
        this.userPokemonCache = new CopyOnWriteArraySet<>();
    }

    private void tickingCallback(MinecraftServer server) {
        this.pokemonBoards.forEach(CaughtPokemonBoard::tick);
    }

    private void triggerCallback(MinecraftServer server) {
        reloadUsers(server);
        List<Entry> entries = new ArrayList<>(this.userPokemonCache);
        this.pokemonBoards.forEach(board -> board.refreshScores(entries));
    }

    public void triggerUpdate() {
        this.ticker.triggerUpdate();
    }

    @Override
    public void close() {
        this.pokemonBoards.forEach(Hologram::close);
    }

    public void registerListeners() {
        ServerWorldEvents.LOAD.register(this::onWorldLoad);
    }

    private void onWorldLoad(MinecraftServer server, ServerWorld serverWorld) {
        if (serverWorld.getRegistryKey() != World.OVERWORLD) return;

        Supplier<Duration> durationSupplier = this.ticker::getUpdateRemainingDuration;
        spawnLeaderboard(new CaughtPokemonBoard(durationSupplier, serverWorld), BOARD_POSITION_1);
        spawnLeaderboard(new CaughtPokemonBoard(durationSupplier, serverWorld), BOARD_POSITION_2);
        triggerCallback(server);
    }

    private void spawnLeaderboard(CaughtPokemonBoard caughtPokemonBoard, Vec3i vec3i) {
        LOGGER.info("Spawning pokemon caught leaderboard");

        this.pokemonBoards.add(caughtPokemonBoard);
        caughtPokemonBoard.realignLines(Vec3d.ofBottomCenter(vec3i));
    }

    private int countPokemonCaught(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            var element = JsonParser.parseReader(reader);
            if (element.isJsonObject()) {
                return countPokemonCaught(element.getAsJsonObject());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read user from {}", path, e);
        }
        return -1;
    }

    private int countPokemonCaught(JsonObject object) {
        JsonObject registers = object
                .getAsJsonObject("extraData")
                .getAsJsonObject("cobbledex_discovery")
                .getAsJsonObject("registers");

        int caughtCount = 0;
        for (String pokemon : registers.keySet()) {
            var forms = registers.getAsJsonObject(pokemon);

            for (String form : forms.keySet()) {
                var detail = forms.getAsJsonObject(form);

                var status = detail.get("status");
                if (status == null || status.isJsonNull()) {
                    continue;
                }

                if (status.getAsString().equals(CAUGHT_STATUS)) {
                    caughtCount++;
                }
            }
        }
        return caughtCount;
    }

    public void reloadUsers(MinecraftServer server) {
        reloadUsers(server, server.getSavePath(WorldSavePath.ROOT).resolve(PLAYER_FOLDER));
    }

    private void reloadUsers(MinecraftServer server, Path directory) {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return;
        }

        this.userPokemonCache.clear(); // Clear users before iterating
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.forEach(path -> {
                if (path.getFileName().toString().endsWith(".json")) {
                    try {
                        reloadUser(server, path);
                    } catch (Exception e) {
                        LOGGER.error("Failed to load user from {}", path, e);
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.error("Exception loading users from {}", directory, e);
        }
    }

    private void reloadUser(MinecraftServer server, Path path) {
        UUID uuid;
        try {
            uuid = UUID.fromString(path.getFileName().toString().replace(".json", ""));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed to parse uuid from {}", path, e);
            return;
        }

        int caught = countPokemonCaught(path);
        if (caught == -1) return;

        var userCache = server.getUserCache();
        if (userCache == null) return;

        String username;
        var profile = userCache.getByUuid(uuid);
        if (profile.isPresent()) {
            username = profile.get().getName();
        } else {
            username = uuid.toString();
        }
        this.userPokemonCache.add(new Entry(username, caught));
    }
}
