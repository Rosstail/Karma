package com.rosstail.karma.commands.subcommands.checkcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class CheckOtherCommand extends SubCommand {

    public CheckOtherCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("%desc%", LangManager.getMessage(LangMessage.COMMANDS_CHECK_OTHER_DESC))
                        .replaceAll("%syntax%", getSyntax()));
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Check Karma status of another player";
    }

    @Override
    public String getSyntax() {
        return "karma check <player>";
    }

    @Override
    public String getPermission() {
        return "karma.command.check.other";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        Player target = Bukkit.getServer().getPlayer(args[1]);
        PlayerModel model;

        if (target != null && target.isOnline()) {
            model = PlayerDataManager.getPlayerModelMap().get(args[1]);
        } else {
            String uuid = PlayerDataManager.getPlayerUUIDFromName(args[1]);
            if (uuid == null) {
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_DOES_NOT_EXIST).replaceAll("%player%", args[1])));
                return;
            }
            model = StorageManager.getManager().selectPlayerModel(uuid);
        }

        if (model == null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NO_DATA)));
            return;
        }

        if (target != null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptPlayerMessage(target, LangManager.getMessage(LangMessage.COMMANDS_CHECK_OTHER_RESULT), PlayerType.PLAYER.getText()));
        } else {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_CHECK_OTHER_RESULT), PlayerType.PLAYER.getText()));
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
