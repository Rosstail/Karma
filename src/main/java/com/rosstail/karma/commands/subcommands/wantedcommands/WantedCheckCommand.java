package com.rosstail.karma.commands.subcommands.wantedcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.HelpCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantedcheckcommands.KarmaWantedCheckOtherCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantedcheckcommands.KarmaWantedCheckSelfCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands.KarmaWantedEditAddCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands.KarmaWantedEditRemoveCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands.KarmaWantedEditResetCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands.KarmaWantedEditSetCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WantedCheckCommand extends SubCommand {

    public WantedCheckCommand() {
        subCommands.add(new KarmaWantedCheckSelfCommand());
        subCommands.add(new KarmaWantedCheckOtherCommand());
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_CHECK).replaceAll("%syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Check wanted status";
    }

    @Override
    public String getSyntax() {
        return "karma wanted check";
    }

    @Override
    public String getPermission() {
        return "karma.command.wanted.check";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length > 2) {
            subCommands.get(1).perform(sender, args);
        } else {
            subCommands.get(0).perform(sender, args);
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
