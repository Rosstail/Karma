package com.rosstail.karma.commands;

import com.rosstail.karma.Karma;
import com.rosstail.karma.apis.ExpressionCalculator;
import com.rosstail.karma.commands.list.Commands;
import com.rosstail.karma.configdata.ConfigData;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rosstail.karma.commands.list.Commands.*;
/**
 * Checking what method/class will be used on command, depending of command Sender and number of args.
 */
public class KarmaCommand implements CommandExecutor, TabExecutor {
    private final AdaptMessage adaptMessage;
    private final CheckKarmaCommand checkKarmaCommand;
    private final EditKarmaCommand editKarmaCommand;

    public KarmaCommand() {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
        this.checkKarmaCommand = new CheckKarmaCommand(this);
        this.editKarmaCommand = new EditKarmaCommand(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String string = String.join(" ", args);
        if (!canLaunchCommand(sender, COMMAND_KARMA)) {
            return false;
        }

        if (string.startsWith(COMMAND_KARMA_CHECK.getCommand())) {
            if (args.length == 2) {
                checkKarmaCommand.karmaOther(sender, args[1]);
            } else {
                checkKarmaCommand.karmaSelf(sender);
            }
        } else if (string.startsWith(COMMAND_KARMA_SET.getCommand())) {
            editKarmaCommand.karmaSet(sender, args);
        } else if (string.startsWith(COMMAND_KARMA_ADD.getCommand())) {
            editKarmaCommand.karmaAdd(sender, args);
        } else if (string.startsWith(COMMAND_KARMA_REMOVE.getCommand())) {
            editKarmaCommand.karmaRemove(sender, args);
        } else if (string.startsWith(COMMAND_KARMA_RESET.getCommand())) {
            editKarmaCommand.karmaReset(sender, args);
        } else if (string.startsWith(COMMAND_KARMA_CALCULATE.getCommand())) {
          if (canLaunchCommand(sender, COMMAND_KARMA_CALCULATE)) {
              if (args.length > 1) {
                  ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(args));
                  expressionList.remove("calculate");
                  String expression = String.join(" ", expressionList);
                  Player player = null;
                  if (sender instanceof Player) {
                      player = ((Player) sender).getPlayer();
                      expression = adaptMessage.message(player, expression, null);
                  }
                  double result = ExpressionCalculator.eval(expression);

                  sender.sendMessage(adaptMessage.message(player,
                          LangManager.getMessage(LangMessage.CALCULATION)
                                  .replaceAll("%expression%", expression).replaceAll("%result%", String.valueOf(result))
                          , null));
              } else {
                  errorMessage(sender, new ArrayIndexOutOfBoundsException());
              }
          }
        } else if (string.startsWith(COMMAND_KARMA_RELOAD.getCommand())) {
            if (canLaunchCommand(sender, COMMAND_KARMA_RELOAD)) {
                ConfigData.applyNewConfigValues(Karma.getInstance().getCustomConfig());
                sender.sendMessage(adaptMessage.message(null, LangManager.getMessage(LangMessage.RELOAD), null));
            }
        }
        else if (string.startsWith(COMMAND_KARMA_HELP.getCommand())) {
            if (canLaunchCommand(sender, COMMAND_KARMA_HELP)) {
                sender.sendMessage(adaptMessage.listMessage(null, LangManager.getListMessage(LangMessage.HELP)));
            }
        } else if (string.startsWith("test")) { //EXPERIMENTATIONS TO MAKE CALCULATIONS WITH SOME EVALS ON eval(X) PLACEHOLDERS
            if (args.length >= 2) {
                /*
                */
                ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
                argList.remove("test");
                String arg = String.join(" ", argList);
                if (arg.contains("eval")) {
                    if (arg.matches("^eval\\((?:[^)(]+|\\((?:[^)(]+|\\([^)(]*\\))*\\))*\\)$")) {
                        Pattern p = Pattern.compile("^eval\\((?:[^)(]+|\\((?:[^)(]+|\\([^)(]*\\))*\\))*\\)$");
                        Matcher m = p.matcher(arg);

                        while (m.find()) {
                            String matched = m.group();
                            if (arg.contains(matched)) {
                                String value = String.valueOf(ExpressionCalculator.eval(matched.replace("eval", "")));
                                arg = arg.replaceAll(matched, value);
                            }
                        }
                    }
                    sender.sendMessage(arg);
                }
                /*
                 */
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
        List<String> commands = new ArrayList<>();
        String string = String.join(" ", args);

        if (args.length <= 1) {
            commands.add("set");
            commands.add("check");
            commands.add("add");
            commands.add("remove");
            commands.add("reset");
            commands.add("help");
            commands.add("calculate");
            commands.add("reload");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length <= 2) {
            if (string.startsWith(COMMAND_KARMA_CALCULATE.getCommand()) && sender.hasPermission(COMMAND_KARMA_CALCULATE.getPermission())) {
                commands.add(ConfigData.getConfigData().getPvpHitRewardExpression());
                commands.add(ConfigData.getConfigData().getPvpKillRewardExpression());
            } else if (string.startsWith(COMMAND_KARMA_SET.getCommand()) || string.startsWith(COMMAND_KARMA_ADD.getCommand())
                    || string.startsWith(COMMAND_KARMA_REMOVE.getCommand()) || string.startsWith(COMMAND_KARMA_RESET.getCommand())
                    || string.startsWith(COMMAND_KARMA_CHECK.getCommand())) {
                Bukkit.getOnlinePlayers().forEach(player -> commands.add(player.getName()));
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        } else if (args.length <= 3){
            if (string.startsWith(COMMAND_KARMA_RESET.getCommand())) {
                commands.add("true");
                commands.add("false");
            }
            StringUtil.copyPartialMatches(args[2], commands, completions);
        } else if (args.length <= 4){
            if (string.startsWith(COMMAND_KARMA_SET.getCommand()) || string.startsWith(COMMAND_KARMA_ADD.getCommand())
            || string.startsWith(COMMAND_KARMA_REMOVE.getCommand())) {
                commands.add("true");
                commands.add("false");
            }
            StringUtil.copyPartialMatches(args[3], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    boolean canLaunchCommand(CommandSender sender, Commands command) {
        if (!(sender instanceof Player) || sender.hasPermission(command.getPermission())) {
            return true;
        }
        permissionDenied(sender, command);
        return false;
    }

    private void permissionDenied(CommandSender sender, Commands command) {
        String message = LangManager.getMessage(LangMessage.PERMISSION_DENIED);
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            message = message.replaceAll("%command%", command.getCommand());
            message = message.replaceAll("%permission%", command.getPermission());
            sender.sendMessage(message);
        }
    }

    /**
     * @param sender
     */
    void disconnectedPlayer(CommandSender sender) {
        sender.sendMessage(adaptMessage.message(null, LangManager.getMessage(LangMessage.DISCONNECTED), null));
    }

    void errorMessage(CommandSender sender, Exception e) {
        if (e instanceof ArrayIndexOutOfBoundsException) {
            sender.sendMessage(adaptMessage.message(null, LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS), null));
        }
        if (e instanceof NumberFormatException) {
            sender.sendMessage(adaptMessage.message(null, LangManager.getMessage(LangMessage.WRONG_VALUE), null));
        }
    }
}
