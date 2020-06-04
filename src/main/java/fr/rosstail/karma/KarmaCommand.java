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
    private Karma karma = Karma.get();
    private PAPI papi = new PAPI();

    CheckKarmaCommand checkKarmaCommand = new CheckKarmaCommand();
    EditKarmaCommand editKarmaCommand = new EditKarmaCommand();

    File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
    YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);

    public KarmaCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("set")) {
                if (!(sender instanceof Player) || sender.hasPermission("karma.set")) {
                    editKarmaCommand.karmaSet(sender, args);
                } else {
                    permissionDenied(sender);
                }
            }
            else if (args[0].equalsIgnoreCase("add")) {
                if (!(sender instanceof Player) || sender.hasPermission("karma.add")) {
                    editKarmaCommand.karmaAdd(sender, args);
                } else {
                    permissionDenied(sender);
                }
            }
            else if (args[0].equalsIgnoreCase("remove")) {
                if (!(sender instanceof Player) || sender.hasPermission("karma.remove")) {
                editKarmaCommand.karmaRemove(sender, args);
                } else {
                    permissionDenied(sender);
                }
            }
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                if (!(sender instanceof Player) || sender.hasPermission("karma.reset")) {
                editKarmaCommand.karmaReset(sender, args);
                } else {
                    permissionDenied(sender);
                }
            }
        }
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!(sender instanceof Player) || sender.hasPermission("karma.reload")) {
                    sender.sendMessage("Karma can't be reloaded by itself for now. Please wait a future update.");
                } else {
                    permissionDenied(sender);
                }
            }
            else if (!(sender instanceof Player) || sender.hasPermission("karma.other")) {
                checkKarmaCommand.karmaOther(sender, args);
            }
            else {
                permissionDenied(sender);
            }
        }
        else if (sender instanceof Player) {
            if (sender.hasPermission("karma.own")) {
                checkKarmaCommand.karmaSelf(sender);
            } else {
                permissionDenied(sender);
            }
        }
        else {
            String message = configurationLang.getString("by-player-only");

            if (message != null) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                message = papi.setPlaceholdersOnMessage(message, (Player) sender);
                sender.sendMessage(message);
            }
        }
        return true;
    }

    private void permissionDenied(CommandSender sender) {
        String message = configurationLang.getString("permission-denied");
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            message = papi.setPlaceholdersOnMessage(message, (Player) sender);
            sender.sendMessage(message);
        }
    }
}
