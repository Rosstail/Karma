package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands;

import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands.editplayertiersubcommands.EditPlayerTierSetCommand;
import com.rosstail.karma.datas.PlayerModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EditPlayerTierCommand extends EditPlayerSubCommand {
    public List<EditPlayerTierSubCommand> subCommands = new ArrayList<>();

    public EditPlayerTierCommand() {
        subCommands.add(new EditPlayerTierSetCommand());
    }

    @Override
    public String getName() {
        return "tier";
    }

    @Override
    public String getDescription() {
        return "manage tier of player";
    }

    @Override
    public String getSyntax() {
        return "karma edit player <player> tier";
    }

    @Override
    public String getPermission() {
        return "karma.command.edit.player.tier";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }

    @Override
    public void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        List<String> subCommandsStringList = new ArrayList<>();
        for (EditPlayerTierSubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (args.length < 5) {
            StringBuilder message = new StringBuilder("EditPlayerTierCommand:");
            for (EditPlayerSubCommand subCommand : subCommands) {
                message.append("\n - ").append(subCommand.getName());
            }
            sender.sendMessage(message.toString());
            return;
        }
        String subCommandString = args[4];

        if (!subCommandsStringList.contains(subCommandString)) {
            sender.sendMessage("EditPlayerTierCommand#performOnline wrong command " + subCommandString);
        }
        subCommands.get(subCommandsStringList.indexOf(subCommandString)).performOnline(sender, model, args, arguments, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        List<String> subCommandsStringList = new ArrayList<>();
        for (EditPlayerSubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (args.length < 5) {
            StringBuilder message = new StringBuilder("EditPlayerTierCommand:");
            for (EditPlayerSubCommand subCommand : subCommands) {
                message.append("\n - ").append(subCommand.getName());
            }
            sender.sendMessage(message.toString());
            return;
        }
        String subCommandString = args[4];

        if (!subCommandsStringList.contains(subCommandString)) {
            sender.sendMessage("EditPlayerTierCommand#performOffline wrong command " + subCommandString);
            System.out.println(subCommandsStringList);
            return;
        }
        subCommands.get(subCommandsStringList.indexOf(subCommandString)).performOffline(sender, model, args, arguments);
    }
}
