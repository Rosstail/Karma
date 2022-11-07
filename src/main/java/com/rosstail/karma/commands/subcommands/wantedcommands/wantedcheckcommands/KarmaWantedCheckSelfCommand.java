package com.rosstail.karma.commands.subcommands.wantedcommands.wantedcheckcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KarmaWantedCheckSelfCommand extends SubCommand {

    public KarmaWantedCheckSelfCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_WANTED_CHECK).replaceAll("%syntax%", getSyntax()), null);
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
        return "karma.command.wanted.check.self";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.BY_PLAYER_ONLY), PlayerType.PLAYER.getText()));
            return;
        }
        sender.sendMessage(AdaptMessage.getAdaptMessage().adapt((Player) sender, LangManager.getMessage(LangMessage.WANTED_OWN_CHECK), PlayerType.PLAYER.getText()));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
