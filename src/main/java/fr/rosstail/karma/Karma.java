package fr.rosstail.karma;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class and methods of the plugin
 */
public class Karma extends JavaPlugin {
    private static Karma instance;

    public Karma() {
    }

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.createFolders();
        Bukkit.getPluginManager().registerEvents(new PlayerConnect(), this);
        Bukkit.getPluginManager().registerEvents(new KillEvents(), this);
        Bukkit.getPluginManager().registerEvents(new HitEvents(), this);
        this.getCommand("karma").setExecutor(new KarmaCommand());
    }

    /**
     * Create the subfolders inside plugins/Karma/ folder
     */
    public void createFolders() {
        File file = new File(this.getDataFolder(), "lang/");
        if (!file.exists()) {
            file.mkdir();
            System.out.println("Create lang files");
            YamlConfiguration defaultLangFile = YamlConfiguration.loadConfiguration(this.getTextResource("lang/en_EN.yml"));
            file = new File(this.getDataFolder(), "lang/" + defaultLangFile + ".yml");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        file = new File(this.getDataFolder(), "playerdata/");
        if (!file.exists()) {
            String message = this.getConfig().getString("messages.creating-playerdata-folder");
            if (message != null) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                System.out.println(message);
            }
            file.mkdir();
        }
    }

    /**
     * Get the instance to use Karma folder location everytime
     * @return
     */
    public static Karma getInstance() {
        return instance;
    }

    public void onDisable() {
    }
}
