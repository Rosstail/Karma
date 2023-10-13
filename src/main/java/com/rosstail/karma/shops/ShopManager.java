package com.rosstail.karma.shops;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ShopManager {
    private final Karma plugin;
    private static ShopManager shopManager;
    private final Map<String, Shop> shops = new HashMap<>();

    public static void initShopManager(Karma plugin) {
        if (shopManager == null) {
            shopManager = new ShopManager(plugin);
        }
    }

    ShopManager(Karma plugin) {
        this.plugin = plugin;
    }

    public void setupShops() {
        FileConfiguration config = plugin.getCustomConfig();
        ConfigurationSection shopSection = config.getConfigurationSection("shops");
        shops.clear();
        if (!shopSection.getBoolean("enable", false)) {
            return;
        }

        Set<String> configShops = shopSection.getConfigurationSection("list").getKeys(false);

        for (Map.Entry<String, Shop> entry : shops.entrySet()) { //Check and remove tiers that do not exist anymore
            String s = entry.getKey();
            ConfigurationSection shopConfigSection = config.getConfigurationSection("shops.list." + s);
            if (shopConfigSection == null) {
                shops.remove(s);
            }
        }

        configShops.forEach(shopID -> {
            ConfigurationSection shopConfigSection = config.getConfigurationSection("shops.list." + shopID);
            if (shopConfigSection != null) {
                if (shops.containsKey(shopID)) { //Just update
                    shops.get(shopID).init(shopConfigSection);
                } else { //create and update
                    Shop shop = new Shop(shopID);
                    shop.init(shopConfigSection);
                    shops.put(shopID, shop);
                }
            }
        });
    }

    public static ShopManager getShopManager() {
        return shopManager;
    }

    public Map<String, Shop> getShops() {
        return shops;
    }
}
