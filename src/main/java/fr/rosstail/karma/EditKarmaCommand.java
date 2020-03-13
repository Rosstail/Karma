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

            File file = new File(this.karma.getDataFolder(), "playerdata/" + target.getUniqueId() + ".yml");
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            configuration.set("karma", value);

            try {
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            value = verifyKarmaLimits.checkKarmaLimit(target);
            String tier = setTier.checkTier(target);
            message = karma.getConfig().getString("messages.set-karma");

            if (message != null) {
                message = message.replaceAll("<player>", target.getName());
                message = message.replaceAll("<newKarma>", Integer.toString(value));
                message = message.replaceAll("<tier>", tier);
                message = ChatColor.translateAlternateColorCodes('&', message);
                commandSender.sendMessage(message);
            }
        }
        else {
            disconnectedPlayer(commandSender, target);
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

                message = karma.getConfig().getString("messages.add-karma");

                if (message != null) {
                    message = message.replaceAll("<player>", target.getName());
                    message = message.replaceAll("<value>", Integer.toString(value));
                    message = message.replaceAll("<newKarma>", Integer.toString(newValue));
                    message = message.replaceAll("<tier>", tier);
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    commandSender.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            verifyKarmaLimits.checkKarmaLimit(target);
        }
        else {
            disconnectedPlayer(commandSender, target);
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

                message = karma.getConfig().getString("messages.remove-karma");

                if (message != null) {
                    message = message.replaceAll("<player>", target.getName());
                    message = message.replaceAll("<value>", Integer.toString(value));
                    message = message.replaceAll("<newKarma>", Integer.toString(newValue));
                    message = message.replaceAll("<tier>", tier);
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    commandSender.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            disconnectedPlayer(commandSender, target);
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

                message = karma.getConfig().getString("messages.reset-karma");

                if (message != null) {
                    message = message.replaceAll("<player>", target.getName());
                    message = message.replaceAll("<karma>", String.valueOf(resKarma));
                    message = message.replaceAll("<newKarma>", Integer.toString(newValue));
                    message = message.replaceAll("<tier>", tier);
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    commandSender.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            disconnectedPlayer(commandSender, target);
        }
    }

    private void disconnectedPlayer(CommandSender commandSender, Player target) {
        message = karma.getConfig().getString("messages.disconnected-player");
        message = message.replaceAll("<player>", target.getName());
        message = ChatColor.translateAlternateColorCodes('&', message);
        commandSender.sendMessage(message);
    }
}