package me.kubbidev.cobbleworld.application;

import me.kubbidev.cobbleworld.CobbleWorldMod;
import me.kubbidev.cobbleworld.application.command.InteractionManager;
import me.kubbidev.cobbleworld.application.listener.ConnectionListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ApplicationClient implements AutoCloseable {
    private final CobbleWorldMod mod;

    /** A synchronization aid used to manage the shutdown process of asynchronous */
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private final ConnectionListener connectionListener;
    private final InteractionManager interactionManager;

    /** Manages the lifecycle and functionality of shards for a distributed client gateway */
    private @Nullable ShardManager shardManager;

    public ApplicationClient(CobbleWorldMod mod) {
        this.mod = mod;
        this.connectionListener = new ConnectionListener(this);
        this.interactionManager = new InteractionManager(mod);
    }

    public void connect(String token) {
        if (!token.isBlank()) {
            // Establish the connection with the remote gateway
            establishConnection(token);
        } else {
            CobbleWorldMod.LOGGER.warn("Token is blank, skipping connection");
        }
    }

    private void establishConnection(String token) {
        this.shardManager = DefaultShardManagerBuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .enableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.VOICE_STATE,
                        CacheFlag.ONLINE_STATUS
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setEnableShutdownHook(false)
                .addEventListeners(this.interactionManager)
                .addEventListeners(new ListenerAdapter() {

                    @Override
                    public void onReady(@NotNull ReadyEvent e) {
                        ApplicationClient.this.onShardReady(e.getJDA());
                    }

                    @Override
                    public void onShutdown(@NotNull ShutdownEvent e) {
                        ApplicationClient.this.shutdownLatch.countDown();
                    }
                })
                .build();
    }

    public CobbleWorldMod getMod() {
        return this.mod;
    }

    public InteractionManager getInteractionManager() {
        return this.interactionManager;
    }

    public Optional<ShardManager> getShardManager() {
        return Optional.ofNullable(this.shardManager);
    }

    /**
     * Awaits the shutdown of the {@link ShardManager} by blocking.
     *
     * <p>This method ensures that the gateway's shutdown process completes
     * before continuing further.</p>
     * <p>
     */
    private void awaitShutdown() {
        try {
            if (!this.shutdownLatch.await(30, TimeUnit.SECONDS)) { // blocking
                CobbleWorldMod.LOGGER.error("The gateway shutdown timed out!");
            }
        } catch (InterruptedException e) {
            CobbleWorldMod.LOGGER.warn("Interrupted while waiting for gateway shutdown", e);
        }
    }

    @Override
    public void close() {
        if (this.shardManager != null) {
            this.shardManager.shutdown();
            awaitShutdown(); // blocking
        }
    }

    /**
     * Handles tasks  that should be performed when a shard becomes ready.
     */
    private void onShardReady(@NotNull JDA shard) {
        this.connectionListener.registerListeners();
        this.interactionManager.registerInteraction(shard);
    }
}
