package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerkarmacommands;

import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import com.rosstail.karma.datas.PlayerModel;
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

    public abstract void performOnline(CommandSender sender, PlayerModel model, String[] args, Player player);

    public abstract void performOffline(CommandSender sender, PlayerModel model, String[] args);

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
