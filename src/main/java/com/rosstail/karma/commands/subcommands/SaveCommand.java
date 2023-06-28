package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class SaveCommand extends SubCommand {

    public SaveCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_SAVE).replaceAll("%syntax%", getSyntax()), null);
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
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        PlayerDataManager.saveAllPlayerModelToStorage();
        sender.sendMessage("SaveCommand#perform: saved" + PlayerDataManager.getPlayerModelMap().size() + " player models to storage");
        /*sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.SAVED_DATA)
                .replaceAll("%number%", String.valueOf(playerModelMap.size())), null));

         */
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
