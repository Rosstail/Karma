package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands.editplayertiersubcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands.EditPlayerTierSubCommand;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.events.karmaevents.PlayerOverTimeResetEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class EditPlayerTierSetCommand extends EditPlayerTierSubCommand {

    public EditPlayerTierSetCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_EDIT_SET).replaceAll("%syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Set player's tier and karma";
    }

    @Override
    public String getSyntax() {
        return "karma edit player <player> tier set <tiername> (-f -o -c)";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
    }

    @Override
    public void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeOnlineTier(sender, model, args, arguments, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeOfflineKarma(sender, model, args, arguments);
    }

    public void changeOnlineTier(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        if (args.length < 6) {
            sender.sendMessage("Please insert a tier name " + TierManager.getTierManager().getTiers().keySet());
            return;
        }

        String tierName = args[5];

        if (!TierManager.getTierManager().getTiers().containsKey(tierName)) {
            sender.sendMessage("This tier does not exist");
            return;
        }
        Tier tier = TierManager.getTierManager().getTierByName(tierName);
        float value = tier.getDefaultKarma();

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, model, value);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        sender.sendMessage("Set player " + player.getName() + " tier to " + tierName);

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

    public void changeOfflineKarma(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        if (args.length < 6) {
            sender.sendMessage("Please insert a tier name " + TierManager.getTierManager().getTiers().keySet());
            return;
        }

        String tierName = args[5];

        if (!TierManager.getTierManager().getTiers().containsKey(tierName)) {
            sender.sendMessage("This tier does not exist");
            return;
        }
        Tier tier = TierManager.getTierManager().getTierByName(tierName);
        float value = tier.getDefaultKarma();

        model.setPreviousKarma(model.getKarma());
        model.setKarma(value);
        StorageManager.getManager().updatePlayerModel(model);

        sender.sendMessage("Edited offline tier of " + model.getUsername() + " :" + tierName);
        String currentTierName = model.getTierName();
        if (!Objects.equals(currentTierName, tierName)) { //Safe name check
            sender.sendMessage("His tier will change from " + currentTierName + " to " + tierName);
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        System.out.println(TierManager.getTierManager().getTiers().keySet());
        return new ArrayList<>(TierManager.getTierManager().getTiers().keySet());
    }
}
