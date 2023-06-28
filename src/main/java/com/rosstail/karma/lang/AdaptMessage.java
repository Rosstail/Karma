package com.rosstail.karma.lang;

import com.rosstail.karma.Karma;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getLogger;

public class AdaptMessage {

    private static AdaptMessage adaptMessage;
    private final Karma plugin;
    private static final Pattern hexPattern = Pattern.compile("\\{(#[a-fA-F0-9]{6})}");

    public enum prints {
        OUT,
        WARNING,
        ERROR;
    }

    public AdaptMessage(Karma plugin) {
        this.plugin = plugin;
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
                sendTitle(player, title.trim(), subTitle != null ? subTitle.trim() : null);
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
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(adaptMessage.adapt(player, message, PlayerType.PLAYER.getText())));
    }

    private void sendTitle(Player player, String title, String subTitle) {
        ConfigData configData = ConfigData.getConfigData();
        player.sendTitle(adaptMessage.adapt(player, title, PlayerType.PLAYER.getText()),
                adaptMessage.adapt(player, subTitle, PlayerType.PLAYER.getText()), configData.titleFadeIn, configData.titleStay, configData.titleFadeOut);
    }

    public String adapt(Player player, String message, String playerType) {
        if (message == null) {
            return null;
        }

        String pluginName = plugin.getName().toLowerCase();

        if (player != null) {
            message = message.replaceAll("%" + playerType + "%", player.getName());
            String playerPluginPlaceholder = "%" + pluginName + "_" + playerType + "_";
            if (!player.hasMetadata("NPC")) {
                PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
                float playerKarma = model.getKarma();
                float playerPreviousKarma = model.getPreviousKarma();
                float difPlayerDiffKarma = playerKarma - playerPreviousKarma;

                Tier playerTier = TierManager.getTierManager().getTierByName(model.getTierName());
                Tier playerPreviousTier = TierManager.getTierManager().getTierByName(model.getPreviousTierName());
                long wantedTime = PlayerDataManager.getWantedTimeLeft(model);
                Timestamp wantedTimeStamp = model.getWantedTimeStamp();

                String status;
                String shortStatus;

                if (model.isWanted()) {
                    status = LangManager.getMessage(LangMessage.STATUS_WANTED);
                    shortStatus = LangManager.getMessage(LangMessage.STATUS_WANTED_SHORT);
                } else {
                    status = LangManager.getMessage(LangMessage.STATUS_INNOCENT);
                    shortStatus = LangManager.getMessage(LangMessage.STATUS_INNOCENT_SHORT);
                }

                message = message.replaceAll(playerPluginPlaceholder + "karma%", decimalFormat(playerKarma, '.'));
                message = message.replaceAll(playerPluginPlaceholder + "karma_abs%", decimalFormat(Math.abs(playerKarma), '.'));
                message = message.replaceAll(playerPluginPlaceholder + "karma_int%", String.valueOf((int) playerKarma));
                message = message.replaceAll(playerPluginPlaceholder + "karma_int_abs%", String.valueOf(Math.abs((int) playerKarma)));
                message = message.replaceAll(playerPluginPlaceholder + "previous_karma%", decimalFormat(playerPreviousKarma, '.'));
                message = message.replaceAll(playerPluginPlaceholder + "previous_karma_abs%", decimalFormat(Math.abs(playerPreviousKarma), '.'));
                message = message.replaceAll(playerPluginPlaceholder + "previous_karma_int%", String.valueOf((int) playerPreviousKarma));
                message = message.replaceAll(playerPluginPlaceholder + "previous_karma_int_abs%", String.valueOf(Math.abs((int) playerPreviousKarma)));
                message = message.replaceAll(playerPluginPlaceholder + "diff_karma%", decimalFormat(difPlayerDiffKarma, '.'));
                message = message.replaceAll(playerPluginPlaceholder + "diff_karma%_abs", decimalFormat(Math.abs(difPlayerDiffKarma), '.'));
                message = message.replaceAll(playerPluginPlaceholder + "diff_karma_int%", String.valueOf((int) difPlayerDiffKarma));
                message = message.replaceAll(playerPluginPlaceholder + "diff_karma_int_abs%", String.valueOf(Math.abs((int) difPlayerDiffKarma)));

                message = message.replaceAll(playerPluginPlaceholder + "tier%", playerTier.getName());
                message = message.replaceAll(playerPluginPlaceholder + "previous_tier%", playerPreviousTier.getName());
                message = message.replaceAll(playerPluginPlaceholder + "tier_display%", playerTier.getDisplay());
                message = message.replaceAll(playerPluginPlaceholder + "tier_short_display%", playerTier.getShortDisplay());
                message = message.replaceAll(playerPluginPlaceholder + "previous_tier_display%", playerPreviousTier.getDisplay());
                message = message.replaceAll(playerPluginPlaceholder + "previous_tier_short_display%", playerPreviousTier.getShortDisplay());
                message = message.replaceAll(playerPluginPlaceholder + "wanted_status%", status);
                message = message.replaceAll(playerPluginPlaceholder + "wanted_status_short%", shortStatus);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigData.getConfigData().getDateTimeFormat());
                message = message.replaceAll(playerPluginPlaceholder + "wanted_time%", String.valueOf(wantedTimeStamp.getTime()));
                message = message.replaceAll(playerPluginPlaceholder + "wanted_time_display%", simpleDateFormat.format(wantedTimeStamp.getTime()));
                message = message.replaceAll(playerPluginPlaceholder + "wanted_time_delay%", String.valueOf(wantedTime));
                message = message.replaceAll(playerPluginPlaceholder + "wanted_time_delay_display%", countDownFormat(wantedTime));
            } else {
                message = message.replaceAll(playerPluginPlaceholder + "karma%", decimalFormat(player.getMetadata("Karma").get(0).asFloat(), '.'));
            }
        }
        message = message.replaceAll("%timestamp%", String.valueOf(System.currentTimeMillis()));
        message = message.replaceAll("%now%", String.valueOf(System.currentTimeMillis()));

        message = ChatColor.translateAlternateColorCodes('&', setPlaceholderMessage(player, message));
        if (Integer.parseInt(Bukkit.getVersion().split("\\.")[1].replaceAll("\\)", "")) >= 16) {
            Matcher matcher = hexPattern.matcher(message);
            while (matcher.find()) {
                try {
                    String matched = matcher.group(0);
                    String color = matcher.group(1);
                    message = message.replace(matched, String.valueOf(ChatColor.of(color)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return message;
    }

    public String[] listMessage(Player player, List<String> messages) {
        ArrayList<String> newMessages = new ArrayList<>();
        messages.forEach(s -> {
            newMessages.add(adapt(player, s, PlayerType.PLAYER.getText()));
        });
        return newMessages.toArray(new String[0]);
    }

    public void entityHitMessage(String message, Player player, Event event) {
        ConfigData configData = ConfigData.getConfigData();
        if (message == null) {
            return;
        }
        if (coolDown.containsKey(player)) {
            float seconds = 0;

            if (event instanceof EntityDamageByEntityEvent) {
                if (((EntityDamageByEntityEvent) event).getEntity() instanceof Player) {
                    seconds = configData.pvpHitMessageDelay;
                } else {
                    seconds = configData.pveHitMessageDelay;
                }
            } else if (event instanceof EntityDeathEvent) {
                if (((EntityDeathEvent) event).getEntity() instanceof Player) {
                    seconds = configData.pvpKillMessageDelay;
                } else {
                    seconds = configData.pveKillMessageDelay;
                }
            }
            float timeLeft = coolDown.get(player) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        message = adapt(player, message, PlayerType.ATTACKER.getText());

        coolDown.put(player, System.currentTimeMillis());
        sendToPlayer(player, message);
    }

    public String playerHitAdapt(String message, Player attacker, Player victim, Object cause) {
        ConfigData configData = ConfigData.getConfigData();
        if (message == null) {
            return null;
        }
        if (coolDown.containsKey(attacker)) {
            float seconds;

            if (cause instanceof EntityDamageByEntityEvent) {
                seconds = configData.pvpHitMessageDelay;
            } else {
                seconds = configData.pvpKillMessageDelay;
            }
            float timeLeft = coolDown.get(attacker) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return null;
            }
        }

        message = adapt(attacker, message, PlayerType.ATTACKER.getText());
        message = adapt(victim, message, PlayerType.VICTIM.getText());

        coolDown.put(attacker, System.currentTimeMillis());
        return message;
    }

    public String decimalFormat(float value, char replacement) {
        ConfigData configData = ConfigData.getConfigData();
        return String.format("%." + configData.decNumber + "f", value).replaceAll(",", String.valueOf(replacement));
    }

    public String countDownFormat(long diff) {
        String format = ConfigData.getConfigData().getCountdownFormat();
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hoursInDay =TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diff));
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long minutesInHour = TimeUnit.MILLISECONDS.toMinutes(diff)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long secondsInMinute = TimeUnit.MILLISECONDS.toSeconds(diff)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff));

        format = format.replaceAll("\\{dd}", (days / 10 == 0 ? "0" : "") + days);
        format = format.replaceAll("\\{d}", String.valueOf(days));

        format = format.replaceAll("\\{HH}", (hoursInDay / 10 == 0 ? "0" : "") + hoursInDay);
        format = format.replaceAll("\\{H}", String.valueOf(hoursInDay));
        format = format.replaceAll("\\{hh}", (hours / 10 == 0 ? "0" : "") + hours);
        format = format.replaceAll("\\{h}", String.valueOf(hours));

        format = format.replaceAll("\\{mm}", (minutesInHour / 10 == 0 ? "0" : "") + minutesInHour);
        format = format.replaceAll("\\{m}", String.valueOf(minutesInHour));
        format = format.replaceAll("\\{MM}", (minutes / 10 == 0 ? "0" : "") + minutes);
        format = format.replaceAll("\\{M}", String.valueOf(minutes));

        format = format.replaceAll("\\{ss}", (secondsInMinute / 10 == 0 ? "0" : "") + secondsInMinute);
        format = format.replaceAll("\\{s}", String.valueOf(secondsInMinute));
        format = format.replaceAll("\\{SS}", (seconds / 10 == 0 ? "0" : "") + seconds);
        format = format.replaceAll("\\{S}", String.valueOf(seconds));

        return format;
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

    public static long calculateDuration(Long currentWantedTime, String expression) {
        List<String> matches = Arrays.asList("(\\d+)ms", "(\\d+)s", "(\\d+)m", "(\\d+)h", "(\\d+)d");
        List<Integer> ints = Arrays.asList(1, 1000, 60, 60, 24);

        int multiplier = 1;
        long totalTimeMs = 0;
        for (int i = 0; i < matches.size(); i++) {
            Pattern pattern = Pattern.compile(matches.get(i));
            multiplier *= ints.get(i);
            Matcher matcher = pattern.matcher(expression.replaceAll(" ", ""));
            if (matcher.find()) {
                totalTimeMs += (long) Integer.parseInt(String.valueOf(matcher.group(1))) * multiplier;
            }
        }

        if (expression.contains("%now%") || expression.contains("%timestamp%")) {
            totalTimeMs += System.currentTimeMillis();
        }
        if (expression.contains("%player_wanted_time%")) {
            if (currentWantedTime > 0L) {
                totalTimeMs += currentWantedTime;
            } else {
                totalTimeMs += System.currentTimeMillis();
            }
        }

        return totalTimeMs;
    }
}
