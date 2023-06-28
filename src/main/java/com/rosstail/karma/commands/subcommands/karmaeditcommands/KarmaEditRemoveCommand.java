package com.rosstail.karma.commands.subcommands.karmaeditcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.events.karmaevents.PlayerOverTimeResetEvent;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class KarmaEditRemoveCommand extends SubCommand {

    public KarmaEditRemoveCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_EDIT_REMOVE).replaceAll("%syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes karma of a player";
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
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        String playerName = args[2];

        String command = Arrays.toString(args);
        Player player;
        player = Bukkit.getPlayerExact(playerName);

        //If player is disconnected
        if (player != null && player.isOnline()) {
            changeOnlineKarma(sender, player, args);
        } else {
            //if not force
            if (command.contains(" -f")) {
                changeOfflineKarma(sender, playerName, args);
            } else {
                sender.sendMessage("Player " + playerName + " is disconnected. Use -f to override");
            }
        }
    }

    public void changeOnlineKarma(CommandSender sender, Player player, String[] args) {
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        String command = Arrays.toString(args);

        float value;

        try {
            value = model.getKarma() - Float.parseFloat(args[3]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
            return;
        }

        if (!command.contains("-o")) {
            value = PlayerDataManager.limitKarma(value);
        }

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, model, value);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        try {
            if (!command.contains("-r")) {
                ConfigData.getConfigData().overtimeLoopMap.forEach((s, overtimeLoop) -> {
                    PlayerDataManager.setOverTimeStamp(model, s, overtimeLoop.firstTimer);
                });
                PlayerOverTimeResetEvent overTimeResetEvent = new PlayerOverTimeResetEvent(player, "all");
                Bukkit.getPluginManager().callEvent(overTimeResetEvent);
            }
        } catch (Exception ignored) { }
    }

    public void changeOfflineKarma(CommandSender sender, String playerName, String[] args) {
        String playerUUID = PlayerDataManager.getPlayerUUIDFromName(playerName);
        PlayerModel model = StorageManager.getManager().selectPlayerModel(playerUUID);
        String command = Arrays.toString(args);

        if (model == null) {
            if (playerUUID == null) {
                sender.sendMessage("The player " + playerName + " does not exist.");
                return;
            }
            if (!command.contains("-c")) {
                sender.sendMessage("Player " + playerName + " does not have data. Create by adding -c at the end of command");
                return;
            }
            model = new PlayerModel(playerUUID, playerName);
            StorageManager.getManager().insertPlayerModel(model);
        }

        float value;

        try {
            value = model.getKarma() - Float.parseFloat(args[3]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
            return;
        }

        if (!command.contains("-o")) {
            value = PlayerDataManager.limitKarma(value);
        } else {
            sender.sendMessage("new karma value is not limited.");
        }

        model.setPreviousKarma(model.getKarma());
        model.setKarma(value);
        StorageManager.getManager().updatePlayerModel(model);

        sender.sendMessage("Edited offline karma of " + playerName + " :" + value);
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
