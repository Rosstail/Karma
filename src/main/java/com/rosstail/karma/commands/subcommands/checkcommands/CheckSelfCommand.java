package com.rosstail.karma.commands.subcommands.checkcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CheckSelfCommand extends SubCommand {

    public CheckSelfCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.HELP_CHECK).replaceAll("%syntax%", getSyntax()));
    }
    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Check your own Karma status";
    }

    @Override
    public String getSyntax() {
        return "karma check";
    }

    @Override
    public String getPermission() {
        return "karma.command.check.self";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.BY_PLAYER_ONLY)));
            return;
        }
        Player player = (Player) sender;

        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        sender.sendMessage("CheckSelfCommand#perform : "
                        + "\n" + model.getUuid() + " | " + model.getUsername()
                        + "\n" + model.getKarma() + " | " + model.getPreviousKarma()
                        + "\n" + model.getTierName() + " | " + model.getPreviousTierName()
                        + "\n" + model.getWantedTimeStamp().getTime() + " | " + model.isWanted()
                        + "\n" + model.getLastUpdate());
        //sender.sendMessage(AdaptMessage.getAdaptMessage().adapt((Player) sender, LangManager.getMessage(LangMessage.CHECK_OWN_KARMA), PlayerType.PLAYER.getText()));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
