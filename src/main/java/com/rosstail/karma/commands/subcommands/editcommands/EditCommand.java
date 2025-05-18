package com.rosstail.karma.commands.subcommands.editcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class EditCommand extends EditSubCommand {
    public List<EditSubCommand> subCommands = new ArrayList<>();

    public EditCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EDIT_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new EditPlayerCommand());
    }


    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        EditSubCommand subCommand = (EditSubCommand) getSubCommand(subCommands, args[1]);

        if (subCommand == null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        subCommand.perform(sender, args, arguments);
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length <= 2) {
            return subCommands.stream().map(EditSubCommand::getName).toList();
        } else {
            SubCommand editSubCommand = subCommands.stream()
                    .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[1]))
                    .findFirst().orElse(null);

            if (editSubCommand == null) {
                return null;
            }

            return editSubCommand.getSubCommandsArguments(sender, args, arguments);
        }
    }

    @Override
    public String getSubCommandHelp() {
        StringBuilder subCommandHelp = new StringBuilder(super.getSubCommandHelp());
        for (SubCommand subCommand : subCommands) {
            if (subCommand.getHelp() != null) {
                subCommandHelp.append("\n").append(subCommand.getHelp());
            }
        }
        return subCommandHelp.toString();
    }
}