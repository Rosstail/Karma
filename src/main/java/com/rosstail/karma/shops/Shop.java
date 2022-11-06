package com.rosstail.karma.shops;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class Shop {

    private String name;
    private String display;
    private String description;
    private float price;
    private boolean useMinKarma = false;
    private boolean useMaxKarma = false;
    private boolean costResetOvertime;
    private float minShopKarma;
    private float maxShopKarma;
    private SendType sendType;
    private List<String> commands;

    public void init(ConfigurationSection section) {
        display = AdaptMessage.getAdaptMessage().adapt(null, section.getString("display", name.toUpperCase()), null);
        description = AdaptMessage.getAdaptMessage().adapt(null, section.getString("description", "&c-&r"), null);
        price = (float) section.getDouble("price", 0f);
        useMinKarma = section.get("min-karma") != null;
        useMaxKarma = section.get("max-karma") != null;
        costResetOvertime = section.getBoolean("cost-reset-overtime", true);
        minShopKarma = (float) section.getDouble("min-karma", ConfigData.getConfigData().defaultKarma);
        maxShopKarma = (float) section.getDouble("max-karma", ConfigData.getConfigData().defaultKarma);
        sendType = SendType.valueOf(section.getString("send-by", "both").toUpperCase());
        commands = section.getStringList("commands");
    }

    Shop(String name) {
        this.name = name;
    }

    private boolean check(PlayerData playerData) {
        if (useMinKarma && playerData.getKarma() < minShopKarma) {
            return false;
        }
        return !useMaxKarma || !(playerData.getKarma() > maxShopKarma);
    }

    public void handle(Player target) {
        PlayerData playerData = PlayerDataManager.getNoSet(target);
        if (check(playerData)) {
            PlayerKarmaChangeEvent event = new PlayerKarmaChangeEvent(target, playerData.getKarma() - price, costResetOvertime, this);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                CommandManager.commandsLauncher(target, commands);
                target.sendMessage(AdaptMessage.getAdaptMessage().adapt(target, LangManager.getMessage(LangMessage.SHOP_SUCCESS), PlayerType.PLAYER.getText()));
            }
        } else {
            target.sendMessage(AdaptMessage.getAdaptMessage().adapt(target, LangManager.getMessage(LangMessage.SHOP_FAILURE), PlayerType.PLAYER.getText()));
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

    public SendType getSendType() {
        return sendType;
    }
}
