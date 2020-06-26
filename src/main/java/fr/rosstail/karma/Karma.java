package fr.rosstail.karma;

import java.io.File;
import java.io.IOException;
import java.sql.*;

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

    @Override
    public void onLoad() {
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            new WGPreps().worldGuardHook();
        }
    }

    public void onEnable() {

        this.saveDefaultConfig();

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {

                Bukkit.getPluginManager().registerEvents(this, this);

            } else {
                throw new RuntimeException("Could not find PlaceholderAPI!! Plugin can not work without it!");
            }
        }

        if (this.getConfig().getBoolean("mysql.active")) {
            prepareConnection();
        } else {
            this.createPlayerDataFolder();
        }

        this.createLangFiles();
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
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }


    public void setTableToDataBase() {
        String sql = "CREATE TABLE IF NOT EXISTS Karma ( UUID varchar(40) PRIMARY KEY UNIQUE NOT NULL,\n" +
                " NickName varchar(16) NOT NULL,\n" +
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
        configuration.set("creating-player", "[Karma] &9Creating player file for &a<PLAYER>&9.");
        configuration.set("disconnected-player", "[Karma] &cPlayer is not connected or does not exists.");
        configuration.set("check-own-karma", "[Karma] Your karma is &a<KARMA> &rand your tier is &6<TIER>&r.");
        configuration.set("check-other-karma", "[Karma] &6<PLAYER>'s &rkarma is &6<KARMA> &rand his tier is &6<TIER>&r.");
        configuration.set("set-karma", "[Karma] &9<PLAYER>'s &rKarma is now &9<KARMA> &rand his Tier is &9<TIER>&r.");
        configuration.set("add-karma", "[Karma] &aAdded &6<VALUE> &rKarma to &6<PLAYER> &rfor a total of &6<KARMA> &rkarma and the <TIER> tier.");
        configuration.set("remove-karma", "[Karma] &cRemoved &6<VALUE> &rKarma to &6<PLAYER> &rfor a total of &6<KARMA> &rkarma and the <TIER> tier.");
        configuration.set("reset-karma", "[Karma] &6<PLAYER>&r's karma has been reset. Karma : &6<KARMA> &rand tier is &6<TIER>&r.");
        configuration.set("tier-change", "[Karma] You are now a &6<TIER> &r!");
        configuration.set("self-defending-off", "[Karma] You are defending yourself ! Karma unchanged.");
        configuration.set("self-defending-on", "[Karma] You are defending yourself but your Karma changes.");
        configuration.set("permission-denied", "[Karma] &cYou don't have permission !");
        configuration.set("wrong-value", "&c[Karma] You must indicate a number. Example : &f\"/karma add Notch 15\"&c.");
        configuration.set("help", "&b====== &6KARMA HELP &b======\n" +
                "&6/karma (player) &8: &rDisplays targeted player karma and tier or sender by default\n" +
                "&6/karma set [player] [value] &8: &rSet the karma of targeted player to specified value\n" +
                "&6/karma add [player] [value] &8: &rAdd the specified value to the targeted player's karma\n" +
                "&6/karma remove [player] [value] &8: &rsubstract the specified value from the targeted player karma\n" +
                "&6/karma reset [player] &8: &rSet the targeted player karma to the default one.");
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
        configuration.set("creating-player", "[Karma] &9Création du fichier de joueur pour &a<PLAYER>&9.");
        configuration.set("disconnected-player", "[Karma] &cLe joueur est déconnecté ou n'existe pas.");
        configuration.set("check-own-karma", "[Karma] Votre karma est de &a<KARMA> &ret votre alignement est &6<TIER>&r.");
        configuration.set("check-other-karma", "[Karma] Le karma de &6<PLAYER> &rest &6<KARMA> &ret son alignement est &6<TIER>&r.");
        configuration.set("set-karma", "[Karma] Le karma de &9<PLAYER> &rest désormais de &9<KARMA> &ret son alignement est &9<TIER>&r.");
        configuration.set("add-karma", "[Karma] &aAjout de &6<VALUE> &rde karma à &6<PLAYER> &rpour un total de &6<KARMA> &rpoints et l'alignement <TIER>.");
        configuration.set("remove-karma", "[Karma] &cDiminution de &6<VALUE> &rkarma pour &6<PLAYER> &rpour un total de &6<KARMA> &rpoints et l'alignement <TIER>.");
        configuration.set("reset-karma", "[Karma] Le karma du joueur &6<PLAYER> &rest réinitialisé. Karma : &6<KARMA> &ret Alignement : &6<TIER>&r.");
        configuration.set("tier-change", "[Karma] Vous êtes désormais un(e) &6<TIER> &r!");
        configuration.set("self-defending-off", "[Karma] Vous êtes en train de vous défendre ! Karma inchangé.");
        configuration.set("self-defending-on", "[Karma] Vous vous défendez mais votre Karma change tout de même.");
        configuration.set("permission-denied", "[Karma] &cVous n'avez pas la permission !");
        configuration.set("wrong-value", "&c[Karma] Vous devez renseigner un nombre. Exemple : &f\"/karma add Notch 15\"&c.");
        configuration.set("help", "&b====== &6KARMA HELP &b======\n" +
                "&6/karma (joueur) &8: &rAffiche le karma et l'alignement du joueur ciblé ou par défaut.\n" +
                "&6/karma set [joueur] [valeur] &8: &rApplique la valeur spécifiée au karma du joueur ciblé\n" +
                "&6/karma add [joueur] [valeur] &8: &rAjoute la valeur spécifiée au karma du joueur ciblé\n" +
                "&6/karma remove [joueur] [valeur] &8: &rSoustrait la valeur indiquée du karma du joueur ciblé\n" +
                "&6/karma reset [joueur] &8: &rRéinitialise le karma du joueur ciblé.");

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
        configuration.set("creating-player", "[Karma] &9Creando un archivo de jugador para &a<PLAYER>&9.");
        configuration.set("disconnected-player", "[Karma] &cEl jugador no esta conectado o no exite.");
        configuration.set("check-own-karma", "[Karma] Tu karma es &a<KARMA> &ry tu Tier es &6<TIER>&r.");
        configuration.set("check-other-karma", "[Karma] &rEl karma de &6<PLAYER> &res &6<KARMA> &ry su Tier es &6<TIER>&r.");
        configuration.set("set-karma", "[Karma] &rEl karma de &9<PLAYER>'s &rahora es &9<KARMA> &ry su Tier es &9<TIER>&r.");
        configuration.set("add-karma", "[Karma] &aSe añadido &6<VALUE> &rKarma a &6<PLAYER> &rcon un total de &6<KARMA> &rkarma y tier <TIER>.");
        configuration.set("remove-karma", "[Karma] &cEliminado &6<VALUE> &rKarma a &6<PLAYER> &rcon un total de &6<KARMA> &rkarma y tier <TIER>.");
        configuration.set("reset-karma", "[Karma] &rEl karma de &6<PLAYER>&r se ha reiniciado. Karma : &6<KARMA> &ry el tier es &6<TIER>&r.");
        configuration.set("tier-change", "[Karma] ¡ Ahora eres &6<TIER> &r!");
        configuration.set("self-defending-off", "[Karma] You are defending yourself ! Karma unchanged.");
        configuration.set("self-defending-on", "[Karma] You are defending yourself but your Karma changes.");
        configuration.set("permission-denied", "[Karma] &c¡No tienes permiso!");
        configuration.set("wrong-value", "&c[Karma] You must indicate a number. Example : &f\"/karma add Notch 15\"&c.");
        configuration.set("help", "&b====== &6KARMA HELP &b======\n" +
                "&6/karma (player) &8: &rDisplays targeted player karma and tier or sender by default\n" +
                "&6/karma set [player] [value] &8: &rSet the karma of targeted player to specified value\n" +
                "&6/karma add [player] [value] &8: &rAdd the specified value to the targeted player's karma\n" +
                "&6/karma remove [player] [value] &8: &rsubstract the specified value from the targeted player karma\n" +
                "&6/karma reset [player] &8: &rSet the targeted player karma to the default one.");

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
        configuration.set("creating-player", "[Karma] &fCreem un jucator cu numele &a<PLAYER>");
        configuration.set("disconnected-player", "[Karma] &aJucătorul nu este pe server, sau nu exista.");
        configuration.set("check-own-karma", "[Karma] Karma-ul tau este &a<KARMA> &fsi tier-ul tau este &2<TIER>&r.");
        configuration.set("check-other-karma", "[Karma] &a<PLAYER> &fdetine &a<KARMA> &fKarma si tier-ul sau este &2<TIER>&r.");
        configuration.set("set-karma", "[Karma] &9<PLAYER>''s &rAi setat karma &a<KARMA> &rsi tier &2<TIER>&r.");
        configuration.set("add-karma", "[Karma] &fAi scos &a<VALUE> &rKarma de la &2<PLAYER> &rel acum detine de &a<KARMA> &rKarma si tier-ul&2 <TIER>.");
        configuration.set("remove-karma", "[Karma] &cAti scos &6<VALUE> &rkarma de la &6<PLAYER> &rpentru un total de &6<KARMA> &rsi tier-ul <TIER>.");
        configuration.set("reset-karma", "[Karma] &fKarma-ul lui &a<PLAYER>&fa fost resetat. Noul sau Karma este &a<KARMA>;Tier &2<TIER>&r.");
        configuration.set("tier-change", "[Karma] Ai ajuns la tier &a<TIER> &r!");
        configuration.set("self-defending-off", "[Karma] You are defending yourself ! Karma unchanged.");
        configuration.set("self-defending-on", "[Karma] You are defending yourself but your Karma changes.");
        configuration.set("permission-denied", "[Karma] &fDin pacate nu ai &apermisiunea !");
        configuration.set("wrong-value", "&c[Karma] You must indicate a number. Example : &f\"/karma add Notch 15\"&c.");
        configuration.set("help", "&b====== &6KARMA HELP &b======\n" +
                "&6/karma (player) &8: &rDisplays targeted player karma and tier or sender by default\n" +
                "&6/karma set [player] [value] &8: &rSet the karma of targeted player to specified value\n" +
                "&6/karma add [player] [value] &8: &rAdd the specified value to the targeted player's karma\n" +
                "&6/karma remove [player] [value] &8: &rsubstract the specified value from the targeted player karma\n" +
                "&6/karma reset [player] &8: &rSet the targeted player karma to the default one.");

        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
