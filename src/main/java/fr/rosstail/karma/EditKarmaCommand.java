package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class EditKarmaCommand {
    private Karma karma = Karma.getInstance();
    ChangeKarma changeKarma = new ChangeKarma();

    public EditKarmaCommand() {
    }

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
                commandSender.sendMessage("[Karma] " + target.getName() + "'s Karma is now " + value + ".");
            } catch (IOException e) {
                e.printStackTrace();
            }
            changeKarma.checkKarmaLimit(target);
        }
        else
            commandSender.sendMessage("[Karma] The player \"" + args[1] + "\" doesn't exists.");
    }

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
                commandSender.sendMessage("[Karma] " + target.getName() + "'s Karma is now " + targetNewKarma + ".");
            } catch (IOException e) {
                e.printStackTrace();
            }
            changeKarma.checkKarmaLimit(target);
        }
        else
            commandSender.sendMessage("[Karma] The player \"" + args[1] + "\" doesn't exists.");
    }

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
                commandSender.sendMessage("[Karma] " + target.getName() + "'s Karma is now " + targetNewKarma + ".");
            } catch (IOException e) {
                e.printStackTrace();
            }
            changeKarma.checkKarmaLimit(target);
        }
        else
            commandSender.sendMessage("[Karma] The player \"" + args[1] + "\" doesn't exists.");
    }

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
                commandSender.sendMessage("[Karma] " + target.getName() + "'s Karma is now " + resKarma + ".");
            } catch (IOException e) {
                e.printStackTrace();
            }
            changeKarma.checkKarmaLimit(target);
        }
        else
            commandSender.sendMessage("[Karma] The player \"" + args[1] + "\" doesn't exists.");
    }
}
