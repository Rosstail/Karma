package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.players.PlayerDataManager;
import com.rosstail.karma.players.PlayerModel;
import com.rosstail.karma.storage.StorageManager;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.events.karmaevents.PlayerOverTimeResetEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EditPlayerKarmaRemoveCommand extends EditPlayerKarmaSetCommand {

    public EditPlayerKarmaRemoveCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_KARMA_REMOVE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove karma from player's karma";
    }

    @Override
    public String getSyntax() {
        return "karma edit remove <player> <value> (-d -o -g -s)";
    }

    @Override
    public String getPermission() {
        return "karma.command.edit";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
    }

    @Override
    public void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeOnlineKarma(sender, model, args, arguments, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeOfflineKarma(sender, model, args, arguments);
    }

    public void changeOnlineKarma(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        float value;
        boolean silent;

        if (args.length < 6) {
            sender.sendMessage("Set a numerical value");
            return;
        }

        try {
            value = model.getKarma() - Float.parseFloat(args[5]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
            return;
        }

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            value = PlayerDataManager.limitKarma(value);
        }

        silent = CommandManager.doesCommandMatchParameter(arguments, "s", "silent");

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, model, value, silent);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        if (!CommandManager.doesCommandMatchParameter(arguments, "r", "reset")) {
            ConfigData.getConfigData().overtime.overtimeLoopMap.forEach((s, overtimeLoop) -> {
                PlayerDataManager.setOverTimeStamp(model, s, overtimeLoop.firstTimer);
                PlayerOverTimeResetEvent overTimeResetEvent = new PlayerOverTimeResetEvent(player, overtimeLoop.name);
                Bukkit.getPluginManager().callEvent(overTimeResetEvent);
            });
        }
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        String message = adaptMessage.adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_KARMA_REMOVE_RESULT), PlayerType.PLAYER.getText());
        sender.sendMessage(adaptMessage.adaptMessage(message));
    }

    public void changeOfflineKarma(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        float value;

        if (args.length < 6) {
            sender.sendMessage("Set a numerical value");
            return;
        }

        try {
            value = model.getKarma() - Float.parseFloat(args[5]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
            return;
        }

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            value = PlayerDataManager.limitKarma(value);
        } else {
            sender.sendMessage("new karma value is not limited.");
        }

        model.setPreviousKarma(model.getKarma());
        model.setKarma(value);
        StorageManager.getManager().updatePlayerModel(model, true);

        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        String message = adaptMessage.adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_KARMA_REMOVE_RESULT), PlayerType.PLAYER.getText());
        sender.sendMessage(adaptMessage.adaptMessage(message));    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(0));
        return list;
    }
}
