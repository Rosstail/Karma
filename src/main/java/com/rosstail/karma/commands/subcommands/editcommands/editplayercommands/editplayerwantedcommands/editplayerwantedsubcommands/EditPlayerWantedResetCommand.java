package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.editplayerwantedsubcommands;

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

public class EditPlayerWantedResetCommand extends EditPlayerWantedSubCommand {

    public EditPlayerWantedResetCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_EDIT_SET).replaceAll("%syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getDescription() {
        return "Makes the player innocent and never guilty";
    }

    @Override
    public String getSyntax() {
        return "karma edit player <player> wanted reset (-f -o -c)";
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
    public void performOffline(CommandSender sender, PlayerModel model, String[] arguments, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeWantedOffline(sender, model, args, arguments);
    }

    private void changeWantedOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player) {
        sender.sendMessage("EditPlayerWantedResetCommand#changeWantedOnline set wanted time to " + new Timestamp(0));
        PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, model, new Timestamp(0));
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
    }

    private void changeWantedOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments) {
        model.setWantedTimeStamp(new Timestamp(0));
        StorageManager.getManager().updatePlayerModel(model);

        sender.sendMessage("EditPlayerWantedResetCommand#changeWantedOffline set wanted time to " + model.getWantedTimeStamp());

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
