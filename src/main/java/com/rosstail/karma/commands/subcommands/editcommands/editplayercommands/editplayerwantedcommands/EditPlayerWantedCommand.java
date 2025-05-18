package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands;

import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.editplayerwantedsubcommands.EditPlayerWantedAddCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.editplayerwantedsubcommands.EditPlayerWantedRemoveCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.editplayerwantedsubcommands.EditPlayerWantedResetCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.editplayerwantedsubcommands.EditPlayerWantedSetCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.players.PlayerModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EditPlayerWantedCommand extends EditPlayerSubCommand {
    public List<EditPlayerWantedSubCommand> subCommands = new ArrayList<>();

    public EditPlayerWantedCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new EditPlayerWantedSetCommand());
        subCommands.add(new EditPlayerWantedAddCommand());
        subCommands.add(new EditPlayerWantedRemoveCommand());
        subCommands.add(new EditPlayerWantedResetCommand());
    }

    @Override
    public String getName() {
        return "wanted";
    }

    @Override
    public String getDescription() {
        return "Edit data of specified player";
    }

    @Override
    public String getSyntax() {
        return "karma edit player <player> wanted";
    }

    @Override
    public String getPermission() {
        return "karma.command.edit.player.wanted";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length <= 5) {
            return subCommands.stream().map(EditPlayerWantedSubCommand::getName).toList();
        } else {
            EditPlayerWantedSubCommand editPlayerWantedSubCommand = subCommands.stream()
                    .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[4]))
                    .findFirst().orElse(null);

            if (editPlayerWantedSubCommand == null) {
                return null;
            }

            return editPlayerWantedSubCommand.getSubCommandsArguments(sender, args, arguments);
        }
    }

    @Override
    public void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        if (args.length < 5) {
            StringBuilder message = new StringBuilder("EditPlayerKarmaCommand:");
            for (EditPlayerSubCommand subCommand : subCommands) {
                message.append("\n - ").append(subCommand.getName());
            }
            sender.sendMessage(message.toString());
            return;
        }

        String subCommandString = args[4];
        EditPlayerWantedSubCommand editPlayerWantedSubCommand =
                subCommands.stream().filter(subCommands -> subCommands.getName().equalsIgnoreCase(subCommandString))
                        .findFirst().orElse(null);

        if (editPlayerWantedSubCommand == null) {
            sender.sendMessage("EditPlayerKarmaCommand#performOnline wrong command " + subCommandString);
            return;
        }
        editPlayerWantedSubCommand.performOnline(sender, model, args, arguments, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        if (args.length < 5) {
            StringBuilder message = new StringBuilder("EditPlayerWantedCommand:");
            for (EditPlayerSubCommand subCommand : subCommands) {
                message.append("\n - ").append(subCommand.getName());
            }
            sender.sendMessage(message.toString());
            return;
        }
        String subCommandString = args[4];

        EditPlayerWantedSubCommand editPlayerWantedSubCommand =
                subCommands.stream().filter(subCommands -> subCommands.getName().equalsIgnoreCase(subCommandString))
                        .findFirst().orElse(null);

        if (editPlayerWantedSubCommand == null) {
            sender.sendMessage("EditPlayerKarmaCommand#performOffline wrong command " + subCommandString);
            return;
        }

        editPlayerWantedSubCommand.performOffline(sender, model, args, arguments);
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
