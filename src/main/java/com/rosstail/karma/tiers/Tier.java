package com.rosstail.karma.tiers;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.lang.AdaptMessage;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tier {

    private final String name;
    private String display;
    private String shortDisplay;
    private float minKarma;
    private float maxKarma;
    private float defaultKarma;
    private List<String> joinCommands;
    private List<String> joinOnDownCommands;
    private List<String> joinOnUpCommands;
    private final Map<String, Float> tierScoreMap = new HashMap<>();


    Tier(String name) {
        this.name = name;
    }

    public void init(ConfigurationSection section) {
        String display = section.getString("display");
        if (display == null) {
            display = "&7" + name;
        }

        this.display = AdaptMessage.getAdaptMessage().adaptMessage(display);

        String shortDisplay = section.getString("short-display");
        if (shortDisplay == null) {
            shortDisplay = "&7" + name;
        }
        this.shortDisplay = AdaptMessage.getAdaptMessage().adaptMessage(shortDisplay);

        this.minKarma = (float) section.getDouble("minimum", Float.MIN_VALUE);
        this.maxKarma = (float) section.getDouble("maximum", Float.MAX_VALUE);
        this.defaultKarma = (float) section.getDouble("default-karma", (maxKarma + minKarma) / 2f);

        this.joinCommands = section.getStringList("commands.join-commands");
        this.joinOnDownCommands = section.getStringList("commands.join-on-down-commands");
        this.joinOnUpCommands = section.getStringList("commands.join-on-up-commands");

        if (section.getConfigurationSection("score") != null) {
            for (String subTier : section.getConfigurationSection("score").getKeys(false)) {
                tierScoreMap.put(subTier, (float) section.getDouble("score." + subTier, 0F));
            }
        }
        tierScoreMap.put(TierManager.getNoTier().getName(), (float) ConfigData.getConfigData().tiers.fileConfig.getDouble("tiers.list." + this.getName() + ".score.none"));
    }

    /**
     * NULL TIER
     */
    Tier() {
        this.name = "none";
    }

    public void initNoTier(String display, String shortDisplay) {
        if (display == null) {
            display = "&7";
        }
        this.display = AdaptMessage.getAdaptMessage().adaptMessage(display);

        if (shortDisplay == null) {
            shortDisplay = "&7";
        }
        this.shortDisplay = AdaptMessage.getAdaptMessage().adaptMessage(shortDisplay);
        this.minKarma = 0;
        this.maxKarma = 0;
        this.joinCommands = new ArrayList<>();
        this.joinOnDownCommands = new ArrayList<>();
        this.joinOnUpCommands = new ArrayList<>();
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

    public float getMinKarma() {
        return minKarma;
    }

    public float getMaxKarma() {
        return maxKarma;
    }

    public float getDefaultKarma() {
        return defaultKarma;
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

    public Map<String, Float> getTierScoreMap() {
        return tierScoreMap;
    }

    public float getTierScore(String tierName) {
        return tierScoreMap.get(tierName);
    }
}
