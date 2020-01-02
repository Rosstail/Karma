package io.github.rosstail.karma;

import org.bukkit.plugin.java.JavaPlugin;

public class Karma extends JavaPlugin {

    @Override
    public void onEnable() {
        System.out.println("==========================");
        System.out.println("===   KARMA  ENABLED   ===");
        System.out.println("==========================");
    }

    @Override
    public void onDisable() {
        System.out.println("==========================");
        System.out.println("===   KARMA DISABLED   ===");
        System.out.println("==========================");
    }
}
