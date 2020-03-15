package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Checking what method/class will be used on command, depending of command Sender and number of args.
 */
public class KarmaCommand implements CommandExecutor {
    private Karma karma = Karma.getInstance();
    CheckKarmaCommand checkKarmaCommand = new CheckKarmaCommand();
    EditKarmaCommand editKarmaCommand = new EditKarmaCommand();

    public KarmaCommand() {
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length >= 3) {
            if (args[0].equals("set")) {
                if (!(commandSender instanceof Player) || commandSender.hasPermission("karma.set")) {
                    editKarmaCommand.karmaSet(commandSender, args);
                } else {
                    permissionDenied(commandSender);
                }
            }
            else if (args[0].equals("add")) {
                if (!(commandSender instanceof Player) || commandSender.hasPermission("karma.add")) {
                    editKarmaCommand.karmaAdd(commandSender, args);
                } else {
                    permissionDenied(commandSender);
                }
            }
            else if (args[0].equals("remove")) {
                if (!(commandSender instanceof Player) || commandSender.hasPermission("karma.remove")) {
                editKarmaCommand.karmaRemove(commandSender, args);
                } else {
                    permissionDenied(commandSender);
                }
            }
        }
        else if (args.length == 2) {
            if (args[0].equals("reset")) {
                if (!(commandSender instanceof Player) || commandSender.hasPermission("karma.reset")) {
                editKarmaCommand.karmaReset(commandSender, args);
                } else {
                    permissionDenied(commandSender);
                }
            }
        }
        else if (args.length == 1) {
            if (!(commandSender instanceof Player) || commandSender.hasPermission("karma.reset")) {
                checkKarmaCommand.karmaOther(commandSender, args);
            } else {
                permissionDenied(commandSender);
            }
        }
        else if (commandSender instanceof Player) {
            if (commandSender.hasPermission("karma.own")) {
                checkKarmaCommand.karmaSelf(commandSender);
            } else {
                permissionDenied(commandSender);
            }
        }
        else {
            String message = karma.getConfig().getString("general.by-player-only");
            if (message != null) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            }
        }
        return true;
    }

    private void permissionDenied(CommandSender commandSender) {
        String message = this.karma.getConfig().getString("messages.permission-denied");
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            commandSender.sendMessage(message);
        }
    }
}
