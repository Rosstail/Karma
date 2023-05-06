package com.rosstail.karma.commands.subcommands.wantedcommands.wantededitcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.apis.ExpressionCalculator;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerWantedChangeEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KarmaWantedEditSetCommand extends SubCommand {

    public KarmaWantedEditSetCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_WANTED_EDIT_SET).replaceAll("%syntax%", getSyntax()), null);
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Set the wanted time to player";
    }

    @Override
    public String getSyntax() {
        return "karma wanted edit set <player> <value>";
    }

    @Override
    public String getPermission() {
        return "karma.command.wanted.edit";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        Player player;
        try {
            String playerName = args[3];
            player = Bukkit.getPlayerExact(playerName);
            if (player == null || !player.isOnline()) {
                CommandManager.disconnectedPlayer(sender);
                return;
            }
            String expression;
            ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
            expressionList.remove("wanted");
            expressionList.remove("edit");
            expressionList.remove("set");
            expressionList.remove(playerName);
            expression = String.join(" ", expressionList).trim();

            long duration = AdaptMessage.calculateDuration(player, expression);

            PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, new Timestamp(duration), Cause.COMMAND);
            tryWantedChange(playerWantedChangeEvent, player, LangMessage.SET_WANTED);

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            CommandManager.errorMessage(sender, e);
        }
    }

    private void tryWantedChange(PlayerWantedChangeEvent playerWantedChangeEvent, CommandSender sender, LangMessage message) {
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
        if (!playerWantedChangeEvent.isCancelled()) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(playerWantedChangeEvent.getPlayer(), LangManager.getMessage(message), PlayerType.PLAYER.getText()));
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 4) {
            return null;
        }
        if (args.length <= 5) {
            ArrayList<String> expressions = new ArrayList<>();
            expressions.add(ConfigData.getConfigData().wantedDurationExpression);
            expressions.add(ConfigData.getConfigData().wantedMaxDurationExpression);
            return expressions;
        }

        return null;
    }
}
