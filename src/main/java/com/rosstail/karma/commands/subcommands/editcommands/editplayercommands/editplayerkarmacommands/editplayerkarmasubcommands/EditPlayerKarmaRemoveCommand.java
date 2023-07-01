package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands.editplayerkarmasubcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.events.karmaevents.PlayerOverTimeResetEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class EditPlayerKarmaRemoveCommand extends EditPlayerKarmaSetCommand {

    public EditPlayerKarmaRemoveCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_EDIT_SET).replaceAll("%syntax%", getSyntax()), null);
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
        return "karma edit remove <player> <value> (-f -o -c)";
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

        try {
            value = model.getKarma() - Float.parseFloat(args[5]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
            return;
        }

        if (!CommandManager.doesCommandMatchParameter(arguments, "o", "override")) {
            value = PlayerDataManager.limitKarma(value);
        }

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, model, value);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        try {
            if (!CommandManager.doesCommandMatchParameter(arguments, "r", "reset")) {
                ConfigData.getConfigData().overtimeLoopMap.forEach((s, overtimeLoop) -> {
                    PlayerDataManager.setOverTimeStamp(model, s, overtimeLoop.firstTimer);
                });
                PlayerOverTimeResetEvent overTimeResetEvent = new PlayerOverTimeResetEvent(player, "all");
                Bukkit.getPluginManager().callEvent(overTimeResetEvent);
            }
        } catch (Exception ignored) { }
    }

    public void changeOfflineKarma(CommandSender sender, PlayerModel model, String[] arguments, String[] args) {
        String command = Arrays.toString(args);

        float value;

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
        StorageManager.getManager().updatePlayerModel(model);

        sender.sendMessage("Edited offline karma of " + model.getUsername() + " :" + value);
        String currentTierName = model.getTierName();
        String futureTierName = TierManager.getTierManager().getTierByKarmaAmount(value).getName();
        if (!Objects.equals(currentTierName, futureTierName)) { //Safe name check
            sender.sendMessage("His tier will change from " + currentTierName + " to " + futureTierName);
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
