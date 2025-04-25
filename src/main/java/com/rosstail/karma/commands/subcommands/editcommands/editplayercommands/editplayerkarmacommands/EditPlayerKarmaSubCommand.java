package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands;

import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class EditPlayerKarmaSubCommand extends EditPlayerSubCommand {
    @Override
    public String getName() {
        return "player";
    }

    @Override
    public String getDescription() {
        return "Edit player karma subcommands";
    }

    @Override
    public String getSyntax() {
        return "edit player <player> karma <value> (params)";
    }

    @Override
    public String getPermission() {
        return "karma.commands.edit.player.karma";
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return null;
    }
}
