package me.kubbidev.cobbleworld.hologram;

import me.kubbidev.cobbleworld.mixin.ArmorStandAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.entity.EntityChangeListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.SERVER)
public class HologramLine extends ArmorStandEntity {
    public static final float HEIGHT = 0.38F;

    private final EntitiesDestroyS2CPacket destroyPacket
            = new EntitiesDestroyS2CPacket(getId());

    private final Consumer<Packet<?>> receiver;

    @Nullable
    private List<DataTracker.SerializedEntry<?>> changedEntries;

    public HologramLine(ServerWorld serverWorld, Consumer<Packet<?>> receiver) {
        super(EntityType.ARMOR_STAND, serverWorld);
        this.receiver = receiver;
        setInvisible(true);
        setCustomNameVisible(true);
        ((ArmorStandAccessor) this).setMarker(true);
        syncEntityData(false);
        setChangeListener(new EntityChangeListener() {
            @Override
            public void remove(Entity.RemovalReason reason) {
                receiver.accept(HologramLine.this.destroyPacket);
            }

            @Override
            public void updateEntityPosition() {
                receiver.accept(new EntityPositionS2CPacket(HologramLine.this));
            }
        });
    }

    public void setText(Text text) {
        super.setCustomName(text);
        syncEntityData(true);
    }

    public void startTracking(ServerPlayerEntity player) {
        List<Packet<ClientPlayPacketListener>> list = new ArrayList<>();
        sendPackets(list::add);
        player.networkHandler.sendPacket(new BundleS2CPacket(list));
    }

    public void sendPackets(Consumer<Packet<ClientPlayPacketListener>> sender) {
        Packet<ClientPlayPacketListener> packet = createSpawnPacket();
        sender.accept(packet);
        if (this.changedEntries != null) {
            sender.accept(new EntityTrackerUpdateS2CPacket(getId(), this.changedEntries));
        }
    }

    /**
     * Sends a packet for synchronization with watcher and tracked player (if applicable)
     */
    private void sendSyncPacket(Packet<?> packet) {
        this.receiver.accept(packet);
    }

    private void syncEntityData(boolean send) {
        DataTracker dataTracker = getDataTracker();
        List<DataTracker.SerializedEntry<?>> list = dataTracker.getDirtyEntries();
        if (list != null) {
            this.changedEntries = dataTracker.getChangedEntries();
            if (send) {
                sendSyncPacket(new EntityTrackerUpdateS2CPacket(getId(), list));
            }
        }
    }

    public void stopTracking(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(this.destroyPacket);
    }
}
