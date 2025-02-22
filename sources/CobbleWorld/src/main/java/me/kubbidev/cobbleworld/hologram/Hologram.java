package me.kubbidev.cobbleworld.hologram;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Hologram implements AutoCloseable {
    private static final int VIEWING_RANGE = 50;
    private final List<HologramLine> lines = new ArrayList<>();

    private final WeakReference<ServerWorld> reference;
    private Vec3d position = Vec3d.ZERO;
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

    public Hologram(ServerWorld serverWorld) {
        this.reference = new WeakReference<>(serverWorld);
    }

    public boolean hasViewers() {
        return !this.viewers.isEmpty();
    }

    public boolean isViewer(ServerPlayerEntity player) {
        return this.viewers.contains(player.getUuid());
    }

    protected void addViewer(ServerPlayerEntity player) {
        if (this.viewers.add(player.getUuid())) {
            this.lines.forEach(l -> l.startTracking(player));
        }
    }

    protected void removeViewer(ServerPlayerEntity player) {
        if (this.viewers.remove(player.getUuid())) {
            this.lines.forEach(l -> l.stopTracking(player));
        }
    }

    protected void sendPacketToViewers(Packet<?> packet) {
        getReference().ifPresent(serverWorld -> this.viewers.forEach(uuid -> {
            var serverPlayer = serverWorld.getPlayerByUuid(uuid);
            if (serverPlayer != null) {
                ((ServerPlayerEntity) serverPlayer).networkHandler.sendPacket(packet);
            }
        }));
    }

    public final void tick() {
        if (hasViewers()) baseTick();
        getReference().ifPresent(this::tickMovement);
    }

    protected void baseTick() {
        // Empty
    }

    public boolean isInRange(PlayerEntity player) {
        return getReference().map(serverWorld -> {
            if (serverWorld != player.getWorld()) {
                return false;
            }

            double distance = squaredDistanceTo(player);
            return distance <= VIEWING_RANGE * VIEWING_RANGE;
        }).orElse(false);
    }

    public double squaredDistanceTo(PlayerEntity player) {
        return this.position.squaredDistanceTo(player.getPos());
    }

    protected void tickMovement(ServerWorld serverWorld) {
        flushOfflinePlayers(serverWorld);
        for (var serverPlayer : serverWorld.getPlayers()) {

            boolean isViewer = isViewer(serverPlayer);
            if (serverPlayer.getServerWorld() != serverWorld) {
                if (isViewer) {
                    removeViewer(serverPlayer);
                }
                continue;
            }

            boolean inRange = isInRange(serverPlayer);
            if (isViewer && !inRange) {
                removeViewer(serverPlayer);
            } else if (!isViewer && inRange) {
                addViewer(serverPlayer);
            }
        }
    }

    protected void flushOfflinePlayers(ServerWorld serverWorld) {
        this.viewers.removeIf(uuid -> serverWorld.getPlayerByUuid(uuid) == null);
    }

    @Override
    public void close() {
        this.lines.forEach(Entity::discard);
    }

    public Optional<ServerWorld> getReference() {
        return Optional.ofNullable(this.reference.get());
    }

    public int size() {
        return this.lines.size();
    }

    public void realignLines(Vec3d position) {
        this.position = position;
        double x = position.x;
        double y = position.y;
        double z = position.z;

        for (HologramLine line : this.lines) {
            line.setPos(x, (y -= HologramLine.HEIGHT), z);
        }
    }

    public void addLine(Text text) {
        insertLine(size(), text);
    }

    public void insertLine(int i, Text text) {
        getReference().ifPresent(serverWorld -> {
            if (i < 0 || i > size()) return;
            var line = new HologramLine(serverWorld, this::sendPacketToViewers);
            line.setText(text);
            if (i == size()) { // Optimization :)
                this.lines.add(line);
            } else {
                this.lines.add(i, line);
            }

            realignLines(this.position);
        });
    }

    public void setLine(int i, Text text) {
        var hologramLine = lineAt(i);
        if (hologramLine != null) {
            hologramLine.setText(text);
        }
    }

    public @Nullable HologramLine lineAt(int i) {
        return i < 0 || i >= size() ? null : this.lines.get(i);
    }
}
