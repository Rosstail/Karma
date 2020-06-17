package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Change the karma of the target, check the limit fork and new tier after.
 */
public class EditKarmaCommand {
    String message = null;

    private final Karma plugin;
    private final File langFile;
    private final YamlConfiguration configLang;
    private final AdaptMessage adaptMessage;
    private final GetSet getSet;
    private final PAPI papi;

    public EditKarmaCommand(Karma plugin) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder(), "lang/" + plugin.getConfig().getString("general.lang") + ".yml");
        this.configLang = YamlConfiguration.loadConfiguration(langFile);
        this.adaptMessage = new AdaptMessage(plugin);
        this.getSet = new GetSet(plugin);
        this.papi = new PAPI();
    }

    /**
     * The value is now the new karma of the target player.
     * @param commandSender
     * @param args
     */
    public void karmaSet(CommandSender commandSender, String[] args)
    {
        Player player = Bukkit.getServer().getPlayer(args[1]);
        double value = Double.parseDouble(args[2]);
        if (player != null && player.isOnline()) {

            getSet.setKarmaToPlayer(player, value);

            message = configLang.getString("set-karma");
            adaptMessage.message(commandSender, player, value, message);
        } else {
            disconnectedPlayer(commandSender, args);
        }
    }

    /**
     * Add the value to the actual Karma of the target.
     * Put a negative number remove some karma.
     * @param commandSender
     * @param args
     */
    public void karmaAdd(CommandSender commandSender, String[] args)
    {
        Player player = Bukkit.getServer().getPlayer(args[1]);
        double value = Double.parseDouble(args[2]);
        if (player != null && player.isOnline()) {
            double targetNewKarma = getSet.getPlayerKarma(player) + value;

            getSet.setKarmaToPlayer(player, targetNewKarma);
            getSet.setTierToPlayer(player);

            message = configLang.getString("add-karma");
            adaptMessage.message(commandSender, player, value, message);

        } else {
            disconnectedPlayer(commandSender, args);
        }
    }

    /**
     * Substract the karma of target player by the value
     * use a negative number make the karma increase
     * @param commandSender
     * @param args
     */
    public void karmaRemove(CommandSender commandSender, String[] args)
    {
        Player player = Bukkit.getServer().getPlayer(args[1]);
        double value = Double.parseDouble(args[2]);
        if (player != null && player.isOnline()) {
            double targetNewKarma = getSet.getPlayerKarma(player) - value;

            getSet.setKarmaToPlayer(player, targetNewKarma);

            message = configLang.getString("remove-karma");
            adaptMessage.message(commandSender, player, value, message);

        } else {
            disconnectedPlayer(commandSender, args);
        }
    }

    /**
     * Set the karma of target player as default, specified in config.yml
     * @param commandSender
     * @param args
     */
    public void karmaReset(CommandSender commandSender, String[] args) {
        Player player = Bukkit.getServer().getPlayer(args[1]);
        if (player != null && player.isOnline()) {
            double resKarma = plugin.getConfig().getDouble("karma.default-karma");

            getSet.setKarmaToPlayer(player, resKarma);

            message = configLang.getString("reset-karma");
            adaptMessage.message(commandSender, player, 0, message);
        }
        else {
            disconnectedPlayer(commandSender, args);
        }
    }

    /**
     * @param commandSender
     * @param args
     */
    private void disconnectedPlayer(CommandSender commandSender, String[] args) {
        Player player = Bukkit.getServer().getPlayer(args[1]);
        message = configLang.getString("disconnected-player");
        adaptMessage.message(commandSender, player, 0, message);
    }
}