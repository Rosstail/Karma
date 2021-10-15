package com.rosstail.karma;


import java.io.File;
import java.io.IOException;
import java.sql.*;

import com.rosstail.karma.apis.PAPIExpansion;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.KarmaCommand;
import com.rosstail.karma.datas.FileResourcesUtils;
import com.rosstail.karma.events.WorldFights;
import com.rosstail.karma.events.CustomEventHandler;
import com.rosstail.karma.configdata.ConfigData;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.Cause;
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

    public Connection connection;
    public String host, database, username, password;
    public int port;

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
            AdaptMessage.print("Preparing default config.yml", Cause.OUT);
            this.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(fileConfig);
        ConfigData.initKarmaValues(getCustomConfig());
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
            prepareConnection();
        }
        this.createPlayerDataFolder();
        
        Bukkit.getPluginManager().registerEvents(new CustomEventHandler(), this);
        this.getCommand(getName().toLowerCase()).setExecutor(new KarmaCommand());
    }

    private void prepareConnection() {
        host = this.getCustomConfig().getString("mysql.host");
        database = this.getCustomConfig().getString("mysql.database");
        username = this.getCustomConfig().getString("mysql.username");
        password = this.getCustomConfig().getString("mysql.password");
        port = this.getCustomConfig().getInt("mysql.port");
        try {
            openConnection();
            setTableToDataBase();
            updateTableToDataBase();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database,
                    this.username, this.password);
        }
    }


    public void setTableToDataBase() {
        String sql = "CREATE TABLE IF NOT EXISTS " + getName() + " ( UUID varchar(40) PRIMARY KEY UNIQUE NOT NULL,\n" +
                " Karma double,\n" +
                " Previous_Karma double,\n" +
                " Tier varchar(50),\n" +
                " Previous_Tier varchar(50),\n" +
                " Last_Attack DATETIME NOT NULL DEFAULT '1970-01-01 01:00:00');";
        try {
            if (connection != null && !connection.isClosed()) {
                Statement statement = connection.createStatement();
                statement.execute(sql);
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTableToDataBase() {
        String sql = "ALTER TABLE " + getName() + " " +
                "ADD Previous_Karma double AFTER Karma," +
                "ADD Previous_Tier varchar(50) AFTER Tier;";
        try {
            if (connection != null && !connection.isClosed()) {
                Statement statement = connection.createStatement();
                statement.execute(sql);
                statement.close();
            }
        } catch (SQLException e) {
        }
    }

    /**
     * Create the folder for player's datas
     */
    public void createPlayerDataFolder() {
        File folder = new File(this.getDataFolder(), "playerdata/");
        if (!folder.exists()) {
            String message = this.getCustomConfig().getString("messages.creating-playerdata-folder");
            if (message != null) {
                message = AdaptMessage.getAdaptMessage().message(null, message, null);

                getServer().getConsoleSender().sendMessage(message);
            }
            folder.mkdir();
        }
    }

    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
