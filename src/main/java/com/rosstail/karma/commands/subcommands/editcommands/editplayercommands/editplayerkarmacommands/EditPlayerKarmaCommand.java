package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands;

import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands.EditPlayerKarmaAddCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands.EditPlayerKarmaRemoveCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands.EditPlayerKarmaResetCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands.EditPlayerKarmaSetCommand;
import com.rosstail.karma.datas.PlayerModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EditPlayerKarmaCommand extends EditPlayerSubCommand {
    public List<EditPlayerKarmaSubCommand> subCommands = new ArrayList<>();

    public EditPlayerKarmaCommand() {
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
    public void perform(CommandSender sender, String[] args) {
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }

    @Override
    public void performOnline(CommandSender sender, PlayerModel model, String[] args, Player player) {
        List<String> subCommandsStringList = new ArrayList<>();
        for (EditPlayerSubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (args.length < 5) {
            StringBuilder message = new StringBuilder("EditPlayerKarmaCommand:");
            for (EditPlayerSubCommand subCommand : subCommands) {
                message.append("\n - ").append(subCommand.getName());
            }
            sender.sendMessage(message.toString());
            return;
        }
        String subCommandString = args[4];

        if (!subCommandsStringList.contains(subCommandString)) {
            sender.sendMessage("EditPlayerKarmaCommand#performOnline wrong command " + subCommandString);
        }
        subCommands.get(subCommandsStringList.indexOf(subCommandString)).performOnline(sender, model, args, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerModel model, String[] args) {
        List<String> subCommandsStringList = new ArrayList<>();
        for (EditPlayerSubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (args.length < 5) {
            StringBuilder message = new StringBuilder("EditPlayerCommand:");
            for (EditPlayerSubCommand subCommand : subCommands) {
                message.append("\n - ").append(subCommand.getName());
            }
            sender.sendMessage(message.toString());
            return;
        }
        String subCommandString = args[4];

        if (!subCommandsStringList.contains(subCommandString)) {
            sender.sendMessage("EditPlayerKarmaCommand#performOffline wrong command " + subCommandString);
            System.out.println(subCommandsStringList);
            return;
        }
        subCommands.get(subCommandsStringList.indexOf(subCommandString)).performOffline(sender, model, args);
    }
}
