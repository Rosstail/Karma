package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.wantedcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.HelpCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.wantedcommands.wantededitcommands.KarmaWantedEditAddCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.wantedcommands.wantededitcommands.KarmaWantedEditRemoveCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.wantedcommands.wantededitcommands.KarmaWantedEditResetCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.wantedcommands.wantededitcommands.KarmaWantedEditSetCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WantedEditCommand extends SubCommand {

    public WantedEditCommand() {
        subCommands.add(new KarmaWantedEditSetCommand());
        subCommands.add(new KarmaWantedEditAddCommand());
        subCommands.add(new KarmaWantedEditRemoveCommand());
        subCommands.add(new KarmaWantedEditResetCommand());
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_WANTED_EDIT).replaceAll("%syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Edit the player WANTED time";
    }

    @Override
    public String getSyntax() {
        return "karma wanted edit <editType> <player>";
    }

    @Override
    public String getPermission() {
        return "karma.command.wanted.edit";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length <= 2) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args);
        } else {
            for (int index = 0; index < getSubCommands().size(); index++) {
                if (args[2].equalsIgnoreCase(getSubCommands().get(index).getName())) {
                    getSubCommands().get(index).perform(sender, args);
                    return;
                }
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 3) {
            ArrayList<String> subCommands = new ArrayList<>();
            for (SubCommand subCommand : getSubCommands()) {
                subCommands.add(subCommand.getName());
            }
            return subCommands;
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (args[2].equalsIgnoreCase(subCommand.getName())) {
                    return subCommand.getSubCommandsArguments(sender, args);
                }
            }
        }

        return null;
    }
}