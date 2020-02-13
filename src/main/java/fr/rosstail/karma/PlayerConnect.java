package fr.rosstail.karma;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerConnect implements Listener {
    private Karma karma = Karma.getInstance();
    VerifyKarmaLimits verifyKarmaLimits = new VerifyKarmaLimits();
    SetTier setTier = new SetTier();
    String message = null;

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
        if (!file.exists()) {
            try {
                file.createNewFile();
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                configuration.set("name", player.getName());
                configuration.set("karma", this.karma.getConfig().getInt("karma.default-karma"));
                configuration.save(file);
            } catch (IOException var4) {
                var4.printStackTrace();
            }

            message = karma.getConfig().getString("messages.creating-player-file");
            if (message != null) {
                message = message.replaceAll("<player>", player.getName());
                message = ChatColor.translateAlternateColorCodes('&', message);
                System.out.println(message);
            }
        }

        verifyKarmaLimits.checkKarmaLimit(player);
        setTier.checkTier(player);

    }
}