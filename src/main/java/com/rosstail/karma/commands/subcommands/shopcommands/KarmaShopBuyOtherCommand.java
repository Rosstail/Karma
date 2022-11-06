package com.rosstail.karma.commands.subcommands.shopcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.shops.Shop;
import com.rosstail.karma.shops.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KarmaShopBuyOtherCommand extends SubCommand {

    public KarmaShopBuyOtherCommand() {
        help = AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.HELP_SHOP).replaceAll("%command-syntax%", getSyntax()), null);
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
        return "karma shop buy <shopname> <player>";
    }

    @Override
    public String getPermission() {
        return "karma.command.shop.self";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        String shopName = args[2];
        String playerName = args[3];
        Player player = Bukkit.getPlayerExact(playerName);

        if (player == null) {
            CommandManager.disconnectedPlayer(sender);
            return;
        }

        if (ShopManager.getShopManager().getShops().containsKey(shopName)) {
            Shop shop = ShopManager.getShopManager().getShops().get(shopName);
            shop.handle(player);
        } else {
            AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.SHOP_NOT_EXIST), null);
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
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
