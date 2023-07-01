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
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_EDIT_SET).replaceAll("%syntax%", getSyntax()), null);
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
        return "karma edit player <player> wanted set <values> (-f -o -c)";
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
        long wantedTimeLeft = PlayerDataManager.getWantedTimeLeft(model);
        List<String> expressionList = new ArrayList<>(Arrays.asList(args));
        expressionList.remove("edit");
        expressionList.remove("player");
        expressionList.remove(model.getUsername());
        expressionList.remove("wanted");
        expressionList.remove("set");
        String expression = String.join(" ", expressionList).trim();

        long duration = AdaptMessage.calculateDuration(wantedTimeLeft, "%now% " + expression);

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            long limiter = AdaptMessage.calculateDuration(wantedTimeLeft, ConfigData.getConfigData().wantedMaxDurationExpression);
            duration = Math.min(duration, limiter);
        } else {
            sender.sendMessage("Wanetd time is not limited.");
        }

        sender.sendMessage("EditPlayerWantedSetCommand#changeWantedOnline set wanted time to " + duration);
        PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, model, new Timestamp(duration));
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
    }

    private void changeWantedOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        long wantedTimeLeft = PlayerDataManager.getWantedTimeLeft(model);
        List<String> expressionList = new ArrayList<>(Arrays.asList(args));
        expressionList.remove("edit");
        expressionList.remove("player");
        expressionList.remove(model.getUsername());
        expressionList.remove("wanted");
        expressionList.remove("set");
        String expression = String.join(" ", expressionList).trim();

        long duration = AdaptMessage.calculateDuration(wantedTimeLeft, "%now% " + expression);

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            long limiter = AdaptMessage.calculateDuration(wantedTimeLeft, ConfigData.getConfigData().wantedMaxDurationExpression);
            duration = Math.min(duration, limiter);
        } else {
            sender.sendMessage("Wanetd time is not limited.");
        }

        model.setWantedTimeStamp(new Timestamp(duration));
        StorageManager.getManager().updatePlayerModel(model);

        sender.sendMessage("EditPlayerWantedSetCommand#changeWantedOffline set wanted time to " + model.getWantedTimeStamp());

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
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 3) {
            return null;
        } else if (args.length <= 4) {
            return Collections.singletonList("0");
        } else if (args.length <= 5) {
            List<String> bools = new ArrayList<>();
            bools.add("true");
            bools.add("false");
            return bools;
        }
        return null;
    }
}
