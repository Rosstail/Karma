package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.commands.subcommands.shopcommands.KarmaShopBuyOtherCommand;
import com.rosstail.karma.commands.subcommands.shopcommands.KarmaShopBuySelfCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.shops.Shop;
import com.rosstail.karma.shops.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ShopCommand extends SubCommand {

    public ShopCommand() {
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
        return "karma shop <shopname>";
    }

    @Override
    public String getPermission() {
        return "karma.command.shop";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        ShopManager shopManager = ShopManager.getShopManager();
        if (args.length > 1 && shopManager.getShops().containsKey(args[1])) {
            Shop shop = ShopManager.getShopManager().getShops().get(args[1]);
            Player target = null;
            if (args.length > 2) {
                if (CommandManager.canLaunchCommand(sender, subCommands.get(1))) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().equals(args[2])) {
                            target = player;
                            break;
                        }
                    }
                    if (target == null) {
                        sender.sendMessage("(not lang file) This player does not exist");
                    }
                }
            } else {
                if (sender instanceof Player) {
                    target = ((Player) sender).getPlayer();
                } else {
                    sender.sendMessage("(not lang file) Shop is only available to players");
                }
            }
            if (target != null) {
                shop.handle(target);
            }
        } else {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.SHOP_HEADER), PlayerType.PLAYER.getText()));
            for (Shop shop : ShopManager.getShopManager().getShops().values()) {
                String line = LangManager.getMessage(LangMessage.SHOP_LINE);
                line = line.replaceAll("%karma_shop_display%", shop.getName());
                line = line.replaceAll("%karma_shop_description%", shop.getDescription());
                line = line.replaceAll("%karma_shop_price%", String.valueOf(shop.getPrice()));
                sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, line, PlayerType.PLAYER.getText()));
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
