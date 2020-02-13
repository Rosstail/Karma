package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

/**
 * Launch commands on tier change if it contains
 */
public class TierChangesDispatchCommands {
    private Karma karma = Karma.getInstance();

    public void executeTierChangesCommands(Player player, String tier) {
        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        int playerKarma = configuration.getInt("karma");

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        List<String> cmds = karma.getConfig().getStringList("tiers." + tier + ".commands");
        String tierDisplay = karma.getConfig().getString("tiers." + tier + ".tier-display-name");


        for (String command : cmds) {
            command = command.replaceAll("<player>", player.getName());
            command = command.replaceAll("<karma>", Integer.toString(playerKarma));
            command = command.replaceAll("<tier>", tierDisplay);
            command = ChatColor.translateAlternateColorCodes('&', command);
            if (command.startsWith("<@>")) {
                command = command.replaceAll("<@>", "");
                Bukkit.dispatchCommand(console, command);
            }
            else
                Bukkit.dispatchCommand(player, command);
        }
    }
}