package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands;

import com.rosstail.karma.commands.subcommands.editcommands.EditSubCommand;
import com.rosstail.karma.datas.PlayerModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class EditPlayerSubCommand extends EditSubCommand {

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public String getPermission() {
        return "karma.command.edit.player";
    }

    @Override
    public String getDescription() {
        return "Edit data of player";
    }

    @Override
    public String getSyntax() {
        return "karma edit player <player>";
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }

    public abstract void performOnline(CommandSender sender, PlayerModel model, String[] args, Player player);

    public abstract void performOffline(CommandSender sender, PlayerModel model, String[] args);
}
