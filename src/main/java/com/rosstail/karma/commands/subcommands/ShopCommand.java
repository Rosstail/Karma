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
import org.bukkit.entity.Player;

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
        if (args.length >= 3) {
            if (args.length >= 4) {
                subCommands.get(1).perform(sender, args, arguments);
            } else {
                subCommands.get(0).perform(sender, args, arguments);
            }
        } else {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_SHOP_HEADER)));
            for (Shop shop : ShopManager.getShopManager().getShops().values()) {
                String line = LangManager.getMessage(LangMessage.COMMANDS_SHOP_LINE);
                line = line.replaceAll("\\[shop_name]", shop.getName());
                line = line.replaceAll("\\[shop_display]", shop.getDisplay());
                line = line.replaceAll("\\[shop_description]", shop.getDescription());
                line = line.replaceAll("\\[shop_price]", String.valueOf(shop.getPrice()));
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(line));
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        if (args.length <= 2) {
            ArrayList<String> subCommands = new ArrayList<>();
            for (SubCommand subCommand : getSubCommands()) {
                subCommands.add(subCommand.getName());
            }
            return subCommands;
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (args[1].equalsIgnoreCase(subCommand.getName())) {
                    return subCommand.getSubCommandsArguments(sender, args, arguments);
                }
            }
        }
        return null;
    }
}
