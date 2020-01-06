package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;

public class PlayerConnect implements Listener {

    private Karma karma;

    public PlayerConnect(Karma karma) {
        this.karma = karma;
    }

    @EventHandler
    public void onCheckPlayerJoinNumber(PlayerJoinEvent event) {
        createPlayerData(event.getPlayer());
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

}