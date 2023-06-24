package com.rosstail.karma.commands.subcommands.karmaeditcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
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

public class KarmaEditRemoveCommand extends SubCommand {

    public KarmaEditRemoveCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_EDIT_REMOVE).replaceAll("%syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes karma of a player";
    }

    @Override
    public String getSyntax() {
        return "karma edit remove <player> <value> (reset)";
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
        double value;
        boolean reset = true;
        try {
            player = Bukkit.getServer().getPlayer(args[2]);
            value = Double.parseDouble(args[3]);
            try {
                reset = Boolean.parseBoolean(args[4]);
            } catch (Exception ignored) {

            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
            return;
        }
        if (player != null && player.isOnline()) {
            PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, playerData.getKarma() - value, reset, Cause.COMMAND);
            tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.REMOVE_KARMA);
        } else {
            sender.sendMessage(PlayerDataManager.getPlayerUUIDFromName(args[2]));
            //CommandManager.disconnectedPlayer(sender);
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
            return Collections.singletonList("0");
        } else if (args.length <= 5) {
            List<String> bools = new ArrayList<>();
            bools.add("true");
            bools.add("false");
            return bools;
        }
        return null;
    }
}
