package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands;

import com.rosstail.karma.commands.subcommands.editcommands.EditSubCommand;
import com.rosstail.karma.datas.PlayerModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    public abstract void performOnline(CommandSender sender, PlayerModel model, String[] args, String[] arguments, Player player);

    public abstract void performOffline(CommandSender sender, PlayerModel model, String[] args, String[] arguments);
}
