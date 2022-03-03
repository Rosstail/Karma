package com.rosstail.karma.commands;

import com.rosstail.karma.Karma;
import com.rosstail.karma.apis.ExpressionCalculator;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.customevents.PlayerWantedChangeEvent;
import com.rosstail.karma.datas.DBInteractions;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
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
import org.bukkit.util.StringUtil;


import java.sql.Timestamp;
import java.util.*;

/**
 * Checking what method/class will be used on command, depending of command Sender and number of args.
 */
public class KarmaCommand implements CommandExecutor, TabExecutor {
    private final AdaptMessage adaptMessage;
    private final ConfigData configData;

    private enum commands {
        KARMA("", "karma"),
        KARMA_CALCULATE("calculate", "karma.calculate"),
        KARMA_CHECK("check", "karma.self"),
        KARMA_HELP("help", "karma.help"),
        KARMA_SET("set", "karma.set"),
        KARMA_ADD("add", "karma.add"),
        KARMA_REMOVE("remove", "karma.remove"),
        KARMA_RESET("reset", "karma.reset"),
        WANTED("wanted", "karma.wanted"),
        WANTED_CHECK("wanted check", "karma.wanted.self"),
        WANTED_OTHER("wanted check", "karma.wanted.other"),
        WANTED_SET("wanted set", "karma.wanted.set"),
        WANTED_ADD("wanted add", "karma.wanted.add"),
        WANTED_REMOVE("wanted remove", "karma.wanted.remove"),
        WANTED_RESET("wanted reset", "karma.wanted.reset"),
        KARMA_OTHER("check", "karma.other"),
        KARMA_SAVE("save", "karma.save"),
        KARMA_RELOAD("reload", "karma.reload");

        final String command;
        final String perm;
        commands(String command, String perm) {
            this.command = command;
            this.perm = perm;
        }
    }

