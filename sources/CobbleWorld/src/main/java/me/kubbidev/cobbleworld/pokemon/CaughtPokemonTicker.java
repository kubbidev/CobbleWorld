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

    private final TickingCallback tickingCallback;
    private final TickingCallback triggerCallback;

    private long nextUpdateTicks = getUpdateRemainingTicks();

    public CaughtPokemonTicker(
            TickingCallback tickingCallback,
            TickingCallback triggerCallback
    ) {
        this.tickingCallback = tickingCallback;
        this.triggerCallback = triggerCallback;
    }

    public void registerListeners() {
        ServerTickEvents.END_SERVER_TICK.register(this::accept);
    }

    @Override
    public final void accept(MinecraftServer server) {
        this.nextUpdateTicks--;
        this.tickingCallback.accept(server, this.nextUpdateTicks);
        if (this.nextUpdateTicks <= 0) {
            this.nextUpdateTicks = getUpdateRemainingTicks();
            this.triggerCallback.accept(server, this.nextUpdateTicks);
        }
    }

    public void triggerUpdate() {
        this.nextUpdateTicks = 0;
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

    private long getUpdateRemainingTicks() {
        return calculateUpdateRemainingDuration().toSeconds() * 50;
    }

    public Duration calculateUpdateRemainingDuration() {
        return Duration.between(getCurrentDateTime(), getScheduledUpdateDateTime());
    }
}
