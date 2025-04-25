package com.rosstail.karma.blocks;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.players.PlayerDataModel;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class BlocksManager {

    private static BlocksManager blocksManager;

    private final Karma plugin;
    private Map<String, BlocksModel> blocksModelMap;

    public BlocksManager(Karma plugin) {
        this.plugin = plugin;
    }

    public static void initBlocksManager(Karma plugin) {
        if (blocksManager == null) {
            blocksManager = new BlocksManager(plugin);
        }
    }

    public void setup() {
        ConfigurationSection blocksSection = ConfigData.getConfigData().config.getConfigurationSection("blocks.list");
        if (blocksSection != null) {
            blocksModelMap = new HashMap<>();
            blocksSection.getKeys(false).forEach(s -> {
                blocksModelMap.put(s, new BlocksModel(blocksSection.getConfigurationSection(s)));
            });
        }
    }

    public void placeHandler(Player player, PlayerDataModel model, Block block) {
        String blockName = block.getBlockData().getMaterial().name();
        blocksModelMap.forEach((s, blocksModel) -> {
            Matcher matcher = blocksModel.regexName.matcher(blockName);
            if (matcher.find()) {
                blocksModelMap.get(s).handlePlace(player, model, block);
            }
        });
    }

    public void breakHandler(Player player, PlayerDataModel model, Block block) {
        String blockName = block.getBlockData().getMaterial().name();

        blocksModelMap.forEach((s, blocksModel) -> {
            Matcher matcher = blocksModel.regexName.matcher(blockName);
            if (matcher.find()) {
                blocksModelMap.get(s).handleBreak(player, model, block);
            }
        });
    }

    public static BlocksManager getBlocksManager() {
        return blocksManager;
    }
}
