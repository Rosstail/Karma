package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands;

import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class EditPlayerWantedSubCommand extends EditPlayerSubCommand {
    @Override
    public String getName() {
        return "wanted";
    }

    @Override
    public String getDescription() {
        return "Edit player wanted subcommands";
    }

    @Override
    public String getSyntax() {
        return "edit player <player> wanted <type> <value> (params)";
    }

    @Override
    public String getPermission() {
        return "karma.commands.edit.player.wanted";
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
