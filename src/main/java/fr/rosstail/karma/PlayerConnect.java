package fr.rosstail.karma;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PlayerConnect extends GetSet implements Listener {

    private Karma karma = Karma.get();
    String message = null;

    File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
    YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);

    public PlayerConnect() {
    }

    @EventHandler
    public void onCheckPlayerJoinNumber(PlayerJoinEvent event) {
        this.createPlayerData(event.getPlayer());
    }

    /**
     * Create the player datas inside Karma/playerdata/ folder if his file doens't already exists.
     * Check on connection if his karma is in the limit fork.
     * @param player
     */
    public void createPlayerData(Player player) {
        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {
                initPlayerData(player);
            } else {
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                    configuration.set("name", player.getName());
                    configuration.set("karma", this.karma.getConfig().getDouble("karma.default-karma"));
                    try {
                        configuration.save(file);
                        setTierToPlayer(player);
                    } catch (IOException var4) {
                        var4.printStackTrace();
                    }

                    message = configurationLang.getString("creating-player");
                    if (message != null) {
                        message = message.replaceAll("<player>", player.getName());
                        message = ChatColor.translateAlternateColorCodes('&', message);
                        System.out.println(message);
                    }
                }
            }
        } catch (
            SQLException e) {
            e.printStackTrace();
        }

    }
}