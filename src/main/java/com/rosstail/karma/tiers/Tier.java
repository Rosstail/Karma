package com.rosstail.karma.tiers;

import com.rosstail.karma.Karma;
import com.rosstail.karma.lang.AdaptMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tier {

    private final String name;
    private final String display;
    private final String shortDisplay;
    private final double minKarma;
    private final double maxKarma;
    private final List<String> joinCommands;
    private final List<String> joinOnDownCommands;
    private final List<String> joinOnUpCommands;
    private final List<String> killedCommands;
    private final Map<Tier, Double> scores = new HashMap<>();


    Tier(ConfigurationSection section, String name) {
        this.name = name;

        String display = section.getString("display");
        if (display == null) {
            display = "&7" + name;
        }

        this.display = AdaptMessage.getAdaptMessage().adapt(null, display, null);

        String shortDisplay = section.getString("short-display");
        if (shortDisplay == null) {
            shortDisplay = "&7" + name;
        }
        this.shortDisplay = AdaptMessage.getAdaptMessage().adapt(null, shortDisplay, null);

        this.minKarma = section.getDouble("minimum");
        this.maxKarma = section.getDouble("maximum");
        this.joinCommands = section.getStringList("commands.join-commands");
        this.joinOnDownCommands = section.getStringList("commands.join-on-down-commands");
        this.joinOnUpCommands = section.getStringList("commands.join-on-up-commands");
        this.killedCommands = section.getStringList("commands.killed-commands.commands");
    }

    /**
     * NULL TIER
     */
    Tier(String display, String shortDisplay) {
        this.name = null;

        if (display == null) {
            display = "&7";
        }
        this.display = AdaptMessage.getAdaptMessage().adapt(null, display, null);

        if (shortDisplay == null) {
            shortDisplay = "&7";
        }
        this.shortDisplay = AdaptMessage.getAdaptMessage().adapt(null, shortDisplay, null);
        this.minKarma = 0;
        this.maxKarma = 0;
        this.joinCommands = new ArrayList<>();
        this.joinOnDownCommands = new ArrayList<>();
        this.joinOnUpCommands = new ArrayList<>();
        this.killedCommands = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public String getShortDisplay() {
        return shortDisplay;
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

    public Map<Tier, Double> getScores() {
        return scores;
    }

    public double getTierScore(Tier tier) {
        return scores.get(tier);
    }

    public void initScores(TierManager tierManager) {
        YamlConfiguration config = Karma.getInstance().getCustomConfig();
        tierManager.getTiers().forEach((s, tier) -> {
            scores.put(tier, config.getDouble("tiers.list." + this.getName() + ".score." + tier.getName()));
        });
        scores.put(TierManager.getNoTier(), config.getDouble("tiers.list." + this.getName() + ".score.none"));
    }
}
