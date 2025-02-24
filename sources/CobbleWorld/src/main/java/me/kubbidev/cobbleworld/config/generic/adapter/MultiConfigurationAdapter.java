package me.kubbidev.cobbleworld.config.generic.adapter;

import com.google.common.collect.ImmutableList;
import me.kubbidev.cobbleworld.CobbleWorldMod;

import java.util.List;
import java.util.Map;

/**
 * A {@link ConfigurationAdapter} composed of one or more other ConfigurationAdapters.
 */
public class MultiConfigurationAdapter implements ConfigurationAdapter {
    private final CobbleWorldMod mod;
    private final List<ConfigurationAdapter> adapters;

    /**
     * Creates a {@link MultiConfigurationAdapter}.
     *
     * <p>The first adapter in the list has priority (the final say) in deciding what the value is.
     * All adapters are tried in reverse order, and the value returned from the previous adapter
     * is passed into the next as the {@code def} value.</p>
     *
     * @param mod the mod
     * @param adapters a list of adapters
     */
    public MultiConfigurationAdapter(CobbleWorldMod mod, List<ConfigurationAdapter> adapters) {
        this.mod = mod;
        this.adapters = ImmutableList.copyOf(adapters).reverse();
    }

    public MultiConfigurationAdapter(CobbleWorldMod mod, ConfigurationAdapter... adapters) {
        this(mod, ImmutableList.copyOf(adapters));
    }

    @Override
    public CobbleWorldMod getMod() {
        return this.mod;
    }

    @Override
    public void reload() {
        for (ConfigurationAdapter adapter : this.adapters) {
            adapter.reload();
        }
    }

    @Override
    public String getString(String path, String def) {
        String result = def;
        for (ConfigurationAdapter adapter : this.adapters) {
            result = adapter.getString(path, result);
        }
        return result;
    }

    @Override
    public int getInteger(String path, int def) {
        int result = def;
        for (ConfigurationAdapter adapter : this.adapters) {
            result = adapter.getInteger(path, result);
        }
        return result;
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        boolean result = def;
        for (ConfigurationAdapter adapter : this.adapters) {
            result = adapter.getBoolean(path, result);
        }
        return result;
    }

    @Override
    public List<String> getStringList(String path, List<String> def) {
        List<String> result = def;
        for (ConfigurationAdapter adapter : this.adapters) {
            result = adapter.getStringList(path, result);
        }
        return result;
    }

    @Override
    public Map<String, String> getStringMap(String path, Map<String, String> def) {
        Map<String, String> result = def;
        for (ConfigurationAdapter adapter : this.adapters) {
            result = adapter.getStringMap(path, result);
        }
        return result;
    }
}
