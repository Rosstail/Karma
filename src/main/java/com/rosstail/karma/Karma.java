package com.rosstail.karma;


import com.rosstail.karma.apis.PAPIExpansion;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.datas.*;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.events.KarmaEventHandler;
import com.rosstail.karma.events.MinecraftEventHandler;
import com.rosstail.karma.events.WorldFights;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.shops.ShopManager;
import com.rosstail.karma.tiers.TierManager;
import com.rosstail.karma.timemanagement.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main class and methods of the plugin
 */
public class Karma extends JavaPlugin implements Listener {
    private YamlConfiguration config;
    private static Karma instance;
    private Timer updateDataTimer;
    private Timer scoreboardTimer;
    private TopFlopScoreManager topFlopScoreManager;

    @Override
    public void onLoad() {
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            WGPreps.initWGPreps();
            WGPreps.getWgPreps().worldGuardHook();
        }
    }

    public void onEnable() {
        instance = this;

        AdaptMessage.initAdaptMessage(this);
        TierManager.initTierManager(this);
        TimeManager.initTimeManager(this);
        ShopManager.initShopManager(this);
        WorldFights.initWorldFights(this);

        loadCustomConfig();
        StorageManager manager = StorageManager.initStorageManage(this);
        manager.chooseDatabase();

        LangManager.initCurrentLang(getCustomConfig().getString("general.lang"));

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                new PAPIExpansion(this).register();
                Bukkit.getPluginManager().registerEvents(this, this);

            } else {
                throw new RuntimeException("Could not find PlaceholderAPI!! Plugin can not work without it!");
            }
        }

        this.createPlayerDataFolder();

        TopFlopScoreManager.init();
        this.topFlopScoreManager = TopFlopScoreManager.getTopFlopScoreManager();
        topFlopScoreManager.getScores();


        Bukkit.getPluginManager().registerEvents(new MinecraftEventHandler(), this);
        Bukkit.getPluginManager().registerEvents(new KarmaEventHandler(), this);
        this.getCommand(getName().toLowerCase()).setExecutor(new CommandManager());

        this.updateDataTimer = new Timer();
        this.scoreboardTimer = new Timer();
        int delay = Math.max(1, ConfigData.getConfigData().saveDelay);

        updateDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                PlayerDataManager.saveAllPlayerModelToStorage();
            }
        }, delay, delay);

        scoreboardTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                topFlopScoreManager.getScores();
            }
        }, 60 * 1000, 60 * 1000);

        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerDataManager.initPlayerModelToMap(StorageManager.getManager().selectPlayerModel(player.getUniqueId().toString()));
        });
    }

    /**
     * Create the folder for player's datas
     */
    private void createPlayerDataFolder() {
        File folder = new File(this.getDataFolder(), "playerdata/");
        if (!folder.exists()) {
            String message = this.getCustomConfig().getString("messages.creating-playerdata-folder");
            if (message != null) {
                message = AdaptMessage.getAdaptMessage().adapt(null, message, null);

                getServer().getConsoleSender().sendMessage(message);
            }
            folder.mkdir();
        }
    }

    public void onDisable() {
        if (ConfigData.getConfigData().overtimeActive || ConfigData.getConfigData().wantedEnable) {
            PlayerDataManager.stopTimer(PlayerDataManager.getScheduler());
        }
        Map<String, PlayerModel> playerModelMap = PlayerDataManager.getPlayerModelMap();
        for (Map.Entry<String, PlayerModel> entry : playerModelMap.entrySet()) {
            String s = entry.getKey();
            PlayerModel model = entry.getValue();
            StorageManager.getManager().updatePlayerModel(model);
        }
        StorageManager.getManager().disconnect();
        updateDataTimer.cancel();
        scoreboardTimer.cancel();
    }

    private void initDefaultLocales() {
        try {
            FileResourcesUtils.main("lang",this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Karma getInstance() {
        return instance;
    }

    public void loadCustomConfig() {
        File fileConfig = new File("plugins/" + getName() + "/config.yml");
        if (!(fileConfig.exists())) {
            AdaptMessage.print("Preparing default config.yml", AdaptMessage.prints.OUT);
            this.saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(fileConfig);

        ConfigData.init(getCustomConfig());
        initDefaultLocales();

        if (ConfigData.getConfigData().overtimeActive || ConfigData.getConfigData().wantedEnable) {
            PlayerDataManager.setupScheduler();
        } else {
            PlayerDataManager.stopTimer(PlayerDataManager.getScheduler());
        }
        WorldFights.getWorldFights().setEnabledWorlds();
        TierManager.getTierManager().setupTiers();
        TimeManager.getTimeManager().setupTimes();
        ShopManager.getShopManager().setupShops();
    }

    public YamlConfiguration getCustomConfig() {
        return config;
    }
}
