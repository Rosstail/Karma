package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.WantedCheckCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.WantedEditCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands.KarmaWantedEditAddCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands.KarmaWantedEditRemoveCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands.KarmaWantedEditResetCommand;
import com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands.KarmaWantedEditSetCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WantedCommand extends SubCommand {

    public WantedCommand() {
        subCommands.add(new WantedCheckCommand());
        subCommands.add(new WantedEditCommand());
    }

    @Override
    public String getName() {
        return "wanted";
    }

    @Override
    public String getDescription() {
        return "Wanted commands";
    }

    @Override
    public String getSyntax() {
        return "karma wanted";
    }

    @Override
    public String getPermission() {
        return "karma.command.wanted";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length <= 1) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args);
        } else {
            for (int index = 0; index < getSubCommands().size(); index++) {
                if (args[1].equalsIgnoreCase(getSubCommands().get(index).getName())) {
                    getSubCommands().get(index).perform(sender, args);
                    return;
                }
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 2) {
            ArrayList<String> subCommands = new ArrayList<>();
            for (SubCommand subCommand : getSubCommands()) {
                subCommands.add(subCommand.getName());
            }
            return subCommands;
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (args[1].equalsIgnoreCase(subCommand.getName())) {
                    return subCommand.getSubCommandsArguments(sender, args);
                }
            }
        }

        return null;
    }
}
