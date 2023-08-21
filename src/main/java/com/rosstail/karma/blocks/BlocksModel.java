package com.rosstail.karma.blocks;

import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.events.karmaevents.PlayerOverTimeResetEvent;
import kotlin.text.Regex;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class BlocksModel {
    String blockName;
    public final Pattern regexName;

    private BlocksData placeBlocksData;
    private float placeKarma = 0f;
    private boolean placeResetOvertime = true;

    private BlocksData breakBlocksData;
    private float breakKarma = 0f;
    private boolean breakResetOvertime = true;

    public BlocksModel(ConfigurationSection section) {
        this.blockName = section.getName();
        this.regexName = Pattern.compile(section.getString("regex", this.blockName).replaceAll("\\*", "(\\\\w+)?"));
        ConfigurationSection placeSection = section.getConfigurationSection("place");
        if (placeSection != null) {
            ConfigurationSection placeDataSection = placeSection.getConfigurationSection("data");
            if (placeDataSection != null) {
                this.placeBlocksData = new BlocksData(this, placeDataSection);
            }
            this.placeKarma = (float) placeSection.getDouble("value");
            this.placeResetOvertime = placeSection.getBoolean("reset-overtime", true);
        }
        ConfigurationSection breakSection = section.getConfigurationSection("break");
        if (breakSection != null) {
            ConfigurationSection breakDataSection = breakSection.getConfigurationSection("data");
            if (breakDataSection != null) {
                this.breakBlocksData = new BlocksData(this, breakDataSection);
            }
            this.breakKarma = (float) breakSection.getDouble("value");
            this.breakResetOvertime = breakSection.getBoolean("reset-overtime", true);
        }

    }

    public void handlePlace(Player player, PlayerModel model, Block block) {
        if (placeBlocksData != null) {
            if (!placeBlocksData.checkData(block)) {
                return;
            }
        }
        handleKarmaChange(player, model, placeKarma);
    }

    public void handleBreak(Player player, PlayerModel model, Block block) {
        if (breakBlocksData != null) {
            if (!breakBlocksData.checkData(block)) {
                return;
            }
        }
        handleKarmaChange(player, model, breakKarma);
    }

    private void handleKarmaChange(Player player, PlayerModel model, float value) {
        float newKarma = model.getKarma() + value;

        if (PlayerDataManager.limitKarma(newKarma) != model.getKarma()) {
            PlayerKarmaChangeEvent karmaChangeEvent = new PlayerKarmaChangeEvent(player, model, newKarma);
            Bukkit.getPluginManager().callEvent(karmaChangeEvent);

            model.getOverTimeStampMap().forEach((s, timestamp) -> {
                PlayerOverTimeResetEvent playerOverTimeResetEvent = new PlayerOverTimeResetEvent(player, s);
                Bukkit.getPluginManager().callEvent(playerOverTimeResetEvent);
            });
        }
    }

    public Pattern getRegexName() {
        return regexName;
    }

    public BlocksData getPlaceBlocksData() {
        return placeBlocksData;
    }

    public float getPlaceKarma() {
        return placeKarma;
    }

    public BlocksData getBreakBlocksData() {
        return breakBlocksData;
    }

    public float getBreakKarma() {
        return breakKarma;
    }

    public String getBlockName() {
        return blockName;
    }

    public boolean isPlaceResetOvertime() {
        return placeResetOvertime;
    }

    public boolean isBreakResetOvertime() {
        return breakResetOvertime;
    }
}

