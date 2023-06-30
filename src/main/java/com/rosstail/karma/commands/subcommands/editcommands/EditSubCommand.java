package com.rosstail.karma.commands.subcommands.editcommands;

import com.rosstail.karma.commands.SubCommand;

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
}
