package com.rosstail.karma.commands.subcommands.checkcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.checkcommands.CheckOtherCommand;
import com.rosstail.karma.commands.subcommands.checkcommands.CheckSelfCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CheckCommand extends SubCommand {

    public CheckCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_CHECK).replaceAll("%syntax%", getSyntax()), null);
        subCommands.add(new CheckSelfCommand());
        subCommands.add(new CheckOtherCommand());
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Check karma status";
    }

    @Override
    public String getSyntax() {
        return "karma check (player)";
    }

    @Override
    public String getPermission() {
        return "karma.command.check";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length > 1) {
            subCommands.get(1).perform(sender, args);
        } else {
            subCommands.get(0).perform(sender, args);
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
