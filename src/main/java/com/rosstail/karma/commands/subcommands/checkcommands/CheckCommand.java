package com.rosstail.karma.commands.subcommands.checkcommands;

import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CheckCommand extends SubCommand {

    public CheckCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_CHECK_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new CheckSelfCommand());
        subCommands.add(new CheckOtherCommand());
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Check karma status";
    }

    @Override
    public String getSyntax() {
        return "karma check (player)";
    }

    @Override
    public String getPermission() {
        return "karma.command.check";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (args.length > 1) {
            subCommands.get(1).perform(sender, args, arguments);
        } else {
            subCommands.get(0).perform(sender, args, arguments);
        }
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return null;
    }

    @Override
    public String getSubCommandHelp() {
        return subCommands.stream()
                .filter(subCommand -> subCommand.getHelp() != null)
                .map(subCommand -> "\n" + subCommand.getHelp()).toString();
    }
}
