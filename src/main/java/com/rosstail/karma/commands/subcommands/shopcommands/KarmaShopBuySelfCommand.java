package com.rosstail.karma.commands.subcommands.shopcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.shops.ShopManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KarmaShopBuySelfCommand extends SubCommand {

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
        return "karma shop <shopName>";
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

        if (!(sender instanceof Player)) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.BY_PLAYER_ONLY), PlayerType.PLAYER.getText()));
            return;
        }
        sender.sendMessage(AdaptMessage.getAdaptMessage().adapt((Player) sender, LangManager.getMessage(LangMessage.WANTED_OWN_CHECK), PlayerType.PLAYER.getText()));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 1) {
            ArrayList<String> shops = new ArrayList<>();
            ShopManager.getShopManager().getShops().forEach((s, shop) -> {
                shops.add(s);
            });
            return shops;
        }

        return null;
    }
}
