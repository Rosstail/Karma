package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands;

import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands.EditPlayerKarmaAddCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands.EditPlayerKarmaRemoveCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands.EditPlayerKarmaResetCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands.EditPlayerKarmaSetCommand;
import com.rosstail.karma.players.PlayerModel;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EditPlayerKarmaCommand extends EditPlayerSubCommand {
    public List<EditPlayerKarmaSubCommand> subCommands = new ArrayList<>();

    public EditPlayerKarmaCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_KARMA_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new EditPlayerKarmaSetCommand());
        subCommands.add(new EditPlayerKarmaAddCommand());
        subCommands.add(new EditPlayerKarmaRemoveCommand());
        subCommands.add(new EditPlayerKarmaResetCommand());
    }
    @Override
    public String getName() {
        return "karma";
    }

    @Override
    public String getDescription() {
        return "Edit data of specified player";
    }

    @Override
    public String getSyntax() {
        return "karma edit player <player> karma";
    }

    @Override
    public String getPermission() {
        return "karma.command.edit.player.karma";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length <= 5) {
            return subCommands.stream().map(EditPlayerKarmaSubCommand::getName).toList();
        } else {
            EditPlayerKarmaSubCommand editPlayerKarmaSubCommand = subCommands.stream()
                    .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[4]))
                    .findFirst().orElse(null);

            if (editPlayerKarmaSubCommand == null) {
                return null;
            }

            return editPlayerKarmaSubCommand.getSubCommandsArguments(sender, args, arguments);
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

        EditPlayerSubCommand editPlayerSubCommand = subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(subCommandString))
                .findFirst().orElse(null);

        if (editPlayerSubCommand == null) {
            sender.sendMessage("EditPlayerKarmaCommand#performOnline wrong command " + subCommandString);
            return;
        }
        editPlayerSubCommand.performOnline(sender, model, args, arguments, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        if (args.length < 5) {
            sender.sendMessage(getSubCommandHelp());
            return;
        }
        String subCommandString = args[4];

        EditPlayerSubCommand editPlayerSubCommand = subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(subCommandString))
                .findFirst().orElse(null);

        if (editPlayerSubCommand == null) {
            sender.sendMessage("EditPlayerKarmaCommand#performOffline wrong command " + subCommandString);
            return;
        }
        editPlayerSubCommand.performOffline(sender, model, args, arguments);
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
