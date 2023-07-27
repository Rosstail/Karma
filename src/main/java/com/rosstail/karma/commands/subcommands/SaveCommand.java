package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SaveCommand extends SubCommand {

    public SaveCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("%desc%", LangManager.getMessage(LangMessage.COMMANDS_SAVE_DESC))
                        .replaceAll("%syntax%", getSyntax()));
    }
    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getDescription() {
        return "Saves current players data.";
    }

    @Override
    public String getSyntax() {
        return "karma save";
    }

    @Override
    public String getPermission() {
        return "karma.command.save";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        PlayerDataManager.saveAllPlayerModelToStorage();
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_SAVE_RESULT)
                .replaceAll("%amount%", String.valueOf(PlayerDataManager.getPlayerModelMap().size()))));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
