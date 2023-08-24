package com.rosstail.karma.commands.subcommands.checkcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CheckSelfCommand extends SubCommand {

    public CheckSelfCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_CHECK_SELF_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }
    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Check your own Karma status";
    }

    @Override
    public String getSyntax() {
        return "karma check";
    }

    @Override
    public String getPermission() {
        return "karma.command.check.self";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(adaptMessage.adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY)));
            return;
        }
        Player player = (Player) sender;

        sender.sendMessage(adaptMessage.adaptMessage(adaptMessage.adaptPlayerMessage(player, LangManager.getMessage(LangMessage.COMMANDS_CHECK_SELF_RESULT), PlayerType.PLAYER.getText())));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
