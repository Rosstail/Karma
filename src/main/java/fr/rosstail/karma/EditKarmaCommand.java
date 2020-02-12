package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * Change the karma of the target, check the limit fork and new tier after.
 */
public class EditKarmaCommand {
    private Karma karma = Karma.getInstance();
    VerifyKarmaLimits verifyKarmaLimits = new VerifyKarmaLimits();
    SetTier setTier = new SetTier();
    String message = null;

    public EditKarmaCommand() {
    }

    /**
     * The value is now the new karma of the target player.
     * @param commandSender
     * @param args
     */
    public void karmaSet(CommandSender commandSender, String[] args)
    {
        Player target = Bukkit.getServer().getPlayer(args[1]);
        int value = Integer.parseInt(args[2]);
        if (target != null) {
            try {
                File file = new File(this.karma.getDataFolder(), "playerdata/" + target.getUniqueId() + ".yml");
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

                configuration.set("karma", value);
                configuration.save(file);
                int newValue = verifyKarmaLimits.checkKarmaLimit(target);
                String tier = setTier.checkTier(target);
                message = "[Karma] " + target.getName() + "'s Karma is now " + newValue + " and his Tier is " + tier + ".";
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            message = "[Karma] The player \"" + args[1] + "\" is not connected.";
            message = ChatColor.translateAlternateColorCodes('&', message);
            commandSender.sendMessage(message);
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
        Player target = Bukkit.getServer().getPlayer(args[1]);
        int value = Integer.parseInt(args[2]);
        if (target != null) {
            try {
                File file = new File(this.karma.getDataFolder(), "playerdata/" + target.getUniqueId() + ".yml");
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                int targetKarma = configuration.getInt("karma");
                int targetNewKarma = targetKarma + value;

                configuration.set("karma", targetNewKarma);
                configuration.save(file);
                int newValue = verifyKarmaLimits.checkKarmaLimit(target);
                String tier = setTier.checkTier(target);

                message = "[Karma] " + target.getName() + "'s Karma is now " + newValue + " and his Tier is " + tier + ".";
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            verifyKarmaLimits.checkKarmaLimit(target);
        }
        else {
            message = "[Karma] The player \"" + args[1] + "\" is not connected.";
            message = ChatColor.translateAlternateColorCodes('&', message);
            commandSender.sendMessage(message);
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
        Player target = Bukkit.getServer().getPlayer(args[1]);
        int value = Integer.parseInt(args[2]);
        if (target != null) {
            try {
                File file = new File(this.karma.getDataFolder(), "playerdata/" + target.getUniqueId() + ".yml");
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                int targetKarma = configuration.getInt("karma");
                int targetNewKarma = targetKarma - value;

                configuration.set("karma", targetNewKarma);
                configuration.save(file);
                int newValue = verifyKarmaLimits.checkKarmaLimit(target);
                String tier = setTier.checkTier(target);

                message = "[Karma] " + target.getName() + "'s Karma is now " + newValue + " and his Tier is " + tier + ".";
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            message = "[Karma] The player \"" + args[1] + "\" is not connected.";
            message = ChatColor.translateAlternateColorCodes('&', message);
            commandSender.sendMessage(message);
        }
    }

    /**
     * Set the karma of target player as default, specified in config.yml
     * @param commandSender
     * @param args
     */
    public void karmaReset(CommandSender commandSender, String[] args)
    {
        Player target = Bukkit.getServer().getPlayer(args[1]);
        if (target != null) {
            try {
                File file = new File(this.karma.getDataFolder(), "playerdata/" + target.getUniqueId() + ".yml");
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

                int resKarma = this.karma.getConfig().getInt("karma.default-karma");
                configuration.set("karma", resKarma);
                configuration.save(file);
                int newValue = verifyKarmaLimits.checkKarmaLimit(target);
                String tier = setTier.checkTier(target);

                message = "[Karma] " + target.getName() + "'s Karma is now " + newValue + " and his Tier is " + tier + ".";
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            message = "[Karma] The player \"" + args[1] + "\" is not connected.";
            message = ChatColor.translateAlternateColorCodes('&', message);
            commandSender.sendMessage(message);
        }
    }
}