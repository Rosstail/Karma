package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Launch commands on tier change if it contains
 */
public class TierChangesDispatchCommands {
    private Karma karma = Karma.getInstance();

    public void executeTierChangesCommands(Player player, String tier) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        List<String> cmds = karma.getConfig().getStringList("tiers." + tier + ".commands");

        for (String command : cmds) {
            command = command.replaceAll("<player>", player.getName());
            if (command.contains("<@>")) {
                command = command.replaceAll("<@>", "");
                Bukkit.dispatchCommand(console, command);
            }
            else
                Bukkit.dispatchCommand(player, command);
        }
    }
}
