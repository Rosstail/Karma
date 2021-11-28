package com.rosstail.karma.lang;

import com.rosstail.karma.Karma;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.tiers.Tier;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getLogger;

public class AdaptMessage {

    private static AdaptMessage adaptMessage;
    private final Karma plugin;
    private final ConfigData configData;
    private final Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public enum prints{
        OUT,
        WARNING,
        ERROR;
    }

    public AdaptMessage(Karma plugin) {
        this.plugin = plugin;
        configData = ConfigData.getConfigData();
    }

    private final Map<Player, Long> coolDown = new HashMap<>();

    public void sendToPlayer(Player player, String message) {
        if (message != null) {
            if (message.startsWith("%msg-title%")) {
                message = message.replace("%msg-title%", "").trim();
                String title;
                String subTitle = null;
                String[] titles = message.split("%msg-subtitle%");
                title = titles[0];
                if (titles.length > 1) {
                    subTitle = titles[1];
                }
                sendTitle(player, title.trim(), subTitle.trim());
            } else if (message.startsWith("%msg-actionbar%")) {
                sendActionBar(player, message.replace("%msg-actionbar%", "").trim());
            } else if (message.startsWith("%msg%")) {
                player.sendMessage(message.replace("%msg%", "").trim());
            } else {
                player.sendMessage(message);
            }
        }
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(adaptMessage.adapt(player, message, PlayerType.player.toString())));
    }

    private void sendTitle(Player player, String title, String subTitle) {
        player.sendTitle(adaptMessage.adapt(player, title, PlayerType.player.toString()),
                adaptMessage.adapt(player, subTitle, PlayerType.player.toString()), configData.titleFadeIn, configData.titleStay, configData.titleFadeOut);
    }

    public String adapt(Player player, String message, String playerType) {
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
                Timestamp wantedTime = playerData.getWantedTimeStamp();

                String status;

                if (playerData.isWanted()) {
                    status = LangManager.getMessage(LangMessage.STATUS_WANTED);
                } else {
                    status = LangManager.getMessage(LangMessage.STATUS_INNOCENT);
                }

                message = message.replaceAll("%" + pluginName + "_" + playerType + "_karma%", decimalFormat(playerKarma));
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_previous_karma%", decimalFormat(playerPreviousKarma));
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_diff_karma%", decimalFormat(playerKarma - playerPreviousKarma));
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_tier%", playerTier.getName());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_previous_tier%", playerPreviousTier.getName());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_tier_display%", playerTier.getDisplay());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_tier_short_display%", playerTier.getShortDisplay());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_previous_tier_display%", playerPreviousTier.getDisplay());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_previous_tier_short_display%", playerPreviousTier.getShortDisplay());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_wanted_status%", status);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigData.getConfigData().getDateTimeFormat());
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_wanted_time%", String.valueOf(wantedTime.getTime()));
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_wanted_time_display%", simpleDateFormat.format(wantedTime.getTime()));
            } else {
                message = message.replaceAll("%" + pluginName + "_" + playerType + "_karma%", decimalFormat(player.getMetadata("Karma").get(0).asDouble()));
            }
        }
        message = message.replaceAll("%timestamp%", String.valueOf(System.currentTimeMillis()));

        message = ChatColor.translateAlternateColorCodes('&', setPlaceholderMessage(player, message));
        if (Integer.parseInt(Bukkit.getVersion().split("\\.")[1]) >= 16) {
            Matcher matcher = hexPattern.matcher(message);
            while (matcher.find()) {
                try {
                    String color = message.substring(matcher.start(), matcher.end());
                    message = message.replaceAll(color, String.valueOf(ChatColor.of(color)));
                    matcher = hexPattern.matcher(message);
                } catch (Exception e) {}
            }
        }
        return message;
    }

    public String[] listMessage(Player player, List<String> messages) {
        ArrayList<String> newMessages = new ArrayList<>();
        messages.forEach(s -> {
            newMessages.add(adapt(player, s, PlayerType.player.toString()));
        });
        return newMessages.toArray(new String[0]);
    }

    public void entityHitMessage(String message, Player player, Event event) {
        if (message == null) {
            return;
        }
        if (coolDown.containsKey(player)) {
            double seconds = 0;

            if (event instanceof EntityDamageByEntityEvent) {
                seconds = configData.hitMessageDelay;
            } else if (event instanceof EntityDeathEvent) {
                seconds = configData.killMessageDelay;
            }
            double timeLeft = coolDown.get(player) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        message = adapt(player, message, PlayerType.attacker.toString());

        coolDown.put(player, System.currentTimeMillis());
        sendToPlayer(player, message);
    }

    public String playerHitAdapt(String message, Player attacker, Player victim, Object cause) {
        if (message == null) {
            return null;
        }
        if (coolDown.containsKey(attacker)) {
            double seconds;

            if(cause instanceof EntityDamageByEntityEvent) {
                seconds = configData.hitMessageDelay;
            } else {
                seconds = configData.killMessageDelay;
            }
            double timeLeft = coolDown.get(attacker) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return null;
            }
        }

        message = adapt(attacker, message, PlayerType.attacker.toString());
        message = adapt(victim, message, PlayerType.victim.toString());

        coolDown.put(attacker, System.currentTimeMillis());
        return message;
    }

    private String decimalFormat(double value) {
        return String.format("%." + configData.decNumber + "f", value).replaceAll(",", ".");
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

    public static void print(String string, prints cause) {
        if (cause.equals(prints.ERROR)) {
            getLogger().severe(string);
        } else if (cause.equals(prints.WARNING)) {
            getLogger().warning(string);
        } else {
            getLogger().info(string);
        }
    }

    public static ArrayList<String> timeRegexAdapt(ArrayList<String> expressionList) {
        expressionList.forEach(s -> {
            //Days
            if (s.matches("[0-9]*d")) {
                expressionList.set(expressionList.indexOf(s),
                        String.valueOf(Long.parseLong(s.replaceAll("d", "")) * 86400000));
            }
            //Hours
            if (s.matches("[0-9]*h")) {
                expressionList.set(expressionList.indexOf(s),
                        String.valueOf(Long.parseLong(s.replaceAll("h", "")) * 3600000));
            }
            //minutes
            if (s.matches("[0-9]*m")) {
                expressionList.set(expressionList.indexOf(s),
                        String.valueOf(Long.parseLong(s.replaceAll("m", "")) * 60000));
            }
            //seconds
            if (s.matches("[0-9]*s")) {
                expressionList.set(expressionList.indexOf(s),
                        String.valueOf(Long.parseLong(s.replaceAll("s", "")) * 1000));
            }
        });

        return expressionList;
    }
}
