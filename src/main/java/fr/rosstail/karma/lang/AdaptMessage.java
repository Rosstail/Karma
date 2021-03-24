package fr.rosstail.karma.lang;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.apis.PAPI;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.datas.PlayerData;
import fr.rosstail.karma.tiers.Tier;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AdaptMessage {
    private final PAPI papi = new PAPI();

    private static AdaptMessage adaptMessage;
    private final Karma plugin;
    private final ConfigData configData;
    private final boolean msgStyle;

    public AdaptMessage(Karma plugin) {
        this.plugin = plugin;
        this.msgStyle = plugin.getConfig().getBoolean("general.use-action-bar-on-actions");
        configData = ConfigData.getConfigData();
    }

    private final Map<Player, Long> coolDown = new HashMap<>();

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    /**
     * Sends automatically the message to the sender with some parameters
     *
     * @param sender  the sender, can be console, player or null.
     */
    public String message(CommandSender sender, Player player, double value, String message) {
        if (message == null) {
            return null;
        }

        if (player != null) {
            PlayerData playerData = PlayerData.gets(player, plugin);
            double playerKarma = playerData.getKarma();
            double playerPreviousKarma = playerData.getPreviousKarma();

            Tier playerTier = playerData.getTier();
            Tier playerPreviousTier = playerData.getPreviousTier();

            String playerDisplayTier;
            if (playerTier != null) {
                playerDisplayTier = playerTier.getDisplay();
            } else {
                playerDisplayTier = ChatColor.translateAlternateColorCodes('&', "&fNone");
            }
            String playerPreviousDisplayTier;
            if (playerPreviousTier != null) {
                playerPreviousDisplayTier = playerPreviousTier.getDisplay();
            } else {
                playerPreviousDisplayTier = ChatColor.translateAlternateColorCodes('&', "&fNone");
            }

            message = message.replaceAll("<TIER>", playerDisplayTier);
            message = message.replaceAll("<PREVIOUS_TIER>", playerPreviousDisplayTier);
            message = message.replaceAll("<PLAYER>", player.getName());
            message = message.replaceAll("<KARMA>", decimalFormat(playerKarma));
            message = message.replaceAll("<OLD_KARMA>", decimalFormat(playerPreviousKarma));
        }
        message = message.replaceAll("<VALUE>", decimalFormat(value));

        message = papi.setPlaceholdersOnMessage(message, player);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void entityHitMessage(String message, Player player, double value) {
        if (message == null) {
            return;
        }
        if (coolDown.containsKey(player)) {
            double seconds = this.plugin.getConfig().getDouble("general.delay-between-hit-messages");
            double timeLeft = coolDown.get(player) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        PlayerData playerData = PlayerData.gets(player, plugin);
        double playerKarma = playerData.getKarma();

        message = message.replaceAll("<ATTACKER>", player.getName());
        message = message.replaceAll("<VALUE>", decimalFormat(value));
        message = message.replaceAll("<OLD_KARMA>", decimalFormat(playerKarma - value));
        message = message.replaceAll("<KARMA>", decimalFormat(playerKarma));

        message = papi.setPlaceholdersOnMessage(message, player);
        message = ChatColor.translateAlternateColorCodes('&', message);

        coolDown.put(player, System.currentTimeMillis());
        if (msgStyle) {
            sendActionBar(player, message);
        } else {
            player.sendMessage(message);
        }
    }

    public void entityKillMessage(String message, Player player) {
        PlayerData playerData = PlayerData.gets(player, plugin);
        double playerKarma = playerData.getKarma();
        double playerPreviousKarma = playerData.getPreviousKarma();
        if (message == null) {
            return;
        }
        message = message.replaceAll("<ATTACKER>", player.getName());
        message = message.replaceAll("<VALUE>", decimalFormat(playerKarma - playerPreviousKarma));
        message = message.replaceAll("<OLD_KARMA>", decimalFormat(playerPreviousKarma));
        message = message.replaceAll("<KARMA>", decimalFormat(playerKarma));

        message = papi.setPlaceholdersOnMessage(message, player);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (coolDown.containsKey(player)) {
            double seconds = this.plugin.getConfig().getDouble("general.delay-between-kill-messages");
            double timeLeft = coolDown.get(player) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        coolDown.put(player, System.currentTimeMillis());
        if (msgStyle) {
            sendActionBar(player, message);
        } else {
            player.sendMessage(message);
        }
    }

    public void playerHitMessage(String message, Player attacker, Player victim, double value) {
        PlayerData attackerData = PlayerData.gets(attacker, plugin);
        PlayerData victimData = PlayerData.gets(victim, plugin);
        double attackerKarma = attackerData.getKarma();
        double victimKarma = victimData.getKarma();

        if (message == null) {
            return;
        }
        message = message.replaceAll("<ATTACKER>", attacker.getName());
        message = message.replaceAll("<VICTIM>", victim.getName());
        message = message.replaceAll("<ATTACKER_OLD_KARMA>", decimalFormat(value));
        message = message.replaceAll("<VALUE>", decimalFormat(attackerKarma - value));
        message = message.replaceAll("<ATTACKER_KARMA>", decimalFormat(attackerKarma));

        message = message.replaceAll("<ATTACKER_TIER>", attackerData.getTier().getDisplay());

        message = message.replaceAll("<VICTIM_KARMA>", decimalFormat(victimKarma));
        message = message.replaceAll("<VICTIM_TIER>", victimData.getTier().getDisplay());

        message = papi.setPlaceholdersOnMessage(message, attacker);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (coolDown.containsKey(attacker)) {
            double seconds = this.plugin.getConfig().getDouble("general.delay-between-hit-messages");
            double timeLeft =
                coolDown.get(attacker) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        coolDown.put(attacker, System.currentTimeMillis());
        if (msgStyle) {
            sendActionBar(attacker, message);
        } else {
            attacker.sendMessage(message);
        }
    }

    public void playerKillMessage(String message, Player killer, Player victim, double value) {
        PlayerData killerData = PlayerData.gets(killer, plugin);
        PlayerData victimData = PlayerData.gets(victim, plugin);
        double killerKarma = killerData.getKarma();
        double victimKarma = victimData.getKarma();

        if (message == null) {
            return;
        }
        message = message.replaceAll("<ATTACKER>", killer.getName());
        message = message.replaceAll("<VICTIM>", victim.getName());
        message = message.replaceAll("<ATTACKER_OLD_KARMA>", decimalFormat(value));
        message = message.replaceAll("<VALUE>", decimalFormat(killerKarma - value));
        message = message.replaceAll("<ATTACKER_KARMA>", decimalFormat(killerKarma));

        message = message.replaceAll("<ATTACKER_TIER>", killerData.getTier().getDisplay());

        message = message.replaceAll("<VICTIM_KARMA>", decimalFormat(victimKarma));
        message = message.replaceAll("<VICTIM_TIER>", victimData.getTier().getDisplay());

        message = papi.setPlaceholdersOnMessage(message, killer);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (coolDown.containsKey(killer)) {
            double seconds =
                this.plugin.getConfig().getDouble("general.delay-between-kill-messages");
            double timeLeft =
                coolDown.get(killer) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        coolDown.put(killer, System.currentTimeMillis());
        if (msgStyle) {
            sendActionBar(killer, message);
        } else {
            killer.sendMessage(message);
        }
    }

    private String decimalFormat(double value) {
        return String.format("%." + configData.getDecNumber() + "f", value);
    }

    public static AdaptMessage getAdaptMessage() {
        return adaptMessage;
    }

    public static void initAdaptMessage(Karma plugin) {
        adaptMessage = new AdaptMessage(plugin);
    }
}