    public KarmaCommand() {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
        this.configData = ConfigData.getConfigData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String string = String.join(" ", args);
        if (!canLaunchCommand(sender, commands.KARMA)) {
            return false;
        }

        if (string.startsWith(commands.KARMA_CHECK.command)) {
            if (args.length == 2) {
                karmaOther(sender, args[1]);
            } else {
                karmaSelf(sender);
            }
        } else if (string.startsWith(commands.KARMA_SET.command)) {
            karmaSet(sender, args);
        } else if (string.startsWith(commands.KARMA_ADD.command)) {
            karmaAdd(sender, args);
        } else if (string.startsWith(commands.KARMA_REMOVE.command)) {
            karmaRemove(sender, args);
        } else if (string.startsWith(commands.KARMA_RESET.command)) {
            karmaReset(sender, args);
        } else if (string.startsWith(commands.WANTED_CHECK.command)) {
            if (args.length == 3) {
                wantedOther(sender, args[2]);
            } else {
                wantedSelf(sender);
            }
        } else if (string.startsWith(commands.WANTED_SET.command)) {
            wantedSet(sender, args);
        } else if (string.startsWith(commands.WANTED_ADD.command)) {
            wantedAdd(sender, args);
        } else if (string.startsWith(commands.WANTED_REMOVE.command)) {
            wantedRemove(sender, args);
        } else if (string.startsWith(commands.WANTED_RESET.command)) {
            wantedReset(sender, args);
        } else if (string.startsWith(commands.KARMA_SAVE.command)) {
            karmaSave(sender);
        } else if (string.startsWith(commands.KARMA_CALCULATE.command)) {
          if (canLaunchCommand(sender, commands.KARMA_CALCULATE)) {
              if (args.length > 1) {
                  ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
                  expressionList.remove("calculate");
                  String expression = String.join(" ", expressionList);
                  Player player = null;
                  if (sender instanceof Player) {
                      player = ((Player) sender).getPlayer();
                      expression = adaptMessage.adapt(player, expression, null);
                  }
                  double result = ExpressionCalculator.eval(expression);

                  sender.sendMessage(adaptMessage.adapt(player,
                          LangManager.getMessage(LangMessage.CALCULATION)
                                  .replaceAll("%expression%", expression).replaceAll("%result%", String.valueOf(result))
                          , null));
              } else {
                  errorMessage(sender, new ArrayIndexOutOfBoundsException());
              }
          }
        } else if (string.startsWith(commands.KARMA_HELP.command)) {
            if (canLaunchCommand(sender, commands.KARMA_HELP)) {
                sender.sendMessage(adaptMessage.listMessage(null, LangManager.getListMessage(LangMessage.HELP)));
            }
        } else {
            Player playerSender = null;
            if (sender instanceof Player) {
                playerSender = ((Player) sender).getPlayer();
            }
            sender.sendMessage(adaptMessage.listMessage(playerSender, LangManager.getListMessage(LangMessage.HELP)));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> list = new ArrayList<>();
        String string = String.join(" ", args);
        ConfigData configData = ConfigData.getConfigData();

        if (args.length <= 1) {
            list.add("add");
            list.add("calculate");
            list.add("check");
            list.add("help");
            list.add("remove");
            list.add("reset");
            list.add("save");
            list.add("set");
            list.add("wanted");
            StringUtil.copyPartialMatches(args[0], list, completions);
        } else if (args.length <= 2) {
            if (string.startsWith(commands.KARMA_CALCULATE.command) && sender.hasPermission(commands.KARMA_CALCULATE.perm)) {
                if (configData.pvpHitRewardExpression != null) {
                    list.add(configData.pvpHitRewardExpression);
                }
                if (configData.pvpKillRewardExpression != null) {
                    list.add(configData.pvpKillRewardExpression);
                }
            } else if (string.startsWith(commands.KARMA_SET.command) || string.startsWith(commands.KARMA_ADD.command)
                    || string.startsWith(commands.KARMA_REMOVE.command) || string.startsWith(commands.KARMA_RESET.command)
                    || string.startsWith(commands.KARMA_CHECK.command)) {
                Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
            } else if (string.startsWith(commands.WANTED.command)) {
                list.add("check");
                list.add("add");
                list.add("remove");
                list.add("reset");
                list.add("set");
            }
            StringUtil.copyPartialMatches(args[1], list, completions);
        } else {
            boolean wantedCommands = string.startsWith(commands.WANTED_CHECK.command) || string.startsWith(commands.WANTED_SET.command)
                    || string.startsWith(commands.WANTED_ADD.command) || string.startsWith(commands.WANTED_REMOVE.command)
                    || string.startsWith(commands.WANTED_RESET.command);
            if (args.length <= 3){
                if (string.startsWith(commands.KARMA_RESET.command)) {
                    list.add("true");
                    list.add("false");
                } else if (wantedCommands) {
                    Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
                }
                StringUtil.copyPartialMatches(args[2], list, completions);
            } else if (args.length <= 4){
                if (string.startsWith(commands.KARMA_SET.command) || string.startsWith(commands.KARMA_ADD.command)
                || string.startsWith(commands.KARMA_REMOVE.command)) {
                    list.add("true");
                    list.add("false");
                } else if (wantedCommands) {
                    if (configData.wantedDurationExpression != null) {
                        list.add(configData.wantedDurationExpression);
                    }
                    if (configData.wantedMaxDurationExpression != null) {
                        list.add(configData.wantedMaxDurationExpression);
                    }
                }
                StringUtil.copyPartialMatches(args[3], list, completions);
            }
        }
        Collections.sort(completions);
        return completions;
    }

    boolean canLaunchCommand(CommandSender sender, commands command) {
        if (!(sender instanceof Player) || sender.hasPermission(command.perm)) {
            return true;
        }
        permissionDenied(sender, command);
        return false;
    }

    private void permissionDenied(CommandSender sender, commands command) {
        String message = LangManager.getMessage(LangMessage.PERMISSION_DENIED);
        if (message != null) {
            message = AdaptMessage.getAdaptMessage().adapt((Player) sender, message, PlayerType.PLAYER.getText());
            message = message.replaceAll("%command%", command.command);
            message = message.replaceAll("%permission%", command.perm);
            sender.sendMessage(message);
        }
    }

    /**
     * @param sender
     */
    void disconnectedPlayer(CommandSender sender) {
        sender.sendMessage(adaptMessage.adapt(null, LangManager.getMessage(LangMessage.DISCONNECTED), null));
    }

    void errorMessage(CommandSender sender, Exception e) {
        if (e instanceof ArrayIndexOutOfBoundsException) {
            sender.sendMessage(adaptMessage.adapt(null, LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS), null));
        }
        if (e instanceof NumberFormatException) {
            sender.sendMessage(adaptMessage.adapt(null, LangManager.getMessage(LangMessage.WRONG_VALUE), null));
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

    /**
     * Is used when an argument is used with the command
     * Is necessary if commandSender isn't a player.
     * @param sender
     * @param playerString
     */
    private void karmaOther(CommandSender sender, String playerString)
    {
        if (!canLaunchCommand(sender, commands.KARMA_OTHER)) {
            return;
        }
        Player player;

        try {
            player = Bukkit.getServer().getPlayer(playerString);
        } catch (ArrayIndexOutOfBoundsException e) {
            errorMessage(sender, e);
            return;
        }

        if (player != null && player.isOnline()) {
            sender.sendMessage(adaptMessage.adapt(player, LangManager.getMessage(LangMessage.CHECK_OTHER_KARMA), PlayerType.PLAYER.getText()));
        } else {
            disconnectedPlayer(sender);
        }
    }

    /**
     * Used when a player use /karma without argument behind
     * @param sender
     */
    private void karmaSelf(CommandSender sender)
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage(adaptMessage.adapt(null, LangManager.getMessage(LangMessage.BY_PLAYER_ONLY), PlayerType.PLAYER.getText()));
            return;
        }
        Player player = (Player) sender;
        if (canLaunchCommand(player, commands.KARMA_CHECK)) {
            sender.sendMessage(adaptMessage.adapt(player, LangManager.getMessage(LangMessage.CHECK_OWN_KARMA), PlayerType.PLAYER.getText()));
        }
    }

