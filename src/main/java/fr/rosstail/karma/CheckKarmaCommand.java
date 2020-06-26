package fr.rosstail.karma;

import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * This command able the commandSender to see what is the Karma and Karma Tier of a conected user
 */
public class CheckKarmaCommand {
    String message = null;

    //private final File langFile;
    //private final YamlConfiguration configLang;
    private final AdaptMessage adaptMessage;

    public CheckKarmaCommand(Karma plugin) {
        //this.langFile = new File(plugin.getDataFolder(), "lang/" + plugin.getConfig().getString("general.lang") + ".yml");
        //this.configLang = YamlConfiguration.loadConfiguration(langFile);
        this.adaptMessage = new AdaptMessage(plugin);
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
            message = LangManager.getMessage(LangMessage.CHECK_OTHER_KARMA); // INSTEAD OF configLang.getString("check-other-karma");
        } else {
            message = LangManager.getMessage(LangMessage.DISCONNECTED_PLAYER); // INSTEAD OF configLang.getString("disconnected-player");
        }
        adaptMessage.message(commandSender, player, 0, message);
    }

    /**
     * Used when a player use /karma without argument behind
     * @param sender
     */
    public void karmaSelf(CommandSender sender)
    {
        Player player = (Player) sender;

        message = LangManager.getMessage(LangMessage.CHECK_OWN_KARMA); // INSTEAD OF configLang.getString("check-own-karma");
        adaptMessage.message(player, player, 0, message);
    }
}
