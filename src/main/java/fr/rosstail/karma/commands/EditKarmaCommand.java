package fr.rosstail.karma.commands;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.customEvents.PlayerKarmaChangeEvent;
import fr.rosstail.karma.datas.PlayerData;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import fr.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import static fr.rosstail.karma.commands.list.Commands.*;

/**
 * Change the karma of the target, check the limit fork and new tier after.
 */
public class EditKarmaCommand {

    private final Karma plugin;
    private final AdaptMessage adaptMessage;
    private final KarmaCommand karmaCommand;
    private final ConfigData karmaValues;

    public EditKarmaCommand(KarmaCommand karmaCommand, Karma plugin) {
        this.plugin = plugin;
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
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                value = Double.parseDouble(args[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                karmaCommand.errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, value, true);
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
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                value = Double.parseDouble(args[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                karmaCommand.errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerData playerData = PlayerData.gets(player);
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, playerData.getKarma() + value, true);
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
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                value = Double.parseDouble(args[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                karmaCommand.errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerData playerData = PlayerData.gets(player);
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, playerData.getKarma() - value, true);
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
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                karmaCommand.errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                double resKarma = karmaValues.getDefaultKarma();
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, resKarma, true);
                tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.RESET_KARMA);
            } else {
                karmaCommand.disconnectedPlayer(sender);
            }
        }
    }

    private void tryKarmaChange(PlayerKarmaChangeEvent playerKarmaChangeEvent, CommandSender sender, LangMessage message) {
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        if (!playerKarmaChangeEvent.isCancelled()) {
            sender.sendMessage(adaptMessage.message(playerKarmaChangeEvent.getPlayer(), LangManager.getMessage(message), PlayerType.player.getId()));
        }
    }
}