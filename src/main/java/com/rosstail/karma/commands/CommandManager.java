package com.rosstail.karma.commands;

import com.rosstail.karma.commands.subcommands.*;
import com.rosstail.karma.commands.subcommands.checkcommands.CheckCommand;
import com.rosstail.karma.commands.subcommands.editcommands.EditCommand;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.wantedcommands.WantedCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checking what method/class will be used on command, depending of command Sender and number of args.
 */
public class CommandManager implements CommandExecutor, TabExecutor {

    private final ArrayList<SubCommand> subCommands = new ArrayList<SubCommand>();
    private static final Pattern shortParamPattern = Pattern.compile("-[A-Za-z]*");
    private static final Pattern longParamPattern = Pattern.compile("--[A-Za-z]*");

    public CommandManager() {
        subCommands.add(new CalculateCommand());
        subCommands.add(new CheckCommand());
        subCommands.add(new EditCommand());
        subCommands.add(new ReloadCommand());
        subCommands.add(new SaveCommand());
        subCommands.add(new ShopCommand());
        subCommands.add(new WantedCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args);
        } else {
            for (int index = 0; index < getSubCommands().size(); index++) {
                if (args[0].equalsIgnoreCase(getSubCommands().get(index).getName())) {
                    getSubCommands().get(index).perform(sender, args);
                    return true;
                }
            }
        }

        return true;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length <= 1) {
            ArrayList<String> subCommandArguments = new ArrayList<>();

            for (int i = 0; i < getSubCommands().size(); i++) {
                subCommandArguments.add(getSubCommands().get(i).getName());
            }

            return subCommandArguments;
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (subCommand.getName().equalsIgnoreCase(args[0])) {
                    return subCommand.getSubCommandsArguments((Player) sender, args);
                }
            }
        }

        return null;
    }

    public static boolean canLaunchCommand(CommandSender sender, SubCommand command) {
        if (!(sender instanceof Player) || sender.hasPermission(command.getPermission())) {
            return true;
        }
        permissionDenied(sender, command);
        return false;
    }

    private static void permissionDenied(CommandSender sender, SubCommand command) {
        String message = LangManager.getMessage(LangMessage.PERMISSION_DENIED);
        if (message != null) {
            message = AdaptMessage.getAdaptMessage().adapt((Player) sender, message, PlayerType.PLAYER.getText());
            message = message.replaceAll("%command%", command.getName());
            message = message.replaceAll("%permission%", command.getPermission());
            sender.sendMessage(message);
        }
    }

    /**
     * @param sender
     */
    /*public static void disconnectedPlayer(CommandSender sender) {
        sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.DISCONNECTED), null));
    }*/
    public static void errorMessage(CommandSender sender, Exception e) {
        if (e instanceof ArrayIndexOutOfBoundsException) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS), null));
        }
        if (e instanceof NumberFormatException) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.WRONG_VALUE), null));
            e.printStackTrace();
        }
    }

    public static void commandsLauncher(Player player, List<String> commands) {
        if (commands != null) {
            commands.forEach(s -> {
                placeCommands(player, s);
            });
        }
    }

    public static void commandsLauncher(Player attacker, Player victim, List<String> commands) {
        if (commands != null) {
            commands.forEach(s -> {
                placeCommands(attacker, victim, s);
            });
        }
    }

    private static void placeCommands(Player player, String command) {
        command = AdaptMessage.getAdaptMessage().adapt(player, command, PlayerType.PLAYER.getText());
        CommandSender senderOrTarget = Bukkit.getConsoleSender();

        String regex = PlayerType.PLAYER.getText();
        if (command.startsWith(regex)) {
            command = command.replaceFirst(regex, "").trim();
            senderOrTarget = player;
        }
        if (command.startsWith("%msg")) {
            if (senderOrTarget instanceof Player) {
                AdaptMessage.getAdaptMessage().sendToPlayer(player, command);
            } else {
                senderOrTarget.sendMessage(command);
            }
        } else {
            Bukkit.dispatchCommand(senderOrTarget, command);
        }
    }

    private static void placeCommands(Player attacker, Player victim, String command) {
        command = AdaptMessage.getAdaptMessage().adapt(attacker, command, PlayerType.ATTACKER.getText());
        command = AdaptMessage.getAdaptMessage().adapt(victim, command, PlayerType.VICTIM.getText());

        CommandSender senderOrTarget = Bukkit.getConsoleSender();
        if (command.startsWith(PlayerType.VICTIM.getText())) {
            command = command.replaceFirst(PlayerType.VICTIM.getText(), "").trim();
            senderOrTarget = victim;
        } else if (command.startsWith(PlayerType.ATTACKER.getText())) {
            command = command.replaceFirst(PlayerType.ATTACKER.getText(), "").trim();
            senderOrTarget = attacker;
        }

        if (command.startsWith("%msg")) {
            if (senderOrTarget instanceof Player) {
                AdaptMessage.getAdaptMessage().sendToPlayer((Player) senderOrTarget, command);
            } else {
                senderOrTarget.sendMessage(command);
            }
        } else {
            Bukkit.dispatchCommand(senderOrTarget, command);
        }
    }

    public static boolean doesCommandMatchParameter(String command, String shortParam, String longParam) {
        Matcher shortMatcher = shortParamPattern.matcher(command);
        while (shortMatcher.find()) {
            if (shortMatcher.group().contains(shortParam)) {
                return true;
            }
        }

        Matcher longMatcher = longParamPattern.matcher(command);
        while (longMatcher.find()) {
            if (longMatcher.group().contains(longParam)) {
                return true;
            }
        }

        return false;
    }
}
