package com.rosstail.karma.commands;

import com.rosstail.karma.commands.subcommands.*;
import com.rosstail.karma.commands.subcommands.checkcommands.CheckCommand;
import com.rosstail.karma.commands.subcommands.editcommands.EditCommand;
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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checking what method/class will be used on command, depending of command Sender and number of args.
 */
public class CommandManager implements CommandExecutor, TabExecutor {

    private final ArrayList<SubCommand> subCommands = new ArrayList<SubCommand>();
    private static final Pattern shortParamPattern = Pattern.compile("^-[A-Za-z]+"); //
    private static final Pattern longParamPattern = Pattern.compile("^--[A-Za-z]+");

    public CommandManager() {
        subCommands.add(new EvalCommand());
        subCommands.add(new CheckCommand());
        subCommands.add(new EditCommand());
        subCommands.add(new ReloadCommand());
        subCommands.add(new SaveCommand());
        subCommands.add(new ShopCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String[] arguments = getCommandArguments(args);
        args = removeFoundArgumentsFromCommand(args, arguments);

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args, null);
            return true;
        }

        for (int index = 0; index < getSubCommands().size(); index++) {
            if (args[0].equalsIgnoreCase(getSubCommands().get(index).getName())) {
                getSubCommands().get(index).perform(sender, args, arguments);
                return true;
            }
        }

        return true;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        String[] arguments = getCommandArguments(args);
        String[] commandArgs = removeFoundArgumentsFromCommand(args, arguments);

        if (args.length <= 1) {
            ArrayList<String> subCommandArguments = new ArrayList<>();

            for (int i = 0; i < getSubCommands().size(); i++) {
                subCommandArguments.add(getSubCommands().get(i).getName());
            }

            return subCommandArguments;
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (subCommand.getName().equalsIgnoreCase(args[0])) {
                    return subCommand.getSubCommandsArguments((Player) sender, commandArgs, arguments);
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
        String message = LangManager.getMessage(LangMessage.COMMANDS_PERMISSION_DENIED);
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        message = adaptMessage.adaptPlayerMessage((Player) sender, message, PlayerType.PLAYER.getText());
        message = adaptMessage.adaptMessage(message);
        message = message.replaceAll("\\[command]", command.getName());
        message = message.replaceAll("\\[permission]", command.getPermission());
        sender.sendMessage(message);
    }

    /**
     * @param sender
     */
    public static void disconnectedPlayer(CommandSender sender, String playerName) {
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_DISCONNECTED).replaceAll("\\[player]", playerName)));
    }
    public static void errorMessage(CommandSender sender, Exception e) {
        if (e instanceof ArrayIndexOutOfBoundsException) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE)));
        }
        if (e instanceof NumberFormatException) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE)));
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
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        command = adaptMessage.adaptMessage(adaptMessage.adaptPlayerMessage(player, command, PlayerType.PLAYER.getText()));

        CommandSender senderOrTarget = Bukkit.getConsoleSender();

        String regex = PlayerType.PLAYER.getText();
        if (command.startsWith(regex)) {
            command = command.replaceFirst(regex, "").trim();
            senderOrTarget = player;
        }
        if (command.startsWith("[msg")) {
            if (senderOrTarget instanceof Player) {
                AdaptMessage.getAdaptMessage().sendToPlayer(player, command);
            } else {
                senderOrTarget.sendMessage(command.replaceAll("\\[msg(.+)?]", "").trim());
            }
        } else {
            Bukkit.dispatchCommand(senderOrTarget, command);
        }
    }

    private static void placeCommands(Player attacker, Player victim, String command) {
        command = AdaptMessage.getAdaptMessage().adaptPvpMessage(attacker, victim, command);

        CommandSender senderOrTarget = Bukkit.getConsoleSender();
        if (command.startsWith(PlayerType.VICTIM.getText())) {
            command = command.replaceFirst(PlayerType.VICTIM.getText(), "").trim();
            senderOrTarget = victim;
        } else if (command.startsWith(PlayerType.ATTACKER.getText())) {
            command = command.replaceFirst(PlayerType.ATTACKER.getText(), "").trim();
            senderOrTarget = attacker;
        }

        if (command.startsWith("[msg")) {
            if (senderOrTarget instanceof Player) {
                AdaptMessage.getAdaptMessage().sendToPlayer((Player) senderOrTarget, command);
            } else {
                senderOrTarget.sendMessage(command);
            }
        } else {
            Bukkit.dispatchCommand(senderOrTarget, command);
        }
    }


    private static String[] getCommandArguments(String[] commandArg) {
        List<String> foundArguments = new ArrayList<>();
        for (String s : commandArg) {
            Matcher shortMatcher = shortParamPattern.matcher(s);
            Matcher longMatcher = longParamPattern.matcher(s);

            if (shortMatcher.find() || longMatcher.find()) {
                foundArguments.add(s);
            }
        }

        return foundArguments.toArray(new String[0]);
    }

    private static String[] removeFoundArgumentsFromCommand(String[] command, String[] foundArguments) {
        List<String> commandList = Arrays.asList(command);
        List<String> foundArgumentList = Arrays.asList(foundArguments);

        List<String> difference = new ArrayList<>(commandList);
        difference.removeAll(foundArgumentList);

        return difference.toArray(new String[0]);
    }

    public static boolean doesCommandMatchParameter(String[] arguments, String shortParam, String longParam) {
        StringBuilder argumentString = new StringBuilder();
        for (String argument : arguments) {
            argumentString.append(argument).append(" ");
        }
        Matcher shortMatcher = shortParamPattern.matcher(argumentString);
        while (shortMatcher.find()) {
            if (shortMatcher.group().contains(shortParam)) {
                return true;
            }
        }

        Matcher longMatcher = longParamPattern.matcher(argumentString);
        while (longMatcher.find()) {
            if (longMatcher.group().contains(longParam)) {
                return true;
            }
        }

        return false;
    }
}
