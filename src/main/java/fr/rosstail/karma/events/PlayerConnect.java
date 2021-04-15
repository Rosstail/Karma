package fr.rosstail.karma.events;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.datas.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerConnect implements Listener {
    private final Karma plugin;
    private final ConfigData configData = ConfigData.getConfigData();
    public PlayerConnect(Karma plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int delay = configData.getSaveDelay();
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player, plugin);
        playerData.initPlayerData();
        playerData.setUpdateDataTimer(delay);
        playerData.setOverTimerChange();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player, plugin);
        playerData.getUpdateDataTimer().cancel();
        playerData.updateData();
        playerData.stopOverTimer();
    }
}
