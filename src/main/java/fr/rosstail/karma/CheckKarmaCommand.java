package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * This command able the commandSender to see what is the Karma and Karma Tier of a conected user
 */
public class CheckKarmaCommand {
    private Karma karma = Karma.getInstance();
    SetTier setTier = new SetTier();
    String message = null;
    Getters getters = new Getters();

    public CheckKarmaCommand() {
    }

    /**
     * Is used when an argument is used with the command
     * Is necessary if commandSender isn't a player.
     * @param commandSender
     * @param args
     */
    public void karmaOther(CommandSender commandSender, String[] args)
    {
        Player player = Bukkit.getServer().getPlayer(args[0]);

        if (player != null && player.isOnline()) {
            message = karma.getConfig().getString("messages.check-other-karma");
            int targetKarma = getters.getPlayerKarma(player);
            String targetTierDisplay = setTier.checkTier(player);

            message = message.replaceAll("<karma>", String.valueOf(targetKarma));
            message = message.replaceAll("<tier>", String.valueOf(targetTierDisplay));
        } else {
            message = karma.getConfig().getString("messages.disconnected-player");
        }

        if (message != null) {
            message = message.replaceAll("<player>", args[0]);
            message = ChatColor.translateAlternateColorCodes('&', message);
            commandSender.sendMessage(message);
        }
    }

    /**
     * Used when a player use /karma without argument behind
     * @param commandSender
     */
    public void karmaSelf(CommandSender commandSender)
    {
        Player player = (Player) commandSender;
        int playerKarma = getters.getPlayerKarma(player);
        String playerTierDisplay = setTier.checkTier(player);

        File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
        YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);
        message = configurationLang.getString("check-own-karma");
        if (message != null) {
            message = message.replaceAll("<player>", player.getName());
            message = message.replaceAll("<karma>", String.valueOf(playerKarma));
            message = message.replaceAll("<tier>", String.valueOf(playerTierDisplay));

            message = ChatColor.translateAlternateColorCodes('&', message);
            player.sendMessage(message);
        }
    }
}