package com.rosstail.karma.events;

import com.rosstail.karma.Karma;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class WorldFights {

    private static WorldFights worldFights = null;
    private final List<World> enabledWorlds;

    public static void setUp(Karma plugin) {
        if (worldFights == null) {
            worldFights = new WorldFights(plugin);
        }
    }

    WorldFights(Karma plugin) {
        ArrayList<World> tempList = new ArrayList<>();

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
                        tempList.add(world);
                    }
                } else {
                    if (!configWorldsList.contains(world.getName())) {
                        tempList.add(world);
                    }
                }
            }
        } else {
            tempList.addAll(Bukkit.getWorlds());
        }

        enabledWorlds = tempList;
    }

    public static WorldFights getCustomFightWorlds() {
        return worldFights;
    }

    public static List<World> getEnabledWorlds() {
        return getCustomFightWorlds().enabledWorlds;
    }

    public static boolean isFightEnabledInWorld(World world) {
        return getEnabledWorlds().contains(world);
    }
}