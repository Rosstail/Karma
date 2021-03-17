package fr.rosstail.karma;


import java.io.File;
import java.io.IOException;
import java.sql.*;

import fr.rosstail.karma.apis.WGPreps;
import fr.rosstail.karma.commands.KarmaCommand;
import fr.rosstail.karma.datas.FileResourcesUtils;
import fr.rosstail.karma.events.HitEvents;
import fr.rosstail.karma.events.KillEvents;
import fr.rosstail.karma.events.PlayerConnect;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.tiers.TierManager;
import fr.rosstail.karma.times.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class and methods of the plugin
 */
public class Karma extends JavaPlugin implements Listener {

    public Connection connection;
    public String host, database, username, password;
    public int port;

    private static Karma instance;

    @Override
    public void onLoad() {
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            new WGPreps().worldGuardHook();
        }
    }

    public void onEnable() {
        instance = this;
        if (!(new File("plugins/Karma/config.yml").exists())) {
            System.out.println("Preparing default config.yml");
            this.saveDefaultConfig();
        }
        TierManager.initTierManager(this);
        TimeManager.initTimeManager(this);

        initDefaultConfigs();
        LangManager.initCurrentLang();
        AdaptMessage.initAdaptMessage(this);

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {

                Bukkit.getPluginManager().registerEvents(this, this);

            } else {
                throw new RuntimeException("Could not find PlaceholderAPI!! Plugin can not work without it!");
            }
        }

        if (this.getConfig().getBoolean("mysql.active")) {
            prepareConnection();
        }
        this.createPlayerDataFolder();

        Bukkit.getPluginManager().registerEvents(new PlayerConnect(this), this);
        Bukkit.getPluginManager().registerEvents(new KillEvents(this), this);
        Bukkit.getPluginManager().registerEvents(new HitEvents(this), this);
        this.getCommand("karma").setExecutor(new KarmaCommand(this));
    }

    private void prepareConnection() {
        host = this.getConfig().getString("mysql.host");
        database = this.getConfig().getString("mysql.database");
        username = this.getConfig().getString("mysql.username");
        password = this.getConfig().getString("mysql.password");
        port = this.getConfig().getInt("mysql.port");
        try {
            openConnection();
            setTableToDataBase();
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
        String sql = "CREATE TABLE IF NOT EXISTS Karma ( UUID varchar(40) PRIMARY KEY UNIQUE NOT NULL,\n" +
                " Karma double,\n" +
                " Tier varchar(50),\n" +
                " Last_Attack bigint(20));";
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

    /**
     * Create the folder for player's datas
     */
    public void createPlayerDataFolder() {
        File folder = new File(this.getDataFolder(), "playerdata/");
        if (!folder.exists()) {
            String message = this.getConfig().getString("messages.creating-playerdata-folder");
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
}
