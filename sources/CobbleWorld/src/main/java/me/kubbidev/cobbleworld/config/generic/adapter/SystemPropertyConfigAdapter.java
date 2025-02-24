package me.kubbidev.cobbleworld.config.generic.adapter;

import me.kubbidev.cobbleworld.CobbleWorldMod;
import me.kubbidev.cobbleworld.config.ConfigKeys;
import org.jetbrains.annotations.Nullable;

public class SystemPropertyConfigAdapter extends StringBasedConfigurationAdapter {
    private static final String PREFIX = "cobbleworld.";

    private final CobbleWorldMod mod;

    public SystemPropertyConfigAdapter(CobbleWorldMod mod) {
        this.mod = mod;
    }

    @Override
    protected @Nullable String resolveValue(String path) {
        // e.g.
        // 'server'            -> cobbleworld.server
        // 'data.table_prefix' -> cobbleworld.data.table-prefix
        String key = PREFIX + path;

        String value = System.getProperty(key);
        if (value != null) {
            String printableValue = ConfigKeys.shouldCensorValue(path) ? "*****" : value;
            CobbleWorldMod.LOGGER.info("Resolved configuration value from system property: {} = {}", key, printableValue);
        }
        return value;
    }

    @Override
    public CobbleWorldMod getMod() {
        return this.mod;
    }

    @Override
    public void reload() {
        // no-op
    }
}
