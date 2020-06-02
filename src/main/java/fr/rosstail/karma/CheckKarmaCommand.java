package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * This command able the commandSender to see what is the Karma and Karma Tier of a conected user
 */
public class CheckKarmaCommand extends GetSet {
    private Karma karma = Karma.get();
    String message = null;
    AdaptMessage adaptMessage = new AdaptMessage();
    File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
    YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);

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
        Player player = Bukkit.getServer().getPlayer(args[0]);

        if (player != null && player.isOnline()) {
            message = configurationLang.getString("check-other-karma");
        } else {
            message = configurationLang.getString("disconnected-player");
        }
        adaptMessage.message(commandSender, player, 0, message);
    }

    /**
     * Used when a player use /karma without argument behind
     * @param sender
     */
    public void karmaSelf(CommandSender sender)
    {
        Player player = (Player) sender;

        message = configurationLang.getString("check-own-karma");
        adaptMessage.message(player, player, 0, message);
    }
}