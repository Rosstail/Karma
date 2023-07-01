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
    public void perform(CommandSender sender, String[] args, String[] arguments) {
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
            if (uuid == null) {
                sender.sendMessage("The player " + args[1] + "does not exist.");
                return;
            }
            model = StorageManager.getManager().selectPlayerModel(uuid); //READ
        }

        if (model == null) {
            sender.sendMessage("CheckOtherCommand#perform :" +
                    "\nThis player " + args[1] + " does not have karma datas.");
            return;
        }

        sender.sendMessage("CheckOtherCommand#perform : "
                + "\nUUID AND NAME: " + model.getUuid() + " | " + model.getUsername()
                + "\nSTATUS: " + (target != null && target.isOnline() ? " Connected" : "Disconnected")
                + "\nCURRENT KARMA and PREVIOUS: " + model.getKarma() + " | " + model.getPreviousKarma()
                + "\nCURRENT TIER and PREVIOUS: " + model.getTierName() + " | " + model.getPreviousTierName()
                + (!Objects.equals(TierManager.getTierManager().getTierByKarmaAmount(model.getKarma()).getName(), model.getTierName())
                ? " (Will become " + TierManager.getTierManager().getTierByKarmaAmount(model.getKarma()).getName() + " on next connection)" : "")
                + "\nWANTED TIME and IS WANTED: " + model.getWantedTimeStamp().getTime() + " | " + model.isWanted()
                + "\nLAST UPDATE: " + model.getLastUpdate()
        );
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
