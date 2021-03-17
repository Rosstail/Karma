package fr.rosstail.karma.commands;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * This command able the commandSender to see what is the Karma and Karma Tier of a conected user
 */
public class CheckKarmaCommand {

    private final AdaptMessage adaptMessage;

    public CheckKarmaCommand() {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
    }

    /**
     * Is used when an argument is used with the command
     * Is necessary if commandSender isn't a player.
     * @param commandSender
     * @param args
     */
    public void karmaOther(CommandSender commandSender, String[] args)
    {
        String message;
        Player player = Bukkit.getServer().getPlayer(args[0]);

        if (player != null && player.isOnline()) {
            message = LangManager.getMessage(LangMessage.CHECK_OTHER_KARMA);
        } else {
            message = LangManager.getMessage(LangMessage.DISCONNECTED);
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
        adaptMessage.message(player, player, 0, LangManager.getMessage(LangMessage.CHECK_OWN_KARMA));
    }
}
