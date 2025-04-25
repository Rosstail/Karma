package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.editplayerwantedsubcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.EditPlayerWantedSubCommand;
import com.rosstail.karma.players.PlayerModel;
import com.rosstail.karma.storage.StorageManager;
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
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_ADD_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
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
        return "karma edit player <player> wanted add <values> (-d -o -g -s)";
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
        boolean silent;
        List<String> expressionList = new ArrayList<>(Arrays.asList(args));
        expressionList.remove("edit");
        expressionList.remove("player");
        expressionList.remove(model.getUsername());
        expressionList.remove("wanted");
        expressionList.remove("add");
        String expression = String.join(" ", expressionList).trim();

        long duration = AdaptMessage.evalDuration(wantedTime, expression);
        long baseDuration = Math.max(model.getWantedTimeStamp().getTime(), System.currentTimeMillis());
        long newDuration = baseDuration + duration;

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            long limiter = AdaptMessage.evalDuration(wantedTime, ConfigData.getConfigData().wanted.wantedMaxDurationExpression);
            newDuration = Math.min(newDuration, limiter);
        }

        silent = CommandManager.doesCommandMatchParameter(arguments, "s", "silent");
        PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, model, new Timestamp(newDuration), silent);
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);

        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        String message = adaptMessage.adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_ADD_RESULT).replaceAll("\\[value]", AdaptMessage.getAdaptMessage().countdownFormatter(duration)), PlayerType.PLAYER.getText());
        sender.sendMessage(adaptMessage.adaptMessage(message));
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
        long duration = AdaptMessage.evalDuration(wantedTime, expression);
        long baseDuration = Math.max(model.getWantedTimeStamp().getTime(), System.currentTimeMillis());
        long newDuration = baseDuration + duration;

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            long limiter = AdaptMessage.evalDuration(wantedTime, ConfigData.getConfigData().wanted.wantedMaxDurationExpression);
            newDuration = Math.min(newDuration, limiter);
        }

        model.setWantedTimeStamp(new Timestamp(newDuration));
        StorageManager.getManager().updatePlayerModel(model, true);

        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        String message = adaptMessage.adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_ADD_RESULT).replaceAll("\\[value]", AdaptMessage.getAdaptMessage().countdownFormatter(duration)), PlayerType.PLAYER.getText());
        sender.sendMessage(adaptMessage.adaptMessage(message));
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return Collections.singletonList("xd xh xm xs");
    }
}
