package fr.rosstail.karma.commands;

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
     * @param args
     */
    public void karmaOther(CommandSender sender, String[] args)
    {
        if (!karmaCommand.canLaunchCommand(sender, Commands.COMMAND_KARMA_OTHER)) {
            return;
        }
        Player player;

        try {
            player = Bukkit.getServer().getPlayer(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            karmaCommand.errorMessage(sender, e);
            return;
        }

        if (player != null && player.isOnline()) {
            adaptMessage.message(sender, player, 0, LangManager.getMessage(LangMessage.CHECK_OTHER_KARMA));
        } else {
            karmaCommand.disconnectedPlayer(sender, args);
        }
    }

    /**
     * Used when a player use /karma without argument behind
     * @param sender
     */
    public void karmaSelf(CommandSender sender)
    {
        if (!(sender instanceof Player)) {
            adaptMessage.message(sender, null, 0, LangManager.getMessage(LangMessage.BY_PLAYER_ONLY));
            return;
        }
        Player player = (Player) sender;
        if (karmaCommand.canLaunchCommand(player, Commands.COMMAND_KARMA)) {
            adaptMessage.message(sender, player, 0, LangManager.getMessage(LangMessage.CHECK_OWN_KARMA));
        }
    }
}
