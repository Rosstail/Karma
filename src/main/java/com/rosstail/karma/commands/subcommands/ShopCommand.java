package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.shopcommands.KarmaShopBuyOtherCommand;
import com.rosstail.karma.commands.subcommands.shopcommands.KarmaShopBuySelfCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.shops.Shop;
import com.rosstail.karma.shops.ShopManager;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ShopCommand extends SubCommand {

    public ShopCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_SHOP_BUY_DESC))
                .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new KarmaShopBuySelfCommand());
        subCommands.add(new KarmaShopBuyOtherCommand());
    }

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public String getDescription() {
        return "Displays a list of shops.";
    }

    @Override
    public String getSyntax() {
        return "karma shop buy <shopname>";
    }

    @Override
    public String getPermission() {
        return "karma.command.shop";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

        if (args.length >= 3) {
            if (args.length >= 4) {
                subCommands.get(1).perform(sender, args, arguments);
            } else {
                subCommands.get(0).perform(sender, args, arguments);
            }
        } else {
            StringBuilder message = new StringBuilder();
            message.append(adaptMessage.adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_SHOP_HEADER)));
            for (Shop shop : ShopManager.getShopManager().getShops().values()) {
                String line = LangManager.getMessage(LangMessage.COMMANDS_SHOP_LINE);
                line = shop.adaptMessage(line);
                message.append("\n").append(line);
            }
            String footer = LangManager.getMessage(LangMessage.COMMANDS_SHOP_FOOTER);
            if (footer != null) {
                message.append("\n").append(adaptMessage.adaptMessage(footer));
            }
            sender.sendMessage(adaptMessage.adaptMessage(message.toString()));
        }
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length <= 2) {
            return getSubCommands().stream().map(SubCommand::getName).toList();
        } else {
            SubCommand subCommand = getSubCommands().stream()
                    .filter(command -> command.getName().equalsIgnoreCase(args[1]))
                    .findFirst().orElse(null);

            if (subCommand == null) {
                return null;
            }

            return subCommand.getSubCommandsArguments(sender, args, arguments);
        }
    }
}
