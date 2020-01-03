package io.github.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerConnect implements Listener {

    @EventHandler
    public void CheckPlayerJoinNumber(PlayerJoinEvent event) {
        String displayPlayerName = event.getPlayer().getName();
        UUID playerId = event.getPlayer().getUniqueId();
        if (! event.getPlayer().hasPlayedBefore() )
            PlayerFirstJoin(playerId, displayPlayerName, event);
        else
            PlayerConnectMessage(displayPlayerName, event);
    }

    public void PlayerFirstJoin(UUID playerId, String displayPlayerName, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.GOLD + "Welcome to " + displayPlayerName +
                " who made his firsts steps into the server !");
        System.out.println(displayPlayerName + " UUID is " + playerId);
    }

    public void PlayerConnectMessage(String displayPlayerName, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.BLUE + displayPlayerName + " is connected.");
    }
}