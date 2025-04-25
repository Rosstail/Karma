package com.rosstail.karma.commands.subcommands.editcommands;

import com.rosstail.karma.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class EditSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getSyntax() {
        return "karma edit";
    }

    @Override
    public String getDescription() {
        return "Edit karma config or the data of a player";
    }

    @Override
    public String getPermission() {
        return "karma.command.edit";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments);
}
