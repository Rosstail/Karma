package com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.events.karmaevents.PlayerWantedChangeEvent;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
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

public class KarmaWantedEditAddCommand extends SubCommand {

    public KarmaWantedEditAddCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_WANTED_EDIT_ADD).replaceAll("%syntax%", getSyntax()), null);
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
        return "karma wanted edit add <player> <value> (-f -o -c)";
    }

    @Override
    public String getPermission() {
        return "karma.command.wanted.edit";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        Player player;
        String command = Arrays.toString(args);
        try {
            String playerName = args[3];
            player = Bukkit.getPlayerExact(playerName);

            String expression;
            ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
            expressionList.remove("wanted");
            expressionList.remove("edit");
            expressionList.remove("add");
            expressionList.remove(playerName);
            expression = String.join(" ", expressionList).trim();

            if (player != null && player.isOnline()) {
                changeWantedOnline(sender, player, expression);
            } else {
                //if not force
                if (command.contains(" -f")) {
                    changeWantedOffline(sender, playerName, expression);
                } else {
                    sender.sendMessage("Player " + playerName + " is disconnected. Use -f to override");
                }
            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
        }
    }

    private void changeWantedOnline(CommandSender sender, Player player, String expression) {
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        long wantedTimeLeft = PlayerDataManager.getWantedTimeLeft(model);
        long duration = AdaptMessage.calculateDuration(wantedTimeLeft, expression);
        long baseDuration = model.getWantedTimeStamp().getTime();
        long newDuration = baseDuration + duration;
        sender.sendMessage("KarmaWantedEditAddCommand#changeWantedOnline set wanted time to " + newDuration);
        PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, model, new Timestamp(newDuration));
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
    }

    private void changeWantedOffline(CommandSender sender, String playerName, String expression) {
        PlayerModel model = StorageManager.getManager().selectPlayerModel(PlayerDataManager.getPlayerUUIDFromName(playerName));

        if (model == null && !expression.contains("-c")) {
            System.out.println("Player " + playerName + " does not have data. Create by adding -c at the end of command");
            return;
        }

        long wantedTimeLeft = PlayerDataManager.getWantedTimeLeft(model);
        long duration = AdaptMessage.calculateDuration(wantedTimeLeft, expression);
        long baseDuration = model.getWantedTimeStamp().getTime();
        long newDuration = baseDuration + duration;
        model.setWantedTimeStamp(new Timestamp(newDuration));
        model.setWanted(PlayerDataManager.isWanted(model));
        StorageManager.getManager().updatePlayerModel(model);

        sender.sendMessage("KarmaWantedEditAddCommand#changeWantedOffline set wanted time to " + model.getWantedTimeStamp());
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 4) {
            return null;
        }
        if (args.length <= 5) {
            return Collections.singletonList("0");
        }

        return null;
    }
}
