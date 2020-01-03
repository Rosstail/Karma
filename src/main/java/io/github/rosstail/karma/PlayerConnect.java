package io.github.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerConnect implements Listener {

    private static File file;
    private static FileConfiguration customFile;

    @EventHandler
    public void CheckPlayerJoinNumber(PlayerJoinEvent event) {
        String displayPlayerName = event.getPlayer().getName();
        UUID playerId = event.getPlayer().getUniqueId();

        createPlayerData(playerId);
        if (! event.getPlayer().hasPlayedBefore() )
            playerFirstJoin(playerId, displayPlayerName, event);
        else
            playerConnectMessage(displayPlayerName, event);
    }

    public static void createFolders() {
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Karma")
                .getDataFolder().getPath() + System.getProperty("file.separator") + "playerdata");
        if ( !file.exists() ) {
            System.out.println("[Karma] \"playerdata\" folder doesn't exists. Creating it.");
            file.mkdir();
        }
    }

    public void playerFirstJoin(UUID playerId, String displayPlayerName, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.GOLD + "Welcome to " + displayPlayerName +
                " who made his firsts steps into the server !");
        System.out.println(displayPlayerName + " UUID is " + playerId);
    }

    public void playerConnectMessage(String displayPlayerName, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.BLUE + displayPlayerName + " is connected.");
    }

    public void createPlayerData(UUID playerId) {
        System.out.println("TEST CREATE FILE");
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Karma").getDataFolder()
                .getPath() + System.getProperty("file.separator") + "playerdata/" + playerId + ".yml");

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                //oww
            }
            System.out.println("PATH : " + file);
        }
        customFile = YamlConfiguration.loadConfiguration(file);
    }

}