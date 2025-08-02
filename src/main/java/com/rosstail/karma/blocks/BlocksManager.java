package com.rosstail.karma.blocks;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.players.PlayerModel;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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
        blocksModelMap = new HashMap<>();
        if (ConfigData.getConfigData().config.isConfigurationSection("blocks.list")) {
            ConfigurationSection blockListSection = ConfigData.getConfigData().config.getConfigurationSection("blocks.list");
            blockListSection.getKeys(false).forEach(s -> {
                blocksModelMap.put(s, new BlocksModel(blockListSection.getConfigurationSection(s)));
            });
        }
    }

    public void placeHandler(Player player, PlayerModel model, Block block) {
        String blockName = block.getBlockData().getMaterial().name();
        blocksModelMap.forEach((s, blocksModel) -> {
            Matcher matcher = blocksModel.regexName.matcher(blockName);
            if (matcher.find()) {
                blocksModelMap.get(s).handlePlace(player, model, block);
            }
        });
    }

    public void breakHandler(Player player, PlayerModel model, Block block) {
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
