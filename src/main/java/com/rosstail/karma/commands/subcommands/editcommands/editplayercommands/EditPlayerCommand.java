package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.EditPlayerKarmaCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands.EditPlayerTierCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.EditPlayerWantedCommand;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
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
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_CHECK).replaceAll("%syntax%", getSyntax()), null);
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
                sender.sendMessage("EditPlayerCommand: Please insert a player name.");
                return;
            }

            StringBuilder message = new StringBuilder("EditPlayerCommand:");
            for (EditPlayerSubCommand subCommand : subCommands) {
                message.append("\n - ").append(subCommand.getName());
            }
            sender.sendMessage(message.toString());
            return;
        }

        String playerName = args[2];
        String subCommandString = args[3];

        if (!subCommandsStringList.contains(subCommandString)) {
            sender.sendMessage("EditPlayerCommand#perform wrong command.");
            return;
        }

        EditPlayerSubCommand subCommand = this.subCommands.get(subCommandsStringList.indexOf(subCommandString));

        Player player;
        player = Bukkit.getPlayerExact(playerName);

        if (player != null && player.isOnline()) {
            PlayerModel model = PlayerDataManager.getPlayerModelMap().get(playerName);
            subCommand.performOnline(sender, model, args, arguments, player);
        } else {
            String playerUUID = PlayerDataManager.getPlayerUUIDFromName(playerName);

            if (playerUUID == null) {
                sender.sendMessage("The player " + playerName + " does not exist");
                return;
            }

            PlayerModel model = StorageManager.getManager().selectPlayerModel(playerUUID);
            //if not, force
            if (CommandManager.doesCommandMatchParameter(arguments, "f", "force")) {

                if (model != null) {
                    subCommand.performOffline(sender, model, args, arguments);
                } else if (CommandManager.doesCommandMatchParameter(arguments, "c", "create")){
                    model = new PlayerModel(playerUUID, playerName);
                    if (!StorageManager.getManager().insertPlayerModel(model)) {
                        System.out.println("problem with the storage.");
                        return;
                    }
                    subCommand.performOffline(sender, model, args, arguments);
                } else {
                    sender.sendMessage("Player does not exist in karma database. Add -c to create player datas");
                }
            } else {
                sender.sendMessage("Player " + playerName + " is disconnected. Use -f to override");
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
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
    public void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {

    }

    @Override
    public void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {

    }
}
