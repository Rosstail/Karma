package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.karmaeditcommands.KarmaEditAddCommand;
import com.rosstail.karma.commands.subcommands.karmaeditcommands.KarmaEditRemoveCommand;
import com.rosstail.karma.commands.subcommands.karmaeditcommands.KarmaEditResetCommand;
import com.rosstail.karma.commands.subcommands.karmaeditcommands.KarmaEditSetCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KarmaEditCommand extends SubCommand {

    public KarmaEditCommand() {
        subCommands.add(new KarmaEditSetCommand());
        subCommands.add(new KarmaEditAddCommand());
        subCommands.add(new KarmaEditRemoveCommand());
        subCommands.add(new KarmaEditResetCommand());
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Edit karma of a player";
    }

    @Override
    public String getSyntax() {
        return "karma edit <editType> <player> <value>";
    }

    @Override
    public String getPermission() {
        return "karma.command.edit";
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
