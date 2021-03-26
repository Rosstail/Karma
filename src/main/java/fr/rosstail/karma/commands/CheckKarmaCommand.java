package fr.rosstail.karma.commands;

import fr.rosstail.karma.commands.list.Commands;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This command able the commandSender to see what is the Karma and Karma Tier of a conected user
 */
public class CheckKarmaCommand {

    private final AdaptMessage adaptMessage;
    private final KarmaCommand karmaCommand;

    public CheckKarmaCommand(KarmaCommand karmaCommand) {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
        this.karmaCommand = karmaCommand;
    }

    /**
     * Is used when an argument is used with the command
     * Is necessary if commandSender isn't a player.
     * @param sender
     * @param playerString
     */
    public void karmaOther(CommandSender sender, String playerString)
    {
        if (!karmaCommand.canLaunchCommand(sender, Commands.COMMAND_KARMA_OTHER)) {
            return;
        }
        Player player;

        try {
            player = Bukkit.getServer().getPlayer(playerString);
        } catch (ArrayIndexOutOfBoundsException e) {
            karmaCommand.errorMessage(sender, e);
            return;
        }

        if (player != null && player.isOnline()) {
            sender.sendMessage(adaptMessage.message(player, 0, LangManager.getMessage(LangMessage.CHECK_OTHER_KARMA)));
        } else {
            karmaCommand.disconnectedPlayer(sender);
        }
    }

    /**
     * Used when a player use /karma without argument behind
     * @param sender
     */
    public void karmaSelf(CommandSender sender)
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage(adaptMessage.message(null, 0, LangManager.getMessage(LangMessage.BY_PLAYER_ONLY)));
            return;
        }
        Player player = (Player) sender;
        if (karmaCommand.canLaunchCommand(player, Commands.COMMAND_KARMA_CHECK)) {
            sender.sendMessage(adaptMessage.message(player, 0, LangManager.getMessage(LangMessage.CHECK_OWN_KARMA)));
        }
    }
}
