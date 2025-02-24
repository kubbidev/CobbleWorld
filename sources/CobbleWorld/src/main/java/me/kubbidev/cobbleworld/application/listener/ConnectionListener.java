package me.kubbidev.cobbleworld.application.listener;

import me.kubbidev.cobbleworld.application.ApplicationClient;
import me.kubbidev.cobbleworld.scheduler.SchedulerAdapter;
import me.kubbidev.cobbleworld.scheduler.SchedulerTask;
import net.dv8tion.jda.api.entities.Activity;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("SpellCheckingInspection")
public class ConnectionListener implements AutoCloseable, Runnable {
    private static final String EMPTY_SERVER = "Aucun joueur connecté";
    private static final String SINGULAR_MESSAGE = "1 joueur connecté";
    private static final String MULTIPLE_MESSAGE = "%s joueurs connectés";

    private final ApplicationClient applicationClient;
    private SchedulerTask schedulerTask;

    public ConnectionListener(ApplicationClient applicationClient) {
        this.applicationClient = applicationClient;
    }

    public void registerListeners() {
        SchedulerAdapter scheduler = this.applicationClient.getMod().getScheduler();
        this.schedulerTask = scheduler.asyncRepeating(this, 5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        Optional<MinecraftServer> server = this.applicationClient.getMod().getServer();
        if (server.isEmpty()) {
            return;
        }

        int onlinePlayers = server.get().getCurrentPlayerCount();
        if (onlinePlayers == 0) {
            updateRichPresence(EMPTY_SERVER);
        } else if (onlinePlayers == 1) {
            updateRichPresence(SINGULAR_MESSAGE);
        } else {
            updateRichPresence(MULTIPLE_MESSAGE.formatted(onlinePlayers));
        }
    }

    private void updateRichPresence(String message) {
        this.applicationClient.getShardManager().ifPresent(s -> s.setActivity(Activity.playing(message)));
    }

    @Override
    public void close() {
        if (this.schedulerTask != null) {
            this.schedulerTask.cancel();
            this.schedulerTask = null;
        }
    }
}
