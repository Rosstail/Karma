package com.rosstail.karma.commands.subcommands;

import com.rosstail.karma.Karma;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.SubCommand;
import com.rosstail.karma.players.PlayerDataManager;
import com.rosstail.karma.events.karmaevents.PlayerTierChangeEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ReloadCommand extends SubCommand {

    public ReloadCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_RELOAD_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads plugin config.";
    }

    @Override
    public String getSyntax() {
        return "karma reload";
    }

    @Override
    public String getPermission() {
        return "karma.command.reload";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        boolean silent = CommandManager.doesCommandMatchParameter(arguments, "s", "silent");
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        PlayerDataManager.saveAllPlayerModelToStorage();
        Karma.getInstance().loadCustomConfig();

        /*
            CHECK ALL PLAYER TIER
         */
        TierManager tierManager = TierManager.getTierManager();
        PlayerDataManager.getPlayerModelMap().forEach((s, playerModel) -> {
            Player player = Bukkit.getPlayer(playerModel.getUsername());
            Tier currentKarmaTier = tierManager.getTierByKarmaAmount(playerModel.getKarma());
            Tier modelTier = tierManager.getTierByName(playerModel.getTierName());
            if (!currentKarmaTier.equals(modelTier)) {
                PlayerTierChangeEvent tierChangeEvent = new PlayerTierChangeEvent(player, playerModel, currentKarmaTier.getName(), silent);
                Bukkit.getPluginManager().callEvent(tierChangeEvent);
            }
        });
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_RELOAD_RESULT)));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
