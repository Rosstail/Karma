package fr.rosstail.karma;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerConnect implements Listener {
    private Karma karma = Karma.getInstance();
    ChangeKarma changeKarma = new ChangeKarma();

    public PlayerConnect() {
    }

    @EventHandler
    public void onCheckPlayerJoinNumber(PlayerJoinEvent event) {
        this.createPlayerData(event.getPlayer());
    }

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

            System.out.println("[Karma] Create new user file " + file + ".");
        }

        this.changeKarma.checkKarmaLimit(player);
    }
}
