package fr.rosstail.karma;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class and methods of the plugin
 */
public class Karma extends JavaPlugin {
    private static Karma instance;

    public Karma() {
    }

    public void onEnable() {
        System.out.println("==========================");
        System.out.println("===   KARMA  ENABLED   ===");
        System.out.println("==========================");
        instance = this;
        this.saveDefaultConfig();
        this.createFolders();
        Bukkit.getPluginManager().registerEvents(new PlayerConnect(), this);
        this.getCommand("karma").setExecutor(new KarmaCommand());
    }

    /**
     * Create the subfolders inside plugins/Karma/ folder
     */
    public void createFolders() {
        File file = new File(this.getDataFolder(), "playerdata/");
        if (!file.exists()) {
            System.out.println("[Karma] \"playerdata\" folder doesn't exists. Creating it.");
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
        System.out.println("==========================");
        System.out.println("===   KARMA DISABLED   ===");
        System.out.println("==========================");
    }
}
