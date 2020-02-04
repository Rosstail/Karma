package fr.rosstail.karma;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KarmaCommand implements CommandExecutor {

    CheckKarmaCommand checkKarmaCommand = new CheckKarmaCommand();
    EditKarmaCommand editKarmaCommand = new EditKarmaCommand();

    public KarmaCommand() {
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length >= 3) {
            if (args[0].equals("set"))
                editKarmaCommand.karmaSet(commandSender, args);
            else if (args[0].equals("add"))
                editKarmaCommand.karmaAdd(commandSender, args);
            else if (args[0].equals("remove"))
                editKarmaCommand.karmaRemove(commandSender, args);
        }
        else if (args.length == 2) {
            if (args[0].equals("reset"))
                editKarmaCommand.karmaReset(commandSender, args);
        }
        else if (args.length == 1)
            checkKarmaCommand.karmaOther(commandSender, args);
        else if (commandSender instanceof Player)
            checkKarmaCommand.karmaSelf(commandSender);
        else {
            commandSender.sendMessage("[Karma] \"/karma\" must be send by a player.");
            commandSender.sendMessage("[Karma] As non-player, you can select a player with \"/karma <player>\".");
        }
        return true;
    }
}
