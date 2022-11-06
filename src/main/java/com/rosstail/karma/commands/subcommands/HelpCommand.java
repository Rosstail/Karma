package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HelpCommand extends SubCommand {

    public HelpCommand(final CommandManager manager) {
        subCommands = manager.getSubCommands();
    }

    public HelpCommand(final SubCommand subCommand) {
        subCommands = subCommand.getSubCommands();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Displays a list of commands";
    }

    @Override
    public String getSyntax() {
        return "karma help";
    }

    @Override
    public String getPermission() {
        return "karma.command.help";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        StringBuilder helpCommand = new StringBuilder("&a=====&6" + getName().toUpperCase() + "&a=====&r");
        for (SubCommand subCommand : subCommands) {
            helpCommand.append("\n").append("&b > &2/").append(subCommand.getSyntax()).append("&8: &r").append(subCommand.getDescription());
        }
        sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, helpCommand.toString(), null));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
