package com.rosstail.karma.commands;

import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class SubCommand {
    public List<SubCommand> subCommands = new ArrayList<>();
    public String help = "";
    public abstract String getName();

    public abstract String getDescription();
    public abstract String getSyntax();
    public abstract String getPermission();
    public abstract void perform(CommandSender sender, String[] args, String[] arguments);

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }
    public String getHelp() {
        return help;
    }

    public String getSubCommandHelp() {
        return AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_HELP_HEADER));
    }

    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
