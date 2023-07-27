package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.EditCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HelpCommand extends SubCommand {

    public HelpCommand(final CommandManager manager) {
        subCommands = manager.getSubCommands();
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_HEADER)
                        .replaceAll("%syntax%", getSyntax())
                        .replaceAll("%permission%", getPermission()));
    }

    public HelpCommand(final SubCommand subCommand) {
        System.out.println("BBBB " + subCommand.subCommands.size());
        if (subCommand instanceof EditCommand) {
            System.out.println("CCC " + ((EditCommand) subCommand).subCommands.size());
        }
        subCommands = subCommand.subCommands;
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_HEADER)
                        .replaceAll("%syntax%", getSyntax())
                        .replaceAll("%permission%", getPermission()));
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
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        StringBuilder helpCommand = new StringBuilder(getHelp());
        for (SubCommand subCommand : subCommands) {
            sender.sendMessage("subcommand " + subCommand);
            if (subCommand.getHelp() != null) {
                sender.sendMessage("subcommandhelp " + subCommand.getHelp());
                helpCommand.append("\n").append(subCommand.getHelp());
            }
        }
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(helpCommand.toString()));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
