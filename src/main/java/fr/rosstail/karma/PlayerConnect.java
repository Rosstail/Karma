package fr.rosstail.karma;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PlayerConnect implements Listener {
    private final Karma plugin;

    PlayerConnect(Karma plugin) {
        this.plugin = plugin;
    }

    @EventHandler public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DataHandler playerData = DataHandler.gets(player, plugin);
        playerData.initPlayerData();
    }
}
