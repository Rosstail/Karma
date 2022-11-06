package com.rosstail.karma.commands.subcommands.karmaeditcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.rosstail.karma.commands.CommandManager.canLaunchCommand;

public class KarmaEditResetCommand extends SubCommand {

    public KarmaEditResetCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_EDIT_RESET).replaceAll("%command-syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getDescription() {
        return "Reset player's karma";
    }

    @Override
    public String getSyntax() {
        return "karma edit reset <player> (reset)";
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
        Player player;
        boolean reset = true;
        try {
            player = Bukkit.getServer().getPlayer(args[2]);
            try {
                reset = Boolean.parseBoolean(args[3]);
            } catch (Exception ignored) {

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
            return;
        }
        if (player != null && player.isOnline()) {
            double resKarma = ConfigData.getConfigData().defaultKarma;
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, resKarma, reset, Cause.COMMAND);
            tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.RESET_KARMA);
        } else {
            CommandManager.disconnectedPlayer(sender);
        }
    }

    private void tryKarmaChange(PlayerKarmaChangeEvent playerKarmaChangeEvent, CommandSender sender, LangMessage message) {
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        if (!playerKarmaChangeEvent.isCancelled()) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(playerKarmaChangeEvent.getPlayer(), LangManager.getMessage(message), PlayerType.PLAYER.getText()));
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 3) {
            return null;
        } else if (args.length <= 4) {
            List<String> bools = new ArrayList<>();
            bools.add("true");
            bools.add("false");
            return bools;
        }
        return null;
    }
}
