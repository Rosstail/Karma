package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.sql.SQLException;


public class PlayerConnect implements Listener {
    private final Karma plugin;
    private final File langFile;
    private final YamlConfiguration configLang;
    private final AdaptMessage adaptMessage;

    PlayerConnect(Karma plugin) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder(),
            "lang/" + plugin.getConfig().getString("general.lang") + ".yml");
        this.configLang = YamlConfiguration.loadConfiguration(langFile);
        this.adaptMessage = new AdaptMessage(plugin);
    }


    @EventHandler public void onPlayerJoin(PlayerJoinEvent event) {
        this.createPlayerData(event.getPlayer());
    }

    /**
     * Create the player datas inside Karma/playerdata/ folder if his file doens't already exists.
     * Check on connection if his karma is in the limit fork.
     *
     * @param player
     */
    public void createPlayerData(Player player) {
        GetSet playerData = GetSet.gets(player, plugin);
        File file = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        try {
            if (file.exists() || plugin.connection != null && !plugin.connection.isClosed()) {
                playerData.initPlayerData();
            } else {
                playerData.createPlayerLocaleFile(player, file);
                String message = configLang.getString("creating-player");
                adaptMessage.message(null, player, 0, message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
