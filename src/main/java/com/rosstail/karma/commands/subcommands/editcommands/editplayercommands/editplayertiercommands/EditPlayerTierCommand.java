package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands;

import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands.editplayertiersubcommands.EditPlayerTierSetCommand;
import com.rosstail.karma.players.PlayerModel;
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
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_TIER_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
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
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length <= 5) {
            return subCommands.stream().map(EditPlayerTierSubCommand::getName).toList();
        }

        SubCommand subCommand = getSubCommand(subCommands, args[4]);

        if (subCommand == null) {
            return null;
        }

        return subCommand.getSubCommandsArguments(sender, args, arguments);
    }

    @Override
    public void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        if (args.length < 5) {
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        String subCommandString = args[4];

        EditPlayerTierSubCommand editPlayerTierSubCommand = subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(subCommandString))
                .findFirst().orElse(null);

        if (editPlayerTierSubCommand == null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        editPlayerTierSubCommand.performOnline(sender, model, args, arguments, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        if (args.length < 5) {
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        String subCommandString = args[4];

        EditPlayerTierSubCommand editPlayerTierSubCommand = subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(subCommandString))
                .findFirst().orElse(null);

        if (editPlayerTierSubCommand == null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        editPlayerTierSubCommand.performOffline(sender, model, args, arguments);
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
