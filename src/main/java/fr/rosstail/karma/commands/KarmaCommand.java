package fr.rosstail.karma.commands;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.apis.PAPI;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
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
    private final AdaptMessage adaptMessage;
    private final CheckKarmaCommand checkKarmaCommand;
    private final EditKarmaCommand editKarmaCommand;
    private final PAPI papi = new PAPI();

    public KarmaCommand(Karma plugin) {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
        this.checkKarmaCommand = new CheckKarmaCommand();
        this.editKarmaCommand = new EditKarmaCommand(plugin);
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length >= 3) {
            try {
                Double.parseDouble(args[2]);
                if (args[0].equalsIgnoreCase("set")) {
                    if (!(sender instanceof Player) || sender.hasPermission("karma.set")) {
                        editKarmaCommand.karmaSet(sender, args);
                    } else {
                        permissionDenied(sender);
                    }
                } else if (args[0].equalsIgnoreCase("add")) {
                    if (!(sender instanceof Player) || sender.hasPermission("karma.add")) {
                        editKarmaCommand.karmaAdd(sender, args);
                    } else {
                        permissionDenied(sender);
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (!(sender instanceof Player) || sender.hasPermission("karma.remove")) {
                        editKarmaCommand.karmaRemove(sender, args);
                    } else {
                        permissionDenied(sender);
                    }
                }
            } catch (NumberFormatException e) {
                String message = LangManager.getMessage(LangMessage.WRONG_VALUE);
                adaptMessage.message(sender, null, 0, message);
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                if (!(sender instanceof Player) || sender.hasPermission("karma.reset")) {
                    editKarmaCommand.karmaReset(sender, args);
                } else {
                    permissionDenied(sender);
                }
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!(sender instanceof Player) || sender.hasPermission("karma.reload")) {
                    sender.sendMessage(
                        "Karma can't be reloaded by itself for now. Please wait a future update.");
                } else {
                    permissionDenied(sender);
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                if (sender.hasPermission("karma.help")) {
                    adaptMessage.message(sender, null, 0, LangManager.getMessage(LangMessage.HELP));
                } else {
                    permissionDenied(sender);
                }
            } else if (!(sender instanceof Player) || sender.hasPermission("karma.other")) {
                checkKarmaCommand.karmaOther(sender, args);
            } else {
                permissionDenied(sender);
            }
        } else if (sender instanceof Player) {
            if (sender.hasPermission("karma.own")) {
                checkKarmaCommand.karmaSelf(sender);
            } else {
                permissionDenied(sender);
            }
        } else {
            sender.sendMessage(LangManager.getMessage(LangMessage.BY_PLAYER_ONLY));
        }
        return true;
    }

    private void permissionDenied(CommandSender sender) {
        String message = LangManager.getMessage(LangMessage.PERMISSION_DENIED);
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            message = papi.setPlaceholdersOnMessage(message, (Player) sender);
            sender.sendMessage(message);
        }
    }
}
