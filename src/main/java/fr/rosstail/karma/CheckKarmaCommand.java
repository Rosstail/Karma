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
            message ="[Karma] " + target.getName() + "'s Karma is " + targetKarma + " and his Tier is " + targetTierDisplay + ".";
        }
        else
            message = "[Karma] The player \"" + args[0] + "\" doesn't exists.";

        message = ChatColor.translateAlternateColorCodes('&', message);
        commandSender.sendMessage(message);
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
        message = "[Karma] Your own Karma is " + playerKarma + " and your actual Tier is " + playerTierDisplay + ".";

        message = ChatColor.translateAlternateColorCodes('&', message);
        player.sendMessage(message);
    }
}