package com.rosstail.karma.events;

import com.rosstail.karma.Karma;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class WorldFights {

    private final Karma plugin;
    private static WorldFights worldFights = null;
    private final List<World> enabledWorlds = new ArrayList<>();

    public static void initWorldFights(Karma plugin) {
        if (worldFights == null) {
            worldFights = new WorldFights(plugin);
        }
    }

    WorldFights(Karma plugin) {
        this.plugin = plugin;
    }

    public void setEnabledWorlds() {
        enabledWorlds.clear();
        boolean isBlackList;
        FileConfiguration config = plugin.getCustomConfig();
        boolean enabled = config.getString("worlds.enable") != null
                && config.getBoolean("worlds.enable");
        if (config.getString("worlds.black-list") != null) {
            isBlackList = config.getBoolean("worlds.black-list");
        } else {
            isBlackList = false;
        }
        List<String> configWorldsList = config.getStringList("worlds.worlds");

        if (enabled) {
            for (World world : Bukkit.getWorlds()) {
                if (!isBlackList) {
                    if (configWorldsList.contains(world.getName())) {
                        enabledWorlds.add(world);
                    }
                } else {
                    if (!configWorldsList.contains(world.getName())) {
                        enabledWorlds.add(world);
                    }
                }
            }
        } else {
            enabledWorlds.addAll(Bukkit.getWorlds());
        }
    }

    public static WorldFights getWorldFights() {
        return worldFights;
    }

    public static List<World> getEnabledWorlds() {
        return getWorldFights().enabledWorlds;
    }

    public static boolean isFightEnabledInWorld(World world) {
        return getEnabledWorlds().contains(world);
    }
}