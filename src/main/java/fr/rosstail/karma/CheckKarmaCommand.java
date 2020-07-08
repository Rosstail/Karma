package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

/**
 * This command able the commandSender to see what is the Karma and Karma Tier of a conected user
 */
public class CheckKarmaCommand {
    String message = null;

    private final File langFile;
    private final YamlConfiguration configLang;
    private final AdaptMessage adaptMessage;

    public CheckKarmaCommand(Karma plugin) {
        this.langFile = new File(plugin.getDataFolder(), "lang/" + plugin.getConfig().getString("general.lang") + ".yml");
        this.configLang = YamlConfiguration.loadConfiguration(langFile);
        this.adaptMessage = new AdaptMessage(plugin);
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
            message = configLang.getString("check-other-karma");
        } else {
            message = configLang.getString("disconnected-player");
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

        message = configLang.getString("check-own-karma");
        adaptMessage.message(player, player, 0, message);
    }
}
