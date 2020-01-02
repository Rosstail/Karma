package io.github.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Karma extends JavaPlugin {

    @Override
    public void onEnable() {
        System.out.println("==========================");
        System.out.println("===   KARMA  ENABLED   ===");
        System.out.println("==========================");
        Bukkit.getPluginManager().registerEvents(new PlayerConnect(), this);
        this.saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        System.out.println("==========================");
        System.out.println("===   KARMA DISABLED   ===");
        System.out.println("==========================");
    }
}
