package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerConnect implements Listener {

    private Karma karma;

    public PlayerConnect(Karma karma) {
        this.karma = karma;
    }

    @EventHandler
    public void onCheckPlayerJoinNumber(PlayerJoinEvent event) {
        createPlayerData(event.getPlayer());
        /*if (! event.getPlayer().hasPlayedBefore() )
            playerFirstJoin(player, event);
        else
            playerConnectMessage(player, event);*/
    }

    public void createPlayerData(Player player) {
        File file = new File(karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                configuration.set("name", player.getName());
                configuration.set("karma", new Integer(karma.getConfig().getInt("karma.default-karma")));
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[Karma] Create new user file " + file + ".");
        }
    }

    /*public void playerFirstJoin(Player player, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.GOLD + "Welcome to " + player.getName() +
                " who made his firsts steps into the server !");
        System.out.println(player.getName() + " UUID is " + player.getUniqueId());
    }

    public void playerConnectMessage(Player player, PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.BLUE + player.getName() + " is connected.");
    }*/

}