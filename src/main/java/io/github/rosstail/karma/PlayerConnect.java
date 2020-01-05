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
    private Karma karma;

    public PlayerConnect(Karma karma) {
        this.karma = karma;
    }

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

    public void playerFirstJoin(UUID playerId, String displayPlayerName, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.GOLD + "Welcome to " + displayPlayerName +
                " who made his firsts steps into the server !");
        System.out.println(displayPlayerName + " UUID is " + playerId);
    }

    public void playerConnectMessage(String displayPlayerName, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.BLUE + displayPlayerName + " is connected.");
    }

    public void createPlayerData(UUID playerId) {
        File file = new File(karma.getDataFolder(), "playerdata/" + playerId + ".yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[Karma] Create new user file " + file + " .");
        }
        customFile = YamlConfiguration.loadConfiguration(file);
    }

}