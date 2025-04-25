package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.EditPlayerKarmaCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands.EditPlayerTierCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.EditPlayerWantedCommand;
import com.rosstail.karma.players.PlayerDataManager;
import com.rosstail.karma.players.PlayerDataModel;
import com.rosstail.karma.storage.StorageManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EditPlayerCommand extends EditPlayerSubCommand {
    List<EditPlayerSubCommand> subCommands = new ArrayList<>();

    public EditPlayerCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new EditPlayerKarmaCommand());
        subCommands.add(new EditPlayerTierCommand());
        subCommands.add(new EditPlayerWantedCommand());
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        List<String> subCommandsStringList = new ArrayList<>();
        for (EditPlayerSubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }


        if (args.length < 4) {
            if (args.length < 3) {
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_INSERT_PLAYER_NAME)));
                return;
            }

            sender.sendMessage(getSubCommandHelp());
            return;
        }

        String playerName = args[2];
        String subCommandString = args[3];

        if (!subCommandsStringList.contains(subCommandString)) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        EditPlayerSubCommand subCommand = this.subCommands.get(subCommandsStringList.indexOf(subCommandString));

        Player player;
        player = Bukkit.getPlayerExact(playerName);

        if (player != null && player.isOnline()) {
            PlayerDataModel model = PlayerDataManager.getPlayerModelMap().get(playerName);
            subCommand.performOnline(sender, model, args, arguments, player);
        } else {
            String playerUUID = PlayerDataManager.getPlayerUUIDFromName(playerName);

            if (playerUUID == null) {
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_DOES_NOT_EXIST).replaceAll("\\[player]", playerName)));
                return;
            }

            PlayerDataModel model = StorageManager.getManager().selectPlayerModel(playerUUID);
            //if not, force
            if (CommandManager.doesCommandMatchParameter(arguments, "d", "disconnect")) {

                if (model != null) {
                    subCommand.performOffline(sender, model, args, arguments);
                } else if (CommandManager.doesCommandMatchParameter(arguments, "g", "generate")){
                    model = new PlayerDataModel(playerUUID, playerName);
                    if (!StorageManager.getManager().uploadPlayerModel(model)) {
                        AdaptMessage.print("Problem with the storage.", AdaptMessage.prints.WARNING);
                        return;
                    }
                    subCommand.performOffline(sender, model, args, arguments);
                } else {
                    sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_NO_DATA).replaceAll("\\[player]", playerName)));
                }
            } else {
                CommandManager.disconnectedPlayer(sender, playerName);
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length == 4) {
            List<String> list = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
            }
            return list;
        } else if (args.length >= 5) {
            for (SubCommand subCommand : subCommands) {
                if (args[3].equalsIgnoreCase(subCommand.getName())) {
                    return subCommand.getSubCommandsArguments(sender, args, arguments);
                }
            }
        }
        return null;
    }

    @Override
    public void performOnline(CommandSender sender, PlayerDataModel model, String[] args, String[] arguments, Player player) {

    }

    @Override
    public void performOffline(CommandSender sender, PlayerDataModel model, String[] args, String[] arguments) {

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
