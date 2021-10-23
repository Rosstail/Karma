package com.rosstail.karma.commands;

import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import static com.rosstail.karma.commands.list.Commands.*;

/**
 * Change the karma of the target, check the limit fork and new tier after.
 */
public class EditKarmaCommand {

    private final AdaptMessage adaptMessage;
    private final KarmaCommand karmaCommand;
    private final ConfigData karmaValues;

    public EditKarmaCommand(KarmaCommand karmaCommand) {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
        this.karmaCommand = karmaCommand;
        this.karmaValues = ConfigData.getConfigData();
    }

    /**
     * The value is now the new karma of the target player.
     * @param sender
     * @param args
     */
    public void karmaSet(CommandSender sender, String[] args) {
        if (karmaCommand.canLaunchCommand(sender, COMMAND_KARMA_SET)) {
            Player player;
            double value;
            boolean reset = true;
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                value = Double.parseDouble(args[2]);
                try {
                    reset = Boolean.parseBoolean(args[3]);
                } catch (Exception ignored) {

                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                karmaCommand.errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, value, reset, Cause.COMMAND);
                tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.SET_KARMA);
            } else {
                karmaCommand.disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Add the value to the actual Karma of the target.
     * Put a negative number remove some karma.
     * @param sender
     * @param args
     */
    public void karmaAdd(CommandSender sender, String[] args) {
        if (karmaCommand.canLaunchCommand(sender, COMMAND_KARMA_ADD)) {
            Player player;
            double value;
            boolean reset = true;
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                value = Double.parseDouble(args[2]);
                try {
                    reset = Boolean.parseBoolean(args[3]);
                } catch (Exception ignored) {

                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                karmaCommand.errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerData playerData = PlayerData.gets(player);
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, playerData.getKarma() + value, reset, Cause.COMMAND);
                tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.ADD_KARMA);
            } else {
                karmaCommand.disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Substract the karma of target player by the value
     * use a negative number make the karma increase
     * @param sender
     * @param args
     */
    public void karmaRemove(CommandSender sender, String[] args) {
        if (karmaCommand.canLaunchCommand(sender, COMMAND_KARMA_REMOVE)) {
            Player player;
            double value;
            boolean reset = true;
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                value = Double.parseDouble(args[2]);
                try {
                    reset = Boolean.parseBoolean(args[3]);
                } catch (Exception ignored) {

                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                karmaCommand.errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerData playerData = PlayerData.gets(player);
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, playerData.getKarma() - value, reset, Cause.COMMAND);
                tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.REMOVE_KARMA);
            } else {
                karmaCommand.disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Set the karma of target player as default, specified in config.yml
     * @param sender
     * @param args
     */
    public void karmaReset(CommandSender sender, String[] args) {
        if (karmaCommand.canLaunchCommand(sender, COMMAND_KARMA_RESET)) {
            Player player;
            boolean reset = true;
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                try {
                    reset = Boolean.parseBoolean(args[2]);
                } catch (Exception ignored) {

                }
            } catch (ArrayIndexOutOfBoundsException e) {
                karmaCommand.errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                double resKarma = karmaValues.defaultKarma;
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, resKarma, reset, Cause.COMMAND);
                tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.RESET_KARMA);
            } else {
                karmaCommand.disconnectedPlayer(sender);
            }
        }
    }

    private void tryKarmaChange(PlayerKarmaChangeEvent playerKarmaChangeEvent, CommandSender sender, LangMessage message) {
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        if (!playerKarmaChangeEvent.isCancelled()) {
            sender.sendMessage(adaptMessage.adapt(playerKarmaChangeEvent.getPlayer(), LangManager.getMessage(message), PlayerType.player.toString()));
        }
    }
}