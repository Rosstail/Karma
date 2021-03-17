package fr.rosstail.karma.commands;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.datas.PlayerData;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Change the karma of the target, check the limit fork and new tier after.
 */
public class EditKarmaCommand {

    private final Karma plugin;
    private final AdaptMessage adaptMessage;

    public EditKarmaCommand(Karma plugin) {
        this.plugin = plugin;
        this.adaptMessage = AdaptMessage.getAdaptMessage();
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
            PlayerData playerData = PlayerData.gets(player, plugin);
            playerData.setKarma(value);
            adaptMessage.message(commandSender, player, value, LangManager.getMessage(LangMessage.SET_KARMA));
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
            PlayerData playerData = PlayerData.gets(player, plugin);
            double targetNewKarma = playerData.getKarma() + value;

            playerData.setKarma(targetNewKarma);

            adaptMessage.message(commandSender, player, value, LangManager.getMessage(LangMessage.ADD_KARMA));

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
            PlayerData playerData = PlayerData.gets(player, plugin);
            double targetNewKarma = playerData.getKarma() - value;

            playerData.setKarma(targetNewKarma);

            adaptMessage.message(commandSender, player, value, LangManager.getMessage(LangMessage.REMOVE_KARMA));

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
            PlayerData playerData = PlayerData.gets(player, plugin);
            double resKarma = plugin.getConfig().getDouble("karma.default-karma");

            playerData.setKarma(resKarma);

            adaptMessage.message(commandSender, player, 0, LangManager.getMessage(LangMessage.RESET_KARMA));
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
        adaptMessage.message(commandSender, player, 0, LangManager.getMessage(LangMessage.DISCONNECTED));
    }
}