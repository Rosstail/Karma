package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Karma extends JavaPlugin {

    @Override
    public void onEnable() {
        System.out.println("==========================");
        System.out.println("===   KARMA  ENABLED   ===");
        System.out.println("==========================");
        this.saveDefaultConfig();
        createFolders();
        Bukkit.getPluginManager().registerEvents(new PlayerConnect(this), this);
    }

    public void createFolders() {
        File file = new File(this.getDataFolder(), "playerdata/");
        if ( !file.exists() ) {
            System.out.println("[Karma] \"playerdata\" folder doesn't exists. Creating it.");
            file.mkdir();
        }
    }

    @Override
    public void onDisable() {
        System.out.println("==========================");
        System.out.println("===   KARMA DISABLED   ===");
        System.out.println("==========================");
    }

}
