package fr.rosstail.karma;


import java.io.File;
import java.io.IOException;
import java.sql.*;

import fr.rosstail.karma.apis.PAPIExpansion;
import fr.rosstail.karma.apis.WGPreps;
import fr.rosstail.karma.commands.KarmaCommand;
import fr.rosstail.karma.datas.FileResourcesUtils;
import fr.rosstail.karma.events.CustomFightWorlds;
import fr.rosstail.karma.events.CustomEventHandler;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.tiers.TierManager;
import fr.rosstail.karma.times.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            System.out.println("Preparing default config.yml");
            this.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(fileConfig);
        CustomFightWorlds.setUp(this);
        ConfigData.initKarmaValues(this.getCustomConfig());
        TierManager.initTierManager(this);
        TimeManager.initTimeManager(this);

        initDefaultConfigs();
        LangManager.initCurrentLang(this.getCustomConfig().getString("general.lang"));
        AdaptMessage.initAdaptMessage(this);

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
        
        Bukkit.getPluginManager().registerEvents(new CustomEventHandler(this), this);
        this.getCommand(getName().toLowerCase()).setExecutor(new KarmaCommand(this));
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
                System.out.println("[Karma] Added Previous_Karma and Previous_Tier columns in database.");
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
                message = ChatColor.translateAlternateColorCodes('&', message);

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
