package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.apis.ExpressionCalculator;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.shopcommands.KarmaShopBuyOtherCommand;
import com.rosstail.karma.commands.subcommands.shopcommands.KarmaShopBuySelfCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvalCommand extends SubCommand {

    public EvalCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EVAL_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "eval";
    }

    @Override
    public String getDescription() {
        return "Calculates an expression.";
    }

    @Override
    public String getSyntax() {
        return "karma eval <expression>";
    }

    @Override
    public String getPermission() {
        return "karma.command.eval";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length > 1) {
            ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
            expressionList.remove("eval");
            String expression = String.join(" ", expressionList);
            Player player;
            if (sender instanceof Player) {
                player = ((Player) sender).getPlayer();
                AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
                expression = adaptMessage.adaptPlayerMessage(player, expression, PlayerType.PLAYER.getText());
                expression = adaptMessage.adaptMessage(expression);
            }
            float result = (float) ExpressionCalculator.eval(expression);

            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(
                    LangManager.getMessage(LangMessage.COMMANDS_EVAL_RESULT)
                            .replaceAll("\\[expression]", expression).replaceAll("\\[result]", String.valueOf(result)))
            );
        } else {
            CommandManager.errorMessage(sender, new ArrayIndexOutOfBoundsException());
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        ArrayList<String> expressions = new ArrayList<>();
        if (ConfigData.getConfigData().pvp.pvpHitAttackerChangeExpression != null) {
            expressions.add(ConfigData.getConfigData().pvp.pvpHitAttackerChangeExpression);
        }
        if (ConfigData.getConfigData().pvp.pvpKillAttackerChangeExpression != null) {
            expressions.add(ConfigData.getConfigData().pvp.pvpKillAttackerChangeExpression);
        }
        if (ConfigData.getConfigData().pvp.pvpHitVictimChangeExpression != null) {
            expressions.add(ConfigData.getConfigData().pvp.pvpHitVictimChangeExpression);
        }
        if (ConfigData.getConfigData().pvp.pvpKillVictimChangeExpression != null) {
            expressions.add(ConfigData.getConfigData().pvp.pvpKillVictimChangeExpression);
        }
        return expressions;
    }
}
