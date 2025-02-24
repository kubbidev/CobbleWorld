package me.kubbidev.cobbleworld.config.generic.adapter;

import me.kubbidev.cobbleworld.CobbleWorldMod;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.nio.file.Path;

public class HoconConfigAdapter extends ConfigurateConfigAdapter {
    public HoconConfigAdapter(CobbleWorldMod mod, Path path) {
        super(mod, path);
    }

    @Override
    protected ConfigurationLoader<? extends ConfigurationNode> createLoader(Path path) {
        return HoconConfigurationLoader.builder().setPath(path).build();
    }
}
