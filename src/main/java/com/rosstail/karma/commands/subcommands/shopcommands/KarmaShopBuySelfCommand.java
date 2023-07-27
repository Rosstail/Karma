package com.rosstail.karma.commands.subcommands.shopcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.shops.SendType;
import com.rosstail.karma.shops.Shop;
import com.rosstail.karma.shops.ShopManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KarmaShopBuySelfCommand extends SubCommand {

    public KarmaShopBuySelfCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("%desc%", LangManager.getMessage(LangMessage.COMMANDS_SHOP_BUY_DESC))
                        .replaceAll("%syntax%", getSyntax()));
    }

    @Override
    public String getName() {
        return "buy";
    }

    @Override
    public String getDescription() {
        return "Check your own Karma status";
    }

    @Override
    public String getSyntax() {
        return "karma shop buy <shopName>";
    }

    @Override
    public String getPermission() {
        return "karma.command.shop.self";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY)));
            return;
        }

        String shopName = args[2];

        if (ShopManager.getShopManager().getShops().containsKey(shopName)) {
            Shop shop = ShopManager.getShopManager().getShops().get(shopName);
            if (shop.getSendType() != SendType.CONSOLE) {
                shop.handle(((Player) sender).getPlayer());
            } else {
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_SHOP_BUY_FAILURE)));
            }
        } else {
            AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_SHOP_NOT_EXIST));
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        if (args.length <= 3) {
            ArrayList<String> shops = new ArrayList<>();
            ShopManager.getShopManager().getShops().forEach((s, shop) -> {
                shops.add(s);
            });
            return shops;
        }

        return null;
    }
}
