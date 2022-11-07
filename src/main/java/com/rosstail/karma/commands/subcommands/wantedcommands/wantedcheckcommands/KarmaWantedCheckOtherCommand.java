package com.rosstail.karma.commands.subcommands.wantedcommands.wantedcheckcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KarmaWantedCheckOtherCommand extends SubCommand {

    public KarmaWantedCheckOtherCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_WANTED_CHECK).replaceAll("%syntax%", getSyntax()), null);
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
        return "karma.command.wanted.check.other";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        Player target = Bukkit.getServer().getPlayer(args[2]);

        if (target != null && target.isOnline()) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(target, LangManager.getMessage(LangMessage.WANTED_OTHER_CHECK), PlayerType.PLAYER.getText()));
        } else {
            CommandManager.disconnectedPlayer(sender);
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
