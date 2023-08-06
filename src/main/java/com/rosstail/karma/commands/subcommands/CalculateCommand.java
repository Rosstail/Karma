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

public class CalculateCommand extends SubCommand {

    public CalculateCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("%desc%", LangManager.getMessage(LangMessage.COMMANDS_CALCULATE_DESC))
                        .replaceAll("%syntax%", getSyntax()));
        subCommands.add(new KarmaShopBuySelfCommand());
        subCommands.add(new KarmaShopBuyOtherCommand());
    }

    @Override
    public String getName() {
        return "calculate";
    }

    @Override
    public String getDescription() {
        return "Calculates an expression.";
    }

    @Override
    public String getSyntax() {
        return "karma calculate <expression>";
    }

    @Override
    public String getPermission() {
        return "karma.command.calculate";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length > 1) {
            ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
            expressionList.remove("calculate");
            String expression = String.join(" ", expressionList);
            Player player;
            if (sender instanceof Player) {
                player = ((Player) sender).getPlayer();
                expression = AdaptMessage.getAdaptMessage().adaptPlayerMessage(player, expression, PlayerType.PLAYER.getText());
                expression = AdaptMessage.getAdaptMessage().adaptMessage(expression);
            }
            float result = (float) ExpressionCalculator.eval(expression);

            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(
                    LangManager.getMessage(LangMessage.COMMANDS_CALCULATE_RESULT)
                            .replaceAll("%expression%", expression).replaceAll("%result%", String.valueOf(result)))
            );
        } else {
            CommandManager.errorMessage(sender, new ArrayIndexOutOfBoundsException());
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        ArrayList<String> expressions = new ArrayList<>();
        expressions.add(ConfigData.getConfigData().pvp.pvpHitAttackerChangeExpression);
        expressions.add(ConfigData.getConfigData().pvp.pvpKillAttackerChangeExpression);
        return expressions;
    }
}
