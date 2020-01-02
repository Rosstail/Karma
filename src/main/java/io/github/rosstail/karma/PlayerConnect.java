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
            PlayerFirstWelcomeMessage(displayPlayerName, event);
        else
            PlayerConnectMessage(displayPlayerName, playerId, event);
    }

    public void PlayerFirstWelcomeMessage(String displayPlayerName, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.GOLD + "Welcome to " + displayPlayerName +
                " who made his firsts steps into the server !");
    }

    public void PlayerConnectMessage(String displayPlayerName, UUID playerId, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.BLUE + displayPlayerName + " is connected with the UUID " + playerId + ".");
    }
}