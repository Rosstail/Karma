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

public class CheckOtherCommand extends SubCommand {

    public CheckOtherCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_CHECK).replaceAll("%syntax%", getSyntax()), null);
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
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        Player target = Bukkit.getServer().getPlayer(args[1]);
        PlayerModel model = null;

        if (target != null && target.isOnline()) {
            model = StorageManager.getManager().selectPlayerModel(target.getUniqueId().toString());
            //sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(target, LangManager.getMessage(LangMessage.CHECK_OTHER_KARMA), PlayerType.PLAYER.getText()));
        } else {
            String uuid = PlayerDataManager.getPlayerUUIDFromName(args[1]);
            if (uuid != null) {
                model = StorageManager.getManager().selectPlayerModel(uuid); //READ
            }
        }

        if (model == null) {
            sender.sendMessage("CheckOtherCommand#perform :" +
                    "\nThis player " + args[1] + " does not exist");
            return;
        }

        sender.sendMessage("CheckOtherCommand#perform : "
                + "\n" + model.getUuid() + " | " + model.getUsername()
                + "\n" + model.getKarma() + " | " + model.getPreviousKarma()
                + "\n" + model.getTierName() + " | " + model.getPreviousTierName()
                + "\n" + model.getWantedTimeStamp().getTime() + " | " + model.isWanted()
                + "\n" + model.getLastUpdate()
        );
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
