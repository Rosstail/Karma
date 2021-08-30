package fr.rosstail.karma.lang;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.apis.ExpressionCalculator;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.datas.PlayerData;
import fr.rosstail.karma.events.Reasons;
import fr.rosstail.karma.tiers.Tier;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdaptMessage {

    private static AdaptMessage adaptMessage;
    private final Karma plugin;
    private final ConfigData configData;
    private final boolean msgStyle;

    public AdaptMessage(Karma plugin) {
        this.plugin = plugin;
        this.msgStyle = plugin.getCustomConfig().getBoolean("general.use-action-bar-on-actions");
        configData = ConfigData.getConfigData();
    }

    private final Map<Player, Long> coolDown = new HashMap<>();

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(adaptMessage.message(player, message, PlayerType.player.getId())));
    }

    /**
     * Sends automatically the message to the sender with some parameters
     *
     */
    public String message(Player player, String message, String playerType) {
        if (message == null) {
            return null;
        }

        String pluginName = plugin.getName().toLowerCase();

        if (player != null) {
            message = message.replaceAll("%" + playerType + "%", player.getName());
            if (!player.hasMetadata("NPC")) {
                PlayerData playerData = PlayerData.gets(player);
                double playerKarma = playerData.getKarma();
                double playerPreviousKarma = playerData.getPreviousKarma();

                Tier playerTier = playerData.getTier();
                Tier playerPreviousTier = playerData.getPreviousTier();
                Timestamp lastAttack = playerData.getLastAttack();

                message = message.replaceAll("%" + pluginName + "_" + playerType + "_karma%", decimalFormat(playerKarma));
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_previous_karma%", decimalFormat(playerPreviousKarma));
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_diff_karma%", decimalFormat(playerKarma - playerPreviousKarma));
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_tier%", playerTier.getName());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_previous_tier%", playerPreviousTier.getName());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_tier_display%", playerTier.getDisplay());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_previous_tier_display%", playerPreviousTier.getDisplay());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigData.getConfigData().getDateTimeFormat());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_last_attack%", simpleDateFormat.format(lastAttack.getTime()));
            } else {
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_karma%", decimalFormat(player.getMetadata("Karma").get(0).asDouble()));
            }
        }
        return ChatColor.translateAlternateColorCodes('&', setPlaceholderMessage(player, message));
    }

    public String[] listMessage(Player player, List<String> messages) {
        ArrayList<String> newMessages = new ArrayList<>();
        messages.forEach(s -> {
            newMessages.add(message(player, s, PlayerType.player.getId()));
        });
        return newMessages.toArray(new String[0]);
    }

    public void entityHitMessage(String message, Player player, Reasons reason) {
        if (message == null) {
            return;
        }
        if (coolDown.containsKey(player)) {
            double seconds = plugin.getCustomConfig().getDouble("general.delay-between-" + reason.getText() + "-messages");
            double timeLeft = coolDown.get(player) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        message = message(player, message, PlayerType.attacker.getId());

        coolDown.put(player, System.currentTimeMillis());
        if (msgStyle) {
            sendActionBar(player, message);
        } else {
            player.sendMessage(message);
        }
    }

    public void playerHitMessage(String message, Player attacker, Player victim, String hitKill) {
        if (message == null) {
            return;
        }
        if (coolDown.containsKey(attacker)) {
            double seconds = plugin.getCustomConfig().getDouble("general.delay-between-" + hitKill + "-messages");
            double timeLeft = coolDown.get(attacker) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        message = message(attacker, message, PlayerType.attacker.getId());
        message = message(victim, message, PlayerType.victim.getId());

        coolDown.put(attacker, System.currentTimeMillis());
        if (msgStyle) {
            sendActionBar(attacker, message);
        } else {
            attacker.sendMessage(message);
        }
    }

    private String decimalFormat(double value) {
        return String.format("%." + configData.getDecNumber() + "f", value).replaceAll(",", ".");
    }

    public static AdaptMessage getAdaptMessage() {
        return adaptMessage;
    }

    public static void initAdaptMessage(Karma plugin) {
        adaptMessage = new AdaptMessage(plugin);
    }

    private String setPlaceholderMessage(Player player, String message) {
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }
}
