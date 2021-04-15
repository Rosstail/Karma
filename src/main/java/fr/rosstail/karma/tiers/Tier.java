package fr.rosstail.karma.tiers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class Tier {

    private final String name;
    private final String display;
    private final double minKarma;
    private final double maxKarma;
    private final List<String> joinCommands;
    private final List<String> joinOnDownCommands;
    private final List<String> joinOnUpCommands;

    private final List<String> killedCommands;



    Tier(ConfigurationSection section, String name) {
        this.name = name;

        String display = section.getString("display");
        if (display == null) {
            display = "&7" + name;
        }
        this.display = ChatColor.translateAlternateColorCodes('&', display);
        this.minKarma = section.getDouble("minimum");
        this.maxKarma = section.getDouble("maximum");
        this.joinCommands = section.getStringList("join-commands");
        this.joinOnDownCommands = section.getStringList("join-on-down-commands");
        this.joinOnUpCommands = section.getStringList("join-on-up-commands");
        this.killedCommands = section.getStringList("killed-commands");
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public double getMinKarma() {
        return minKarma;
    }

    public double getMaxKarma() {
        return maxKarma;
    }

    public List<String> getJoinCommands() {
        return joinCommands;
    }

    public List<String> getJoinOnDownCommands() {
        return joinOnDownCommands;
    }

    public List<String> getJoinOnUpCommands() {
        return joinOnUpCommands;
    }

    public List<String> getKilledCommands() {
        return killedCommands;
    }
}
