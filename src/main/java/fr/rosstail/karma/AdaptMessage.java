package fr.rosstail.karma;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AdaptMessage {
    private PAPI papi = new PAPI();

    private final Karma plugin;
    private final File langFile;
    private final YamlConfiguration configLang;
    private int nbDec;
    private boolean msgStyle;
    private GetSet getSet;

    AdaptMessage(Karma plugin) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder(), "lang/" + plugin.getConfig().getString("general.lang") + ".yml");
        this.configLang = YamlConfiguration.loadConfiguration(langFile);
        this.nbDec = plugin.getConfig().getInt("general.decimal-number-to-show");
        this.msgStyle = plugin.getConfig().getBoolean("general.use-action-bar-on-actions");
        this.getSet = new GetSet(plugin);
    }

    private Map<String, Long> cooldown = new HashMap<String, Long>();

    /**
     * Sends automatically the message to the sender with some parameters
     * @param sender the sender, can be console, player or null.
     * @param player the player targetted
     * @param value the value. Can be reward or a simple value
     * @param message the content of the message
     */
    public void message(CommandSender sender, Player player, double value, String message) {

        if (message != null) {

            if (player != null) {
                double playerKarma = getSet.getPlayerKarma(player);
                message = message.replaceAll("<PLAYER>", player.getName());
                message = message.replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));
                message = message.replaceAll("<TIER>", getSet.getPlayerDisplayTier(player));
                message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", value));
                message = message.replaceAll("<OLD_KARMA>", String.format("%." + nbDec + "f", playerKarma - value));
            }

            message = papi.setPlaceholdersOnMessage(message, player);
            message = ChatColor.translateAlternateColorCodes('&', message);
            if (message != null) {
                if (sender != null) {
                    sender.sendMessage(message);
                } else {
                    player.sendMessage(message);
                }
            }
        }
    }

    public void entityHitMessage(String message, Player player, double value) {
        double playerKarma = getSet.getPlayerKarma(player);

        if (message != null) {
            message = message.replaceAll("<ATTACKER>", player.getName());
            message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", value));
            message = message.replaceAll("<OLD_KARMA>", String.format("%." + nbDec + "f", playerKarma - value));
            message = message.replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));

            message = papi.setPlaceholdersOnMessage(message, player);
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        if (cooldown.containsKey(player.getName())) {
            double seconds = plugin.getConfig().getDouble("general.delay-between-hit-messages");
            double timeLeft = cooldown.get(player.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        cooldown.put(player.getName(), System.currentTimeMillis());
        if (message != null) {
            if (msgStyle) {
                sendActionBar(player, message);
            } else {
                player.sendMessage(message);
            }
        }
    }

    public void entityKillMessage(String message, Player player, double value) {
        double playerKarma = getSet.getPlayerKarma(player);


        if (message != null) {
            message = message.replaceAll("<ATTACKER>", player.getName());
            message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", value));
            message = message.replaceAll("<OLD_KARMA>", String.format("%." + nbDec + "f", playerKarma - value));
            message = message.replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));

            message = papi.setPlaceholdersOnMessage(message, player);
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        if (cooldown.containsKey(player.getName())) {
            double seconds = plugin.getConfig().getDouble("general.delay-between-kill-messages");
            double timeLeft = cooldown.get(player.getName()) - System.currentTimeMillis() + seconds * 1000f;

            if (!(timeLeft <= 0)) {
                return;
            }
        }

        cooldown.put(player.getName(), System.currentTimeMillis());
        if (message != null) {
            if (msgStyle) {
                sendActionBar(player, message);
            } else {
                player.sendMessage(message);
            }
        }
    }

    public void playerHitMessage(String message, Player attacker, Player victim, double value) {
        double attackerKarma = getSet.getPlayerKarma(attacker);
        double victimKarma = getSet.getPlayerKarma(victim);

        if (message != null) {
            message = message.replaceAll("<ATTACKER>", attacker.getName());
            message = message.replaceAll("<VICTIM>", victim.getName());
            message = message.replaceAll("<ATTACKER_OLD_KARMA>", String.format("%." + nbDec + "f", value));
            message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", attackerKarma - value));
            message = message.replaceAll("<ATTACKER_KARMA>", String.format("%." + nbDec + "f", attackerKarma));
            message = message.replaceAll("<ATTACKER_TIER>", getSet.getPlayerDisplayTier(attacker));

            message = message.replaceAll("<VICTIM_KARMA>", String.format("%." + nbDec + "f", victimKarma));
            message = message.replaceAll("<VICTIM_TIER>", getSet.getPlayerDisplayTier(victim));

            message = papi.setPlaceholdersOnMessage(message, attacker);
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        if (cooldown.containsKey(attacker.getName())) {
            double seconds = plugin.getConfig().getDouble("general.delay-between-hit-messages");
            double timeLeft = cooldown.get(attacker.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        cooldown.put(attacker.getName(), System.currentTimeMillis());
        if (message != null) {
            if (msgStyle) {
                sendActionBar(attacker, message);
            } else {
                attacker.sendMessage(message);
            }
        }
    }

    public void playerKillMessage(String message, Player killer, Player victim, double value) {
        double killerKarma = getSet.getPlayerKarma(killer);
        double victimKarma = getSet.getPlayerKarma(victim);

        if (message != null) {
            message = message.replaceAll("<ATTACKER>", killer.getName());
            message = message.replaceAll("<VICTIM>", victim.getName());
            message = message.replaceAll("<ATTACKER_OLD_KARMA>", String.format("%." + nbDec + "f", value));
            message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", killerKarma - value));
            message = message.replaceAll("<ATTACKER_KARMA>", String.format("%." + nbDec + "f", killerKarma));
            message = message.replaceAll("<ATTACKER_TIER>", getSet.getPlayerDisplayTier(killer));

            message = message.replaceAll("<VICTIM_KARMA>", String.format("%." + nbDec + "f", victimKarma));
            message = message.replaceAll("<VICTIM_TIER>", getSet.getPlayerDisplayTier(victim));

            message = papi.setPlaceholdersOnMessage(message, killer);
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        if (cooldown.containsKey(killer.getName())) {
            double seconds = plugin.getConfig().getDouble("general.delay-between-kill-messages");
            double timeLeft = cooldown.get(killer.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        cooldown.put(killer.getName(), System.currentTimeMillis());
        if (message != null) {
            if (msgStyle) {
                sendActionBar(killer, message);
            } else {
                killer.sendMessage(message);
            }
        }
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

}