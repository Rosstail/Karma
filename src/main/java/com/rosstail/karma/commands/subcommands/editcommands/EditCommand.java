package com.rosstail.karma.commands.subcommands.editcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.HelpCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EditCommand extends EditSubCommand {
    public List<EditSubCommand> subCommands = new ArrayList<>();

    public EditCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("%desc%", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_KARMA_DESC))
                        .replaceAll("%syntax%", getSyntax()));
        subCommands.add(new EditPlayerCommand());
    }


    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length < 2) {
            HelpCommand help = new HelpCommand(this);
            sender.sendMessage("sub " + this.subCommands.size());
            help.perform(sender, args, null);
            return;
        }

        for (EditSubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[1])) {
                subCommand.perform(sender, args, arguments);
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        if (args.length <= 2) {
            List<String> list = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
                return list;
            }
        } else {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[1])) {
                    return subCommand.getSubCommandsArguments(sender, args, arguments);
                }
            }
        }
        return null;
    }
}