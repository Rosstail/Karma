package fr.rosstail.karma.events;

import fr.rosstail.karma.Karma;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class CustomFightWorlds {

    private static CustomFightWorlds customFightWorlds = null;
    private final List<World> enabledWorlds;

    public static void setUp(Karma plugin) {
        if (customFightWorlds == null) {
            customFightWorlds = new CustomFightWorlds(plugin);
        }
    }

    CustomFightWorlds(Karma plugin) {
        ArrayList<World> tempList = new ArrayList<>();

        boolean isBlackList;
        FileConfiguration config = plugin.getCustomConfig();
        boolean enabled = config.getString("world-fight-system.enable") != null
                && config.getBoolean("world-fight-system.enable");
        if (config.getString("world-fight-system.black-list") != null) {
            isBlackList = config.getBoolean("world-fight-system.black-list");
        } else {
            isBlackList = false;
        }
        List<String> configWorldsList = config.getStringList("world-fight-system.worlds");

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

    public static CustomFightWorlds getCustomFightWorlds() {
        return customFightWorlds;
    }

    public static List<World> getEnabledWorlds() {
        return getCustomFightWorlds().enabledWorlds;
    }

    public static boolean isFightEnabledInWorld(World world) {
        return getEnabledWorlds().contains(world);
    }
}