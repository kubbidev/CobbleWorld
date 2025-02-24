package me.kubbidev.cobbleworld.config.generic.adapter;

import me.kubbidev.cobbleworld.CobbleWorldMod;
import me.kubbidev.cobbleworld.config.ConfigKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class EnvironmentVariableConfigAdapter extends StringBasedConfigurationAdapter {
    private static final String PREFIX = "COBBLEWORLD_";

    private final CobbleWorldMod mod;

    public EnvironmentVariableConfigAdapter(CobbleWorldMod mod) {
        this.mod = mod;
    }

    @Override
    protected @Nullable String resolveValue(String path) {
        // e.g.
        // 'server'            -> COBBLEWORLD_SERVER
        // 'data.table_prefix' -> COBBLEWORLD_DATA_TABLE_PREFIX
        String key = PREFIX + path.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');

        String value = System.getenv(key);
        if (value != null) {
            String printableValue = ConfigKeys.shouldCensorValue(path) ? "*****" : value;
            CobbleWorldMod.LOGGER.info("Resolved configuration value from environment variable: {} = {}", key, printableValue);
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