    /**
     * The value is now the new karma of the target player.
     * @param sender
     * @param args
     */
    private void karmaSet(CommandSender sender, String[] args) {
        if (canLaunchCommand(sender, commands.KARMA_SET)) {
            Player player;
            double value;
            boolean reset = true;
            try {
                player = Bukkit.getPlayerExact(args[1]);
                value = Double.parseDouble(args[2]);
                try {
                    reset = Boolean.parseBoolean(args[3]);
                } catch (Exception ignored) {

                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                errorMessage(sender, e);
                return;
            }
            if (player != null) {
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, value, reset, Cause.COMMAND);
                tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.SET_KARMA);
            } else {
                disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Add the value to the actual Karma of the target.
     * Put a negative number remove some karma.
     * @param sender
     * @param args
     */
    private void karmaAdd(CommandSender sender, String[] args) {
        if (canLaunchCommand(sender, commands.KARMA_ADD)) {
            Player player;
            double value;
            boolean reset = true;
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                value = Double.parseDouble(args[2]);
                try {
                    reset = Boolean.parseBoolean(args[3]);
                } catch (Exception ignored) {

                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, playerData.getKarma() + value, reset, Cause.COMMAND);
                tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.ADD_KARMA);
            } else {
                disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Substract the karma of target player by the value
     * use a negative number make the karma increase
     * @param sender
     * @param args
     */
    private void karmaRemove(CommandSender sender, String[] args) {
        if (canLaunchCommand(sender, commands.KARMA_REMOVE)) {
            Player player;
            double value;
            boolean reset = true;
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                value = Double.parseDouble(args[2]);
                try {
                    reset = Boolean.parseBoolean(args[3]);
                } catch (Exception ignored) {

                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, playerData.getKarma() - value, reset, Cause.COMMAND);
                tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.REMOVE_KARMA);
            } else {
                disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Set the karma of target player as default, specified in config.yml
     * @param sender
     * @param args
     */
    private void karmaReset(CommandSender sender, String[] args) {
        if (canLaunchCommand(sender, commands.KARMA_RESET)) {
            Player player;
            boolean reset = true;
            try {
                player = Bukkit.getServer().getPlayer(args[1]);
                try {
                    reset = Boolean.parseBoolean(args[2]);
                } catch (Exception ignored) {

                }
            } catch (ArrayIndexOutOfBoundsException e) {
                errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                double resKarma = configData.defaultKarma;
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, resKarma, reset, Cause.COMMAND);
                tryKarmaChange(playerKarmaChangeEvent, sender, LangMessage.RESET_KARMA);
            } else {
                disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Is used when an argument is used with the command
     * Is necessary if commandSender isn't a player.
     * @param sender
     * @param playerString
     */
    private void wantedOther(CommandSender sender, String playerString)
    {
        if (!canLaunchCommand(sender, commands.WANTED_OTHER)) {
            return;
        }
        Player player;

        try {
            player = Bukkit.getServer().getPlayer(playerString);
        } catch (ArrayIndexOutOfBoundsException e) {
            errorMessage(sender, e);
            return;
        }

        if (player != null && player.isOnline()) {
            sender.sendMessage(adaptMessage.adapt(player, LangManager.getMessage(LangMessage.WANTED_OTHER_CHECK), PlayerType.PLAYER.getText()));
        } else {
            disconnectedPlayer(sender);
        }
    }

    /**
     * Used when a player use '/karma wanted check' without argument behind
     * @param sender
     */
    private void wantedSelf(CommandSender sender)
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage(adaptMessage.adapt(null, LangManager.getMessage(LangMessage.BY_PLAYER_ONLY), PlayerType.PLAYER.getText()));
            return;
        }
        Player player = (Player) sender;
        if (canLaunchCommand(player, commands.WANTED_CHECK)) {
            sender.sendMessage(adaptMessage.adapt(player, LangManager.getMessage(LangMessage.WANTED_OWN_CHECK), PlayerType.PLAYER.getText()));
        }
    }

    /**
     * The value is now the new karma of the target player.
     * @param sender
     * @param args
     */
    private void wantedSet(CommandSender sender, String[] args) {
        if (canLaunchCommand(sender, commands.WANTED_SET)) {
            Player player;
            Timestamp value;
            try {
                String playerName = args[2];
                player = Bukkit.getPlayerExact(playerName);
                String expression;
                ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
                expressionList.remove("wanted");
                expressionList.remove("set");
                expressionList.remove(playerName);
                AdaptMessage.timeRegexAdapt(expressionList);
                expression = String.join(" ", expressionList);
                expression = AdaptMessage.getAdaptMessage().adapt(player, expression, PlayerType.PLAYER.getText());
                value = new Timestamp((long) ExpressionCalculator.eval(expression));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, value, Cause.COMMAND);
                tryWantedChange(playerWantedChangeEvent, player, LangMessage.SET_WANTED);
            } else {
                disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Add the value to the actual Karma of the target.
     * Put a negative number remove some karma.
     * @param sender
     * @param args
     */
    private void wantedAdd(CommandSender sender, String[] args) {
        if (canLaunchCommand(sender, commands.WANTED_ADD)) {
            Player player;
            Timestamp value;
            try {
                String playerName = args[2];
                player = Bukkit.getPlayerExact(playerName);
                String expression;
                ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
                expressionList.remove("wanted");
                expressionList.remove("add");
                expressionList.remove(playerName);
                AdaptMessage.timeRegexAdapt(expressionList);
                expression = String.join(" ", expressionList);
                expression = AdaptMessage.getAdaptMessage().adapt(player, expression, PlayerType.PLAYER.getText());
                value = new Timestamp(PlayerDataManager.getPlayerDataMap().get(player).getWantedTimeStamp().getTime() + (long) ExpressionCalculator.eval(expression));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, value, Cause.COMMAND);
                tryWantedChange(playerWantedChangeEvent, player, LangMessage.ADD_WANTED);
            } else {
                disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Substract the karma of target player by the value
     * use a negative number make the karma increase
     * @param sender
     * @param args
     */
    private void wantedRemove(CommandSender sender, String[] args) {
        if (canLaunchCommand(sender, commands.WANTED_REMOVE)) {
            Player player;
            Timestamp value;
            try {
                String playerName = args[2];
                player = Bukkit.getPlayerExact(playerName);
                String expression;
                ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
                expressionList.remove("wanted");
                expressionList.remove("remove");
                expressionList.remove(playerName);
                AdaptMessage.timeRegexAdapt(expressionList);
                expression = String.join(" ", expressionList);
                expression = AdaptMessage.getAdaptMessage().adapt(player, expression, PlayerType.PLAYER.getText());
                value = new Timestamp(PlayerDataManager.getPlayerDataMap().get(player).getWantedTimeStamp().getTime() - (long) ExpressionCalculator.eval(expression));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, value, Cause.COMMAND);
                tryWantedChange(playerWantedChangeEvent, player, LangMessage.REMOVE_WANTED);
            } else {
                disconnectedPlayer(sender);
            }
        }
    }

    /**
     * Set the karma of target player as default, specified in config.yml
     * @param sender
     * @param args
     */
    private void wantedReset(CommandSender sender, String[] args) {
        if (canLaunchCommand(sender, commands.WANTED_RESET)) {
            Player player;
            Timestamp value;
            try {
                String playerName = args[2];
                player = Bukkit.getPlayerExact(playerName);
                value = new Timestamp(0);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                errorMessage(sender, e);
                return;
            }
            if (player != null && player.isOnline()) {
                PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, value, Cause.COMMAND);
                tryWantedChange(playerWantedChangeEvent, player, LangMessage.RESET_WANTED);
            } else {
                disconnectedPlayer(sender);
            }
        }
    }

    private void karmaSave(CommandSender sender) {
        if (canLaunchCommand(sender, commands.KARMA_SAVE)) {
            Map<Player, PlayerData> playersData = PlayerDataManager.getPlayerDataMap();
            PlayerDataManager.saveData(DBInteractions.reasons.COMMAND, playersData);
            sender.sendMessage(adaptMessage.adapt(null, LangManager.getMessage(LangMessage.SAVED_DATA)
                    .replaceAll("%number%", String.valueOf(playersData.size())), null));
        }
    }

    private void tryKarmaChange(PlayerKarmaChangeEvent playerKarmaChangeEvent, CommandSender sender, LangMessage message) {
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        if (!playerKarmaChangeEvent.isCancelled()) {
            sender.sendMessage(adaptMessage.adapt(playerKarmaChangeEvent.getPlayer(), LangManager.getMessage(message), PlayerType.PLAYER.getText()));
        }
    }

    private void tryWantedChange(PlayerWantedChangeEvent playerWantedChangeEvent, CommandSender sender, LangMessage message) {
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
        if (!playerWantedChangeEvent.isCancelled()) {
            sender.sendMessage(adaptMessage.adapt(playerWantedChangeEvent.getPlayer(), LangManager.getMessage(message), PlayerType.PLAYER.getText()));
        }
    }
}
