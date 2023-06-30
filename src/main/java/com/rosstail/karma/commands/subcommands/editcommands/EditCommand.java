package com.rosstail.karma.commands.subcommands.editcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
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
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_CHECK).replaceAll("%syntax%", getSyntax()), null);
        subCommands.add(new EditPlayerCommand());
        //subCommands.add(new EditTierSubCommand());
        //subCommands.add(new EditWantedSubCommand());
    }


    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 2) {
            StringBuilder message = new StringBuilder("EditCommand:");
            for (EditSubCommand subCommand : subCommands) {
                message.append("\n - ").append(subCommand.getName());
            }
            sender.sendMessage(message.toString());
            return;
        }
        for (EditSubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[1])) {
                subCommand.perform(sender, args);
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        for (SubCommand subCommand : subCommands) {
            List<String> list = new ArrayList<>();
            list.add(subCommand.getName());
            return list;
        }
        return null;
    }
}