package com.rosstail.karma;


import com.rosstail.karma.apis.PAPIExpansion;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.datas.DBInteractions;
import com.rosstail.karma.datas.FileResourcesUtils;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.events.CustomEventHandler;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main class and methods of the plugin
 */
public class Karma extends JavaPlugin implements Listener {

    private YamlConfiguration config;
    private static Karma instance;
    private Timer updateDataTimer;

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

        LangManager.initCurrentLang(getCustomConfig().getString("general.lang"));

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                new PAPIExpansion(this).register();
                Bukkit.getPluginManager().registerEvents(this, this);

            } else {
                throw new RuntimeException("Could not find PlaceholderAPI!! Plugin can not work without it!");
            }
        }

        if (getCustomConfig().getBoolean("mysql.active")) {
            try {
                DBInteractions.initDBInteractions(this);
                DBInteractions.getInstance().prepareTable();
            } catch (Exception e) {
                AdaptMessage.print(e.toString(), AdaptMessage.prints.ERROR);
            }
        }
        this.createPlayerDataFolder();
        
        Bukkit.getPluginManager().registerEvents(new CustomEventHandler(), this);
        this.getCommand(getName().toLowerCase()).setExecutor(new CommandManager());

        this.updateDataTimer = new Timer();
        int delay = Math.max(1, ConfigData.getConfigData().saveDelay);
        updateDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                PlayerDataManager.saveData(DBInteractions.reasons.TIMED, PlayerDataManager.getPlayerDataMap());
            }
        }, delay, delay);

        Bukkit.getOnlinePlayers().forEach(player -> {
            System.out.println(player.getName());
            PlayerDataManager.getSet(player).loadPlayerData();
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
        if (ConfigData.getConfigData().isOvertimeActive || ConfigData.getConfigData().wantedEnable) {
            PlayerData.stopTimer(PlayerDataManager.getScheduler());
        }
        PlayerDataManager.saveData(DBInteractions.reasons.SERVER_CLOSE, PlayerDataManager.getPlayerDataMap());
        updateDataTimer.cancel();
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

        if (ConfigData.getConfigData().isOvertimeActive || ConfigData.getConfigData().wantedEnable) {
            PlayerDataManager.setupScheduler();
        } else {
            PlayerData.stopTimer(PlayerDataManager.getScheduler());
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
