package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

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
            if (!(commandSender instanceof Player) || commandSender.hasPermission("karma.other")) {
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
            File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
            YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);
            String message = configurationLang.getString("by-player-only");

            if (message != null) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            }
        }
        return true;
    }

    private void permissionDenied(CommandSender commandSender) {
        File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
        YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);

        String message = configurationLang.getString("permission-denied");
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            commandSender.sendMessage(message);
        }
    }
}
