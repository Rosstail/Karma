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
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_CHECK_OTHER_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
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
        String username = args[1];
        Player target = Bukkit.getServer().getPlayer(username);
        PlayerModel model;
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

        if (target != null && target.isOnline()) {
            model = PlayerDataManager.getPlayerModelMap().get(username);
        } else {
            String uuid = PlayerDataManager.getPlayerUUIDFromName(username);
            if (uuid == null) {
                sender.sendMessage(adaptMessage.adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_DOES_NOT_EXIST).replaceAll("\\[player]", username)));
                return;
            }
            model = StorageManager.getManager().selectPlayerModel(uuid);
        }

        if (model == null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NO_DATA).replaceAll("\\[player]", username)));
            return;
        }

        String message;
        if (target != null) {
            message = adaptMessage.adaptPlayerMessage(target, LangManager.getMessage(LangMessage.COMMANDS_CHECK_OTHER_RESULT), PlayerType.PLAYER.getText());
        } else {
            message = adaptMessage.adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_CHECK_OTHER_RESULT), PlayerType.PLAYER.getText());
        }
        sender.sendMessage(adaptMessage.adaptMessage(message));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
