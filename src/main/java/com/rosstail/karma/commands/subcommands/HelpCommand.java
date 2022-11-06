package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HelpCommand extends SubCommand {

    public HelpCommand(final CommandManager manager) {
        subCommands = manager.getSubCommands();
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_HEADER).replaceAll("%command-syntax%", getSyntax()), null);
    }

    public HelpCommand(final SubCommand subCommand) {
        subCommands = subCommand.getSubCommands();
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_HEADER).replaceAll("%command-syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Displays a list of commands";
    }

    @Override
    public String getSyntax() {
        return "karma help";
    }

    @Override
    public String getPermission() {
        return "karma.command.help";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        StringBuilder helpCommand = new StringBuilder(getHelp());
        for (SubCommand subCommand : subCommands) {
            if (subCommand.getHelp() != null) {
                helpCommand.append("\n").append(subCommand.getHelp());
            }
        }
        sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, helpCommand.toString(), null));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
