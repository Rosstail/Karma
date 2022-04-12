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
    private String display;
    private String shortDisplay;
    private double minKarma;
    private double maxKarma;
    private List<String> joinCommands;
    private List<String> joinOnDownCommands;
    private List<String> joinOnUpCommands;
    private List<String> killedCommands;
    private Map<Tier, Double> scores = new HashMap<>();


    Tier(String name) {
        this.name = name;
    }

    public void init(ConfigurationSection section) {
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
    Tier() {
        this.name = null;
    }

    public void initNoTier(String display, String shortDisplay) {
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
