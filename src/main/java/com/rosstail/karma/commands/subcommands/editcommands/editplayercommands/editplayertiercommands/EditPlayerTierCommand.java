package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands;

import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.HelpCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands.editplayertiersubcommands.EditPlayerTierSetCommand;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EditPlayerTierCommand extends EditPlayerSubCommand {
    public List<EditPlayerTierSubCommand> subCommands = new ArrayList<>();

    public EditPlayerTierCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("%desc%", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_TIER_DESC))
                        .replaceAll("%syntax%", getSyntax()));
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
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        if (args.length <= 5) {
            List<String> list = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
            }
            return list;
        } else {
            for (SubCommand subCommand : subCommands) {
                if (args[4].equalsIgnoreCase(subCommand.getName())) {
                    return subCommand.getSubCommandsArguments(sender, args, arguments);
                }
            }
        }
        return null;
    }

    @Override
    public void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        List<String> subCommandsStringList = new ArrayList<>();
        for (EditPlayerTierSubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (args.length < 5) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args, null);
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
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args, null);
            return;
        }
        String subCommandString = args[4];

        if (!subCommandsStringList.contains(subCommandString)) {
            sender.sendMessage("EditPlayerTierCommand#performOffline wrong command " + subCommandString);
            return;
        }
        subCommands.get(subCommandsStringList.indexOf(subCommandString)).performOffline(sender, model, args, arguments);
    }
}
