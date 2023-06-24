package com.rosstail.karma.commands.subcommands.karmaeditcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KarmaEditSetCommand extends SubCommand {

    public KarmaEditSetCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_EDIT_SET).replaceAll("%syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Set player's karma";
    }

    @Override
    public String getSyntax() {
        return "karma edit set <player> <value> (reset)";
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
        String command = Arrays.toString(args);
        System.out.println("Command is " + command);
        PlayerModel model;
        String playerName = args[2];

        Player player;
        double value;
        boolean reset = true;
        player = Bukkit.getPlayerExact(playerName);

        //If player is disconnected
        if (player == null || !player.isOnline()) {
            //if not force
            if (!command.contains(" -f")) {
                sender.sendMessage("Player " + playerName + " is disconnected. Use -f to override");
                return;
            }

            //if force. No event.
            model = StorageManager.getManager().selectPlayerModel(PlayerDataManager.getPlayerUUIDFromName(playerName));
            try {
                value = Double.parseDouble(args[3]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                CommandManager.errorMessage(sender, e);
                return;
            }
            model.setPreviousKarma(model.getKarma());
            model.setKarma(value);
            StorageManager.getManager().updatePlayerModel(model);
            System.out.println("Edited offline karma of " + playerName + " :" + value);
            return;
        }

        try {
            value = Double.parseDouble(args[3]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
            return;
        }

        try {
            reset = Boolean.parseBoolean(args[4]);
        } catch (Exception ignored) {

        }

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, value, reset, Cause.COMMAND);
        tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.SET_KARMA);
    }

    private void tryKarmaChange(PlayerKarmaChangeEvent playerKarmaChangeEvent, CommandSender sender, LangMessage message) {
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        if (!playerKarmaChangeEvent.isCancelled()) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(playerKarmaChangeEvent.getPlayer(), LangManager.getMessage(message), PlayerType.PLAYER.getText()));
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
