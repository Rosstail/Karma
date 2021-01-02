package fr.rosstail.karma.events;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.datas.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PlayerConnect implements Listener {
    private final Karma plugin;

    public PlayerConnect(Karma plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData.gets(player, plugin).initPlayerData();
    }
}
