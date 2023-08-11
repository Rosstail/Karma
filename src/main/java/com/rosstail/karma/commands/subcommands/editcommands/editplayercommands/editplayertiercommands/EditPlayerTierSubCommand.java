package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayertiercommands;

import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.EditPlayerSubCommand;
import com.rosstail.karma.datas.PlayerModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class EditPlayerTierSubCommand extends EditPlayerSubCommand {
    @Override
    public String getName() {
        return "tier";
    }

    @Override
    public String getDescription() {
        return "Edit player tier subcommands";
    }

    @Override
    public String getSyntax() {
        return "edit player <player> tier";
    }

    @Override
    public String getPermission() {
        return "karma.commands.edit.player.tier";
    }

    public abstract void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player);

    public abstract void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments);

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
