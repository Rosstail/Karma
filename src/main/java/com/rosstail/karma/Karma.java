package com.rosstail.karma;


import java.io.File;
import java.io.IOException;

import com.rosstail.karma.apis.PAPIExpansion;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.KarmaCommand;
import com.rosstail.karma.datas.DBInteractions;
import com.rosstail.karma.datas.FileResourcesUtils;
import com.rosstail.karma.events.WorldFights;
import com.rosstail.karma.events.CustomEventHandler;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.tiers.TierManager;
import com.rosstail.karma.timemanagement.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class and methods of the plugin
 */
public class Karma extends JavaPlugin implements Listener {

    private YamlConfiguration config;
    private static Karma instance;

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
        DBInteractions dbInteractions = DBInteractions.getInstance();
        if (dbInteractions != null) {
            dbInteractions.closeConnexion();
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
