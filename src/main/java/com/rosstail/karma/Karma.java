package com.rosstail.karma;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.rosstail.karma.apis.PAPIExpansion;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.KarmaCommand;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.datas.DBInteractions;
import com.rosstail.karma.datas.FileResourcesUtils;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.events.WorldFights;
import com.rosstail.karma.events.CustomEventHandler;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.tiers.TierManager;
import com.rosstail.karma.timemanagement.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

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
        File fileConfig = new File("plugins/" + getName() + "/config.yml");
        if (!(fileConfig.exists())) {
            AdaptMessage.print("Preparing default config.yml", AdaptMessage.prints.OUT);
            this.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(fileConfig);
        ConfigData.init(getCustomConfig());
        AdaptMessage.initAdaptMessage(this);
        WorldFights.setUp(this);
        TierManager.initTierManager(this);
        TimeManager.initTimeManager(this);

        initDefaultConfigs();
        LangManager.initCurrentLang(getCustomConfig().getString("general.lang"));

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                new PAPIExpansion(this).register();
                Bukkit.getPluginManager().registerEvents(this, this);

            } else {
                throw new RuntimeException("Could not find PlaceholderAPI!! Plugin can not work without it!");
            }
        }

        if (this.getCustomConfig().getBoolean("mysql.active")) {
            try {
                DBInteractions.initDBInteractions(this);
                DBInteractions.getInstance().prepareConnection();
            } catch (Exception e) {
                AdaptMessage.print(e.toString(), AdaptMessage.prints.ERROR);
            }
        }
        this.createPlayerDataFolder();
        
        Bukkit.getPluginManager().registerEvents(new CustomEventHandler(), this);
        this.getCommand(getName().toLowerCase()).setExecutor(new KarmaCommand());

        this.updateDataTimer = new Timer();
        int delay = Math.max(1, ConfigData.getConfigData().saveDelay);
        updateDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveData(DBInteractions.reasons.TIMED, PlayerData.getPlayerList());
            }
        }, delay, delay);
    }

    /**
     * Create the folder for player's datas
     */
    public void createPlayerDataFolder() {
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
        saveData(DBInteractions.reasons.SERVER_CLOSE, PlayerData.getPlayerList());
        updateDataTimer.cancel();
        DBInteractions dbInteractions = DBInteractions.getInstance();
        if (dbInteractions != null) {
            dbInteractions.closeConnexion();
        }
    }

    public void saveData(DBInteractions.reasons reason, Map<Player, PlayerData> map) {
        DBInteractions dbInteractions = DBInteractions.getInstance();
        if (dbInteractions != null) {
            dbInteractions.updatePlayersDB(reason, map);
        } else {
            Bukkit.getScheduler().runTask(this, () -> {
                for (PlayerData playerData : map.values()) {
                    File playerFile = playerData.getPlayerFile();
                    YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                    playerConfig.set("karma", playerData.getKarma());
                    playerConfig.set("previous-karma", playerData.getPreviousKarma());
                    playerConfig.set("tier", playerData.getTier().getName());
                    playerConfig.set("previous-tier", playerData.getPreviousTier().getName());
                    playerConfig.set("wanted-time", playerData.getWantedTimeStamp().getTime());
                    try {
                        playerConfig.save(playerFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    private void initDefaultConfigs() {
        try {
            FileResourcesUtils.main("lang",this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Karma getInstance() {
        return instance;
    }

    public YamlConfiguration getCustomConfig() {
        return config;
    }
}
