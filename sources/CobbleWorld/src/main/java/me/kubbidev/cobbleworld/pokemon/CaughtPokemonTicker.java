package me.kubbidev.cobbleworld.pokemon;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

public class CaughtPokemonTicker implements Consumer<MinecraftServer> {
    /** A constant representing the UTC timezone as used */
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");

    /**
     * A constant representing the specific time of day at which the
     * {@link CaughtPokemonBoard} operations are scheduled to occur.
     *
     * <p>This value defines the daily fixed time set to 22:00 (10:00 PM).</p>
     */
    public static final LocalTime TIME_OF_DAY = LocalTime.of(22, 0);

    private final Consumer<MinecraftServer> tickingCallback;
    private final Consumer<MinecraftServer> triggerCallback;

    private long nextUpdateMillis = getUpdateRemainingMillis();

    public CaughtPokemonTicker(
            Consumer<MinecraftServer> tickingCallback,
            Consumer<MinecraftServer> triggerCallback
    ) {
        this.tickingCallback = tickingCallback;
        this.triggerCallback = triggerCallback;
    }

    public void registerListeners() {
        ServerTickEvents.END_SERVER_TICK.register(this::accept);
    }

    @Override
    public final void accept(MinecraftServer server) {
        this.nextUpdateMillis -= 50;
        this.tickingCallback.accept(server);
        if (this.nextUpdateMillis <= 0) {
            this.nextUpdateMillis = getUpdateRemainingMillis();
            this.triggerCallback.accept(server);
        }
    }

    public void triggerUpdate() {
        this.nextUpdateMillis = 0;
    }

    public Duration getUpdateRemainingDuration() {
        return Duration.ofMillis(this.nextUpdateMillis);
    }

    private ZonedDateTime getCurrentDateTime() {
        return ZonedDateTime.now(ZONE_ID);
    }

    public ZonedDateTime getScheduledUpdateDateTime() {
        ZonedDateTime currentDateTime = getCurrentDateTime();
        ZonedDateTime updatedDateTime = currentDateTime.with(TIME_OF_DAY);

        return currentDateTime.isBefore(updatedDateTime)
                ? updatedDateTime
                : updatedDateTime.plusDays(1);
    }

    private long getUpdateRemainingMillis() {
        return calculateUpdateRemainingDuration().toMillis();
    }

    private Duration calculateUpdateRemainingDuration() {
        return Duration.between(getCurrentDateTime(), getScheduledUpdateDateTime());
    }
}
