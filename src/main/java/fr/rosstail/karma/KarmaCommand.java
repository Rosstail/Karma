package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class KarmaCommand implements CommandExecutor {
    private Karma karma = Karma.getInstance();

    public KarmaCommand() {
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length >= 1)
            karmaOther(commandSender, args);
        else if (commandSender instanceof Player)
            karmaSelf(commandSender);
        else {
            commandSender.sendMessage("[Karma] \"/karma\" must be send by a player.");
            commandSender.sendMessage("[Karma] As non-player, you can select a player with \"/karma <player>\".");
        }
        return true;
    }

    public void karmaOther(CommandSender commandSender, String[] args)
    {
        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (target != null) {
            File file = new File(this.karma.getDataFolder(), "playerdata/" + target.getUniqueId() + ".yml");
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            int targetKarma = configuration.getInt("karma");
            commandSender.sendMessage("[Karma] " + target.getName() + "'s Karma is " + targetKarma + ".");
        }
        else
            commandSender.sendMessage("[Karma] The player \"" + args[0] + "\" doesn't exists.");
    }

    public void karmaSelf(CommandSender commandSender)
    {
        Player player = (Player) commandSender;
        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        int playerKarma = configuration.getInt("karma");
        player.sendMessage("[Karma] Your own Karma is " + playerKarma + ".");
    }
}
