package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.wantedcommands.wantededitcommands;

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
import java.util.List;

public class KarmaWantedEditResetCommand extends SubCommand {

    public KarmaWantedEditResetCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_WANTED_EDIT_RESET).replaceAll("%syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getDescription() {
        return "Reset players wanted time";
    }

    @Override
    public String getSyntax() {
        return "karma wanted edit reset <player> (-f -o -c)";
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

            if (player != null && player.isOnline()) {
                changeWantedOnline(sender, player);
            } else {
                //if not force
                if (CommandManager.doesCommandMatchParameter(command, "f", "force")) {
                    String expression;
                    ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
                    expressionList.remove("wanted");
                    expressionList.remove("edit");
                    expressionList.remove("reset");
                    expressionList.remove(playerName);
                    expression = String.join(" ", expressionList).trim();
                    changeWantedOffline(sender, playerName, expression);
                } else {
                    sender.sendMessage("Player " + playerName + " is disconnected. Use -f to override");
                }
            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
        }
    }

    private void changeWantedOnline(CommandSender sender, Player player) {
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());

        sender.sendMessage("KarmaWantedEditResetCommand#changeWantedOnline set wanted time to " + new Timestamp(0));
        PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, model, new Timestamp(0));
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
    }

    private void changeWantedOffline(CommandSender sender, String playerName, String command) {
        String playerUUID = PlayerDataManager.getPlayerUUIDFromName(playerName);
        PlayerModel model = StorageManager.getManager().selectPlayerModel(playerUUID);

        if (model == null) {
            if (playerUUID == null) {
                sender.sendMessage("The player " + playerName + " does not exist.");
                return;
            }
            if (!CommandManager.doesCommandMatchParameter(command, "c", "create")) {
                sender.sendMessage("Player " + playerName + " does not have data. Create by adding -c at the end of command");
                return;
            }
            model = new PlayerModel(playerUUID, playerName);
            StorageManager.getManager().insertPlayerModel(model);
        }

        model.setWantedTimeStamp(new Timestamp(0));
        StorageManager.getManager().updatePlayerModel(model);

        sender.sendMessage("KarmaWantedEditResetCommand#changeWantedOffline set wanted time to " + model.getWantedTimeStamp());

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
        return null;
    }
}
