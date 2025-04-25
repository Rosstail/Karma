package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.editplayerwantedsubcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.EditPlayerWantedSubCommand;
import com.rosstail.karma.players.PlayerDataModel;
import com.rosstail.karma.storage.StorageManager;
import com.rosstail.karma.events.karmaevents.PlayerWantedChangeEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.storage.mappers.playerdataentity.PlayerDataMapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EditPlayerWantedSetCommand extends EditPlayerWantedSubCommand {

    public EditPlayerWantedSetCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_SET_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Set player wanted duration";
    }

    @Override
    public String getSyntax() {
        return "karma edit player <player> wanted set <values> (-d -o -g -s)";
    }


    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
    }

    @Override
    public void performOnline(CommandSender sender, PlayerDataModel model, String[] args, String[] arguments, Player player) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeWantedOnline(sender, model, args, arguments, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerDataModel model, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeWantedOffline(sender, model, args, arguments);
    }

    private void changeWantedOnline(CommandSender sender, PlayerDataModel model, String[] args, String[] arguments, Player player) {
        long wantedTime = model.getWantedTimeStamp().getTime();
        boolean silent;
        List<String> expressionList = new ArrayList<>(Arrays.asList(args));
        expressionList.remove("edit");
        expressionList.remove("player");
        expressionList.remove(model.getUsername());
        expressionList.remove("wanted");
        expressionList.remove("set");
        String expression = String.join(" ", expressionList).trim();

        long duration = AdaptMessage.evalDuration(wantedTime, "[now] " + expression);

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            long limiter = AdaptMessage.evalDuration(wantedTime, ConfigData.getConfigData().wanted.wantedMaxDurationExpression);
            duration = Math.min(duration, limiter);
        } else {
            sender.sendMessage("Wanted time is not limited.");
        }
        silent = CommandManager.doesCommandMatchParameter(arguments, "s", "silent");

        PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, model, new Timestamp(duration), silent);
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);

        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_SET_RESULT), PlayerType.PLAYER.getText()));
    }

    private void changeWantedOffline(CommandSender sender, PlayerDataModel model, String[] args, String[] arguments) {
        long wantedTime = model.getWantedTimeStamp().getTime();
        List<String> expressionList = new ArrayList<>(Arrays.asList(args));
        expressionList.remove("edit");
        expressionList.remove("player");
        expressionList.remove(model.getUsername());
        expressionList.remove("wanted");
        expressionList.remove("set");
        String expression = String.join(" ", expressionList).trim();

        long duration = AdaptMessage.evalDuration(wantedTime, "[now] " + expression);

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            long limiter = AdaptMessage.evalDuration(wantedTime, ConfigData.getConfigData().wanted.wantedMaxDurationExpression);
            duration = Math.min(duration, limiter);
        } else {
            sender.sendMessage("Wanted time is not limited.");
        }

        model.setWantedTimeStamp(new Timestamp(duration));
        StorageManager.getManager().asyncUploadPlayerModel(model);

        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_SET_RESULT), PlayerType.PLAYER.getText()));
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return Collections.singletonList("xd xh xm xs");
    }
}
