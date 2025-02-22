package me.kubbidev.cobbleworld.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStandEntity.class)
public interface ArmorStandAccessor {

    @Invoker("setMarker")
    void setMarker(boolean marker);
}
