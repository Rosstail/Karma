package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.editplayerwantedsubcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.EditPlayerWantedSubCommand;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.events.karmaevents.PlayerWantedChangeEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.*;

public class EditPlayerWantedAddCommand extends EditPlayerWantedSubCommand {

    public EditPlayerWantedAddCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("%desc%", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_ADD_DESC))
                        .replaceAll("%syntax%", getSyntax()));
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Add wanted time to player";
    }

    @Override
    public String getSyntax() {
        return "karma edit player <player> wanted add <values> (-f -o -c)";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {

    }

    @Override
    public void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeWantedOnline(sender, model, args, arguments, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeWantedOffline(sender, model, args, arguments);
    }

    private void changeWantedOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        long wantedTime = model.getWantedTimeStamp().getTime();
        List<String> expressionList = new ArrayList<>(Arrays.asList(args));
        expressionList.remove("edit");
        expressionList.remove("player");
        expressionList.remove(model.getUsername());
        expressionList.remove("wanted");
        expressionList.remove("add");
        String expression = String.join(" ", expressionList).trim();

        long duration = AdaptMessage.calculateDuration(wantedTime, expression);
        long baseDuration = model.getWantedTimeStamp().getTime();
        long newDuration = baseDuration + duration;

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            long limiter = AdaptMessage.calculateDuration(wantedTime, ConfigData.getConfigData().wanted.wantedMaxDurationExpression);
            newDuration = Math.min(newDuration, limiter);
        }

        PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, model, new Timestamp(newDuration));
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);

        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_ADD_RESULT), PlayerType.PLAYER.getText()));
    }

    private void changeWantedOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        List<String> expressionList = new ArrayList<>(Arrays.asList(args));
        expressionList.remove("edit");
        expressionList.remove("player");
        expressionList.remove(model.getUsername());
        expressionList.remove("wanted");
        expressionList.remove("add");
        String expression = String.join(" ", expressionList).trim();

        long wantedTime = model.getWantedTimeStamp().getTime();
        long duration = AdaptMessage.calculateDuration(wantedTime, expression);
        long baseDuration = model.getWantedTimeStamp().getTime();
        long newDuration = baseDuration + duration;

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            long limiter = AdaptMessage.calculateDuration(wantedTime, ConfigData.getConfigData().wanted.wantedMaxDurationExpression);
            newDuration = Math.min(newDuration, limiter);
        }

        model.setWantedTimeStamp(new Timestamp(newDuration));
        StorageManager.getManager().updatePlayerModel(model);


        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_ADD_RESULT), PlayerType.PLAYER.getText()));

        if (model.isWanted()) {
            if (model.getWantedTimeStamp().getTime() <= System.currentTimeMillis()) {
                sender.sendMessage(" He will become INNOCENT upon reconnect");
            } else {
                sender.sendMessage("His wanted status will be refreshed upon reconnect");
            }
        } else if (model.getWantedTimeStamp().getTime() > System.currentTimeMillis()) {
            sender.sendMessage("His wanted level will become WANTED upon reconnect");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return Collections.singletonList("xd xh xm xs xms");
    }
}
