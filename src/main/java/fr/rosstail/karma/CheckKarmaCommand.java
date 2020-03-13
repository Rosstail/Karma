package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * This command able the commandSender to see what is the Karma and Karma Tier of a conected user
 */
public class CheckKarmaCommand {
    private Karma karma = Karma.getInstance();
    SetTier setTier = new SetTier();
    String message = null;

    public CheckKarmaCommand() {
    }

    /**
     * Is used when an argument is used with the command
     * Is necessary if commandSender isn't a player.
     * @param commandSender
     * @param args
     */
    public void karmaOther(CommandSender commandSender, String[] args)
    {
        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (target != null) {
            File file = new File(this.karma.getDataFolder(), "playerdata/" + target.getUniqueId() + ".yml");
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            int targetKarma = configuration.getInt("karma");
            String targetTierDisplay = setTier.checkTier(target);
            message = karma.getConfig().getString("messages.check-other-karma");
            message = message.replaceAll("<karma>", String.valueOf(targetKarma));
            message = message.replaceAll("<tier>", String.valueOf(targetTierDisplay));
        }
        else
            message = karma.getConfig().getString("messages.check-other-karma");

        if (message != null) {
            if (target.isOnline()) {
                message = message.replaceAll("<player>", target.getName());
                message = ChatColor.translateAlternateColorCodes('&', message);
            }
            else {
                message = karma.getConfig().getString("disconnected-player");
                message = ChatColor.translateAlternateColorCodes('&', message);
            }
            commandSender.sendMessage(message);
        }
    }

    /**
     * Used when a player use /karma without argument behind
     * @param commandSender
     */
    public void karmaSelf(CommandSender commandSender)
    {
        Player player = (Player) commandSender;
        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        int playerKarma = configuration.getInt("karma");
        String playerTierDisplay = setTier.checkTier(player);

        message = karma.getConfig().getString("messages.check-own-karma");
        if (message != null) {
            message = message.replaceAll("<player>", player.getName());
            message = message.replaceAll("<karma>", String.valueOf(playerKarma));
            message = message.replaceAll("<tier>", String.valueOf(playerTierDisplay));

            message = ChatColor.translateAlternateColorCodes('&', message);
            player.sendMessage(message);
        }
    }
}