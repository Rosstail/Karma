package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Change the karma of the target, check the limit fork and new tier after.
 */
public class EditKarmaCommand extends GetSet{
    private Karma karma = Karma.get();
    String message = null;

    File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
    YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);

    public EditKarmaCommand() {
    }

    /**
     * The value is now the new karma of the target player.
     * @param commandSender
     * @param args
     */
    public void karmaSet(CommandSender commandSender, String[] args)
    {
        Player player = Bukkit.getServer().getPlayer(args[1]);
        int value = Integer.parseInt(args[2]);
        if (player != null && player.isOnline()) {

            setKarmaToPlayer(player, value);

            message = configurationLang.getString("set-karma");
            if (message != null) {
                message = message.replaceAll("<player>", player.getName());
                message = message.replaceAll("<newKarma>", Integer.toString(getPlayerKarma(player)));
                message = message.replaceAll("<tier>", getPlayerDisplayTier(player));
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            }
        }
        else {
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
        int value = Integer.parseInt(args[2]);
        if (player != null && player.isOnline()) {
            int targetNewKarma = getPlayerKarma(player) + value;

            setKarmaToPlayer(player, targetNewKarma);
            setTierToPlayer(player);

            message = configurationLang.getString("add-karma");

            if (message != null) {
                message = message.replaceAll("<player>", player.getName());
                message = message.replaceAll("<value>", Integer.toString(value));
                message = message.replaceAll("<newKarma>", Integer.toString(getPlayerKarma(player)));
                message = message.replaceAll("<tier>", getPlayerDisplayTier(player));
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            }

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
        int value = Integer.parseInt(args[2]);
        if (player != null && player.isOnline()) {
            int targetNewKarma = getPlayerKarma(player) - value;

            setKarmaToPlayer(player, targetNewKarma);

            message = configurationLang.getString("remove-karma");

            if (message != null) {
                message = message.replaceAll("<player>", player.getName());
                message = message.replaceAll("<value>", Integer.toString(value));
                message = message.replaceAll("<newKarma>", Integer.toString(getPlayerKarma(player)));
                message = message.replaceAll("<tier>", getPlayerDisplayTier(player));
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            }

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
            int resKarma = this.karma.getConfig().getInt("karma.default-karma");

            setKarmaToPlayer(player, resKarma);

            message = configurationLang.getString("reset-karma");
            if (message != null) {
                message = message.replaceAll("<player>", player.getName());
                message = message.replaceAll("<newKarma>", Integer.toString(getPlayerKarma(player)));
                message = message.replaceAll("<tier>", getPlayerDisplayTier(player));
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            }
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
        message = configurationLang.getString("disconnected-player");

        if (message != null) {
            message = message.replaceAll("<player>", args[1]);
            message = ChatColor.translateAlternateColorCodes('&', message);
            commandSender.sendMessage(message);
        }
    }
}