package fr.rosstail.karma;

import java.io.File;
import java.io.IOException;
import java.sql.*;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class and methods of the plugin
 */
public class Karma extends JavaPlugin {

    public Karma() {
    }

    public static Karma INSTANCE;

    public static Karma get() {
        return INSTANCE;
    }

    public Connection connection;
    public String host, database, username, password;
    public int port;

    @Override
    public void onLoad() {
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            new WGPreps().worldGuardHook();
        }
    }

    public void onEnable() {
        INSTANCE = this;

        this.saveDefaultConfig();

        if (this.getConfig().getBoolean("mysql.active")) {
            prepareConnection();
        } else {
            this.createPlayerDataFolder();
        }

        this.createLangFiles();
        Bukkit.getPluginManager().registerEvents(new PlayerConnect(), this);
        Bukkit.getPluginManager().registerEvents(new KillEvents(), this);
        Bukkit.getPluginManager().registerEvents(new HitEvents(), this);
        this.getCommand("karma").setExecutor(new KarmaCommand());
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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
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
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }


    public void setTableToDataBase() {
        String sql = "CREATE TABLE IF NOT EXISTS Karma ( UUID varchar(40) PRIMARY KEY UNIQUE NOT NULL,\n" +
                " NickName varchar(16) NOT NULL,\n" +
                " Karma double,\n" +
                " Tier varchar(50),\n" +
                " Last_Attack int);";
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

    /**
     * Create the subfolder and files for languages
     */
    public void createLangFiles() {
        File file = new File(this.getDataFolder(), "lang/");
        if (!file.exists()) {
            file.mkdir();
            getServer().getConsoleSender().sendMessage("&9Creating default language files");
            setEnglishLang();
            setFrenchLang();
            setSpanishLang();
            setRomanianLang();
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


    private void setEnglishLang() {
        File file = new File(this.getDataFolder(), "lang/en_EN.yml");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("by-player-only", "[Karma] This command must be send by a player.");
        configuration.set("creating-playerdata-folder", "[Karma] &9playerdata/ folder doesn't exists. &aCreating it&7.");
        configuration.set("creating-player", "[Karma] &9Creating player file for &a<player>&9.");
        configuration.set("disconnected-player", "[Karma] &c<player> is not connected or does not exists.");
        configuration.set("check-own-karma", "[Karma] Your karma is &a<karma> &rand your tier is &6<tier>&r.");
        configuration.set("check-other-karma", "[Karma] &6<player>'s &rkarma is &6<karma> &rand his tier is &6<tier>&r.");
        configuration.set("set-karma", "[Karma] &9<player>'s &rKarma is now &9<newKarma> &rand his Tier is &9<tier>&r.");
        configuration.set("add-karma", "[Karma] &aAdded &6<value> &rKarma to &6<player> &rfor a total of &6<newKarma> &rkarma and the <tier> tier.");
        configuration.set("remove-karma", "[Karma] &cRemoved &6<value> &rKarma to &6<player> &rfor a total of &6<newKarma> &rkarma and the <tier> tier.");
        configuration.set("reset-karma", "[Karma] &6<player>&r's karma has been reset. Karma : &6<newKarma> &rand tier is &6<tier>&r.");
        configuration.set("tier-change", "[Karma] You are now a &6<tier> &r!");
        configuration.set("self-defending-off", "[Karma] You are defending yourself ! Uarma unchanged.");
        configuration.set("self-defending-on", "[Karma] You are defending yourself but your Karma change.");
        configuration.set("permission-denied", "[Karma] &cYou don't have permission !");

        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setFrenchLang() {
        File file = new File(this.getDataFolder(), "lang/fr_FR.yml");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("by-player-only", "[Karma] Cette commande doit être lancée par un joueur.");
        configuration.set("creating-playerdata-folder", "[Karma] Le dossier &9playerdata/ &rn'existe pas. &aCréation&7...");
        configuration.set("creating-player", "[Karma] &9Création du fichier de joueur pour &a<player>&9.");
        configuration.set("disconnected-player", "[Karma] &c<player> est déconnecté ou n'existe pas.");
        configuration.set("check-own-karma", "[Karma] Votre karma est de &a<karma> &ret votre alignement est &6<tier>&r.");
        configuration.set("check-other-karma", "[Karma] Le karma de &6<player> &rest &6<karma> &ret son alignement est &6<tier>&r.");
        configuration.set("set-karma", "[Karma] Le karma de &9<player> &rest désormais de &9<newKarma> &ret son alignement est &9<tier>&r.");
        configuration.set("add-karma", "[Karma] &aAjout de &6<value> &rde karma à &6<player> &rpour un total de &6<newKarma> &rkarma et l'alignement <tier>.");
        configuration.set("remove-karma", "[Karma] &cDiminution de &6<value> &rkarma pour &6<player> &rpour un total de &6<newKarma> &rkarma et l'alignement <tier>.");
        configuration.set("reset-karma", "[Karma] Le karma du joueur &6<player> &rest réinitialisé. Karma : &6<newKarma> &ret Alignement : &6<tier>&r.");
        configuration.set("tier-change", "[Karma] Vous êtes désormais un(e) &6<tier> &r!");
        configuration.set("self-defending-off", "[Karma] Vous êtes en train de vous défendre ! Karma inchangé.");
        configuration.set("self-defending-on", "[Karma] Vous vous défendez mais votre Karma change tout de même.");
        configuration.set("permission-denied", "[Karma] &cVous n'avez pas la permission !");

        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSpanishLang() {
        File file = new File(this.getDataFolder(), "lang/es_ES.yml");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("by-player-only", "[Karma] Este comando es solo para jugadores.");
        configuration.set("creating-playerdata-folder", "[Karma] &9Carpeta playerdata/ no exite. &aCreandola&7.");
        configuration.set("creating-player", "[Karma] &9Creando un archivo de jugador para &a<player>&9.");
        configuration.set("disconnected-player", "[Karma] &c<player> no esta conectado o no exite.");
        configuration.set("check-own-karma", "[Karma] Tu karma es &a<karma> &ry tu Tier es &6<tier>&r.");
        configuration.set("check-other-karma", "[Karma] &rEl karma de &6<player> &res &6<karma> &ry su Tier es &6<tier>&r.");
        configuration.set("set-karma", "[Karma] &rEl karma de &9<player>'s &rahora es &9<newKarma> &ry su Tier es &9<tier>&r.");
        configuration.set("add-karma", "[Karma] &aSe añadido &6<value> &rKarma a &6<player> &rcon un total de &6<newKarma> &rkarma y tier <tier>.");
        configuration.set("remove-karma", "[Karma] &cEliminado &6<value> &rKarma a &6<player> &rcon un total de &6<newKarma> &rkarma y tier <tier>.");
        configuration.set("reset-karma", "[Karma] &rEl karma de &6<player>&r se ha reiniciado. Karma : &6<newKarma> &ry el tier es &6<tier>&r.");
        configuration.set("tier-change", "[Karma] ¡ Ahora eres &6<tier> &r!");
        configuration.set("self-defending-off", "[Karma] You are defending yourself ! Uarma unchanged.");
        configuration.set("self-defending-on", "[Karma] You are defending yourself but your Karma change.");
        configuration.set("permission-denied", "[Karma] &c¡No tienes permiso!");

        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setRomanianLang() {
        File file = new File(this.getDataFolder(), "lang/ro_RO.yml");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("by-player-only", "[Karma] &fComanda aceasta trebuie executata de catre un jucator!");
        configuration.set("creating-playerdata-folder", "[Karma] &fNu exista \"playerdata\", asa ca creem noi!");
        configuration.set("creating-player", "[Karma] &fCreem un jucator cu numele &a<player>");
        configuration.set("disconnected-player", "[Karma] &a<player> nu este pe server, sau nu exista.");
        configuration.set("check-own-karma", "[Karma] Karma-ul tau este &a<karma> &fsi tier-ul tau este &2<tier>&r.");
        configuration.set("check-other-karma", "[Karma] &a<player> &fdetine &a<karma> &fKarma si tier-ul sau este &2<tier>&r.");
        configuration.set("set-karma", "[Karma] &9<player>''s &rAi setat karma &a<newKarma> &rsi tier &2<tier>&r.");
        configuration.set("add-karma", "[Karma] &fAi scos &a<value> &rKarma de la &2<player> &rel acum detine de &a<newKarma> &rKarma si tier-ul&2 <tier>.");
        configuration.set("remove-karma", "[Karma] &cAti scos &6<value> &rkarma de la &6<player> &rpentru un total de &6<newKarma> &rsi tier-ul <tier>.");
        configuration.set("reset-karma", "[Karma] &fKarma-ul lui &a<player>&fa fost resetat. Noul sau Karma este &a<newKarma>;Tier &2<tier>&r.");
        configuration.set("tier-change", "[Karma] Ai ajuns la tier &a<tier> &r!");
        configuration.set("self-defending-off", "[Karma] You are defending yourself ! Uarma unchanged.");
        configuration.set("self-defending-on", "[Karma] You are defending yourself but your Karma change.");
        configuration.set("permission-denied", "[Karma] &fDin pacate nu ai &apermisiunea !");

        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
