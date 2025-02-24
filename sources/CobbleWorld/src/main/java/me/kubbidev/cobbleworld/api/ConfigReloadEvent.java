package me.kubbidev.cobbleworld.api;

import me.kubbidev.cobbleworld.config.CobbleWorldConfiguration;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ConfigReloadEvent {
    Event<ConfigReloadEvent> EVENT = EventFactory.createArrayBacked(ConfigReloadEvent.class, listeners -> (configuration) -> {
        for (ConfigReloadEvent event : listeners) event.onConfigReload(configuration);
    });

    void onConfigReload(CobbleWorldConfiguration configuration);
}
