package me.kubbidev.cobbleworld.config;

import me.kubbidev.cobbleworld.CobbleWorldMod;
import me.kubbidev.cobbleworld.api.ConfigReloadEvent;
import me.kubbidev.cobbleworld.config.generic.KeyedConfiguration;
import me.kubbidev.cobbleworld.config.generic.adapter.ConfigurationAdapter;

public class CobbleWorldConfiguration extends KeyedConfiguration {
    private final CobbleWorldMod mod;

    public CobbleWorldConfiguration(CobbleWorldMod mod, ConfigurationAdapter adapter) {
        super(adapter, ConfigKeys.getKeys());
        this.mod = mod;

        init();
    }

    public CobbleWorldMod getMod() {
        return this.mod;
    }

    @Override
    public void reload() {
        super.reload();
        ConfigReloadEvent.EVENT.invoker().onConfigReload(this);
    }
}
