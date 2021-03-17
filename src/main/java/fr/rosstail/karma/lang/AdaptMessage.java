package fr.rosstail.karma.lang;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.apis.PAPI;
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
    private final int nbDec;
    private final boolean msgStyle;

    public AdaptMessage(Karma plugin) {
        this.plugin = plugin;
        this.nbDec = plugin.getConfig().getInt("general.decimal-number-to-show");
        this.msgStyle = plugin.getConfig().getBoolean("general.use-action-bar-on-actions");
    }

    private final Map<String, Long> coolDown = new HashMap<String, Long>();

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    /**
     * Sends automatically the message to the sender with some parameters
     *
     * @param sender  the sender, can be console, player or null.
     */
    public void message(CommandSender sender, Player player, double value, String message) {
        if (message == null) {
            return;
        }

        if (player != null) {
            PlayerData playerData = PlayerData.gets(player, plugin);
            double playerKarma = playerData.getKarma();
            double playerPreviousKarma = playerData.getPreviousKarma();

            Tier playerTier = playerData.getTier();

            String playerDisplayTier;
            if (playerTier != null) {
                playerDisplayTier = playerTier.getDisplay();
            } else {
                playerDisplayTier = ChatColor.translateAlternateColorCodes('&', "&fNone");
            }

            message = message.replaceAll("<TIER>", playerDisplayTier);
            message = message.replaceAll("<PLAYER>", player.getName());
            message = message.replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));
            message = message.replaceAll("<OLD_KARMA>", String.format("%." + nbDec + "f", playerPreviousKarma));
        }
        message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", value));

        message = papi.setPlaceholdersOnMessage(message, player);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (sender != null) {
            sender.sendMessage(message);
        } else if (player != null){
            player.sendMessage(message);
        }
    }

    public void entityHitMessage(String message, Player player, double value) {
        PlayerData playerData = PlayerData.gets(player, plugin);
        double playerKarma = playerData.getKarma();

        if (message == null) {
            return;
        }
        message = message.replaceAll("<ATTACKER>", player.getName());
        message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", value));
        message = message
            .replaceAll("<OLD_KARMA>", String.format("%." + nbDec + "f", playerKarma - value));
        message = message.replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));

        message = papi.setPlaceholdersOnMessage(message, player);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (coolDown.containsKey(player.getName())) {
            double seconds = this.plugin.getConfig().getDouble("general.delay-between-hit-messages");
            double timeLeft =
                coolDown.get(player.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        coolDown.put(player.getName(), System.currentTimeMillis());
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
        message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", playerKarma - playerPreviousKarma));
        message = message
            .replaceAll("<OLD_KARMA>", String.format("%." + nbDec + "f", playerPreviousKarma));
        message = message.replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));

        message = papi.setPlaceholdersOnMessage(message, player);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (coolDown.containsKey(player.getName())) {
            double seconds =
                this.plugin.getConfig().getDouble("general.delay-between-kill-messages");
            double timeLeft =
                coolDown.get(player.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        coolDown.put(player.getName(), System.currentTimeMillis());
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
        message = message
            .replaceAll("<ATTACKER_OLD_KARMA>", String.format("%." + nbDec + "f", value));
        message = message
            .replaceAll("<VALUE>", String.format("%." + nbDec + "f", attackerKarma - value));
        message = message
            .replaceAll("<ATTACKER_KARMA>", String.format("%." + nbDec + "f", attackerKarma));

        message = message.replaceAll("<ATTACKER_TIER>", attackerData.getTier().getDisplay());

        message = message
            .replaceAll("<VICTIM_KARMA>", String.format("%." + nbDec + "f", victimKarma));
        message = message.replaceAll("<VICTIM_TIER>", victimData.getTier().getDisplay());

        message = papi.setPlaceholdersOnMessage(message, attacker);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (coolDown.containsKey(attacker.getName())) {
            double seconds = this.plugin.getConfig().getDouble("general.delay-between-hit-messages");
            double timeLeft =
                coolDown.get(attacker.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        coolDown.put(attacker.getName(), System.currentTimeMillis());
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
        message = message
            .replaceAll("<ATTACKER_OLD_KARMA>", String.format("%." + nbDec + "f", value));
        message = message
            .replaceAll("<VALUE>", String.format("%." + nbDec + "f", killerKarma - value));
        message = message
            .replaceAll("<ATTACKER_KARMA>", String.format("%." + nbDec + "f", killerKarma));

        message = message.replaceAll("<ATTACKER_TIER>", killerData.getTier().getDisplay());

        message = message
            .replaceAll("<VICTIM_KARMA>", String.format("%." + nbDec + "f", victimKarma));
        message = message.replaceAll("<VICTIM_TIER>", victimData.getTier().getDisplay());

        message = papi.setPlaceholdersOnMessage(message, killer);
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (coolDown.containsKey(killer.getName())) {
            double seconds =
                this.plugin.getConfig().getDouble("general.delay-between-kill-messages");
            double timeLeft =
                coolDown.get(killer.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        coolDown.put(killer.getName(), System.currentTimeMillis());
        if (msgStyle) {
            sendActionBar(killer, message);
        } else {
            killer.sendMessage(message);
        }
    }

    public static AdaptMessage getAdaptMessage() {
        return adaptMessage;
    }

    public static void initAdaptMessage(Karma plugin) {
        adaptMessage = new AdaptMessage(plugin);
    }
}
