package me.kubbidev.cobbleworld.config;


import me.kubbidev.cobbleworld.config.generic.KeyedConfiguration;
import me.kubbidev.cobbleworld.config.generic.key.ConfigKey;
import me.kubbidev.cobbleworld.config.generic.key.SimpleConfigKey;

import java.util.*;

import static me.kubbidev.cobbleworld.config.generic.key.ConfigKeyFactory.*;

/**
 * All of the {@link ConfigKey}s used by CobbleWorld.
 *
 * <p>The {@link #getKeys()} method and associated behaviour allows this class
 * to function a bit like an enum, but with generics.</p>
 */
public final class ConfigKeys {
    private ConfigKeys() {}

    /**
     * The Discord application authentication token used to connect.
     */
    public static final ConfigKey<String> AUTHENTICATION_TOKEN = notReloadable(stringKey("authentication-token", ""));

    /**
     * A list of the keys defined in this class.
     */
    private static final List<SimpleConfigKey<?>> KEYS = KeyedConfiguration.initialise(ConfigKeys.class);

    public static List<? extends ConfigKey<?>> getKeys() {
        return KEYS;
    }

    /**
     * Check if the value at the given path should be censored in console/log output
     *
     * @param path the path
     * @return true if the value should be censored
     */
    public static boolean shouldCensorValue(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.contains("password") || lower.contains("uri");
    }

}
