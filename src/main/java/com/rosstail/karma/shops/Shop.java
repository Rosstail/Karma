package com.rosstail.karma.shops;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.players.PlayerDataManager;
import com.rosstail.karma.players.PlayerModel;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shop {

    private final String name;
    private String display;
    private String description;
    private float price;
    private boolean useMinKarma = false;
    private boolean useMaxKarma = false;
    private boolean costResetOvertime;
    private float minShopKarma;
    private float maxShopKarma;
    private List<String> commands;

    public void init(ConfigurationSection section) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        display = adaptMessage.adaptMessage(section.getString("display", name.toUpperCase()));
        price = (float) section.getDouble("price", 0f);
        useMinKarma = section.get("min-karma") != null;
        useMaxKarma = section.get("max-karma") != null;
        costResetOvertime = section.getBoolean("cost-reset-overtime", true);
        minShopKarma = (float) section.getDouble("min-karma");
        maxShopKarma = (float) section.getDouble("max-karma");
        commands = section.getStringList("commands");

        List<String> desc = new ArrayList<>();
        if (section.isList("description")) {
            for (String s : section.getStringList("description")) {
                desc.add(adaptMessage(s));
            }
        } else {
            desc.add(adaptMessage(section.getString("description")));
        }
        description = String.join("\n", desc);
    }

    Shop(String name) {
        this.name = name;
    }

    private boolean checkHasKarma(PlayerModel model) {
        if (useMinKarma && model.getKarma() < minShopKarma) {
            return false;
        }

        return !useMaxKarma || !(model.getKarma() > maxShopKarma);
    }

    public void handle(Player target) {
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(target.getName());
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        if (checkHasKarma(model)) {
            PlayerKarmaChangeEvent event = new PlayerKarmaChangeEvent(target, model, model.getKarma() - price, true);
            Bukkit.getPluginManager().callEvent(event);
            //after event done
            CommandManager.commandsLauncher(target, commands);
            target.sendMessage(adaptMessage.adaptMessage(adaptMessage.adaptPlayerMessage(target, LangManager.getMessage(LangMessage.COMMANDS_SHOP_BUY_SUCCESS), PlayerType.PLAYER.getText())));
        } else {
            target.sendMessage(adaptMessage.adaptMessage(adaptMessage.adaptPlayerMessage(target, LangManager.getMessage(LangMessage.COMMANDS_SHOP_BUY_FAILURE), PlayerType.PLAYER.getText())));
        }
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public String getDescription() {
        return description;
    }

    public float getPrice() {
        return price;
    }

    public String adaptMessage(String message) {
        if (message == null) {
            return null;
        }
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

        message = message.replace("[shop_display]", display)
                .replace("[shop_price]", adaptMessage.decimalFormat(price, '.'))
                .replace("[shop_min_karma]", adaptMessage.decimalFormat(minShopKarma, '.'))
                .replace("[shop_max_karma]", adaptMessage.decimalFormat(maxShopKarma, '.'))
                .replaceAll("\\[shop_desc]", description)
                .replaceAll("\\[shop_description]", description);

        return message;
    }
}
