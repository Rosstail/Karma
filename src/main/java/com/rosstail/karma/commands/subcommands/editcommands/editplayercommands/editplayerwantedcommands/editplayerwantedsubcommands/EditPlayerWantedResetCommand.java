package com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.editplayerwantedsubcommands;

import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.commands.subcommands.editcommands.editplayercommands.editplayerwantedcommands.EditPlayerWantedSubCommand;
import com.rosstail.karma.players.PlayerDataModel;
import com.rosstail.karma.storage.StorageManager;
import com.rosstail.karma.events.karmaevents.PlayerWantedChangeEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.List;

public class EditPlayerWantedResetCommand extends EditPlayerWantedSubCommand {

    public EditPlayerWantedResetCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_RESET_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getDescription() {
        return "Makes the player innocent and never guilty";
    }

    @Override
    public String getSyntax() {
        return "karma edit player <player> wanted reset (-d -o -g -s)";
    }


    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
    }

    @Override
    public void performOnline(CommandSender sender, PlayerDataModel model, String[] args, String[] arguments, Player player) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeWantedOnline(sender, model, args, arguments, player);
    }

    @Override
    public void performOffline(CommandSender sender, PlayerDataModel model, String[] arguments, String[] args) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        changeWantedOffline(sender, model, args, arguments);
    }

    private void changeWantedOnline(CommandSender sender, PlayerDataModel model, String[] args, String[] arguments, Player player) {
        boolean silent = CommandManager.doesCommandMatchParameter(arguments, "s", "silent");
        PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(player, model, new Timestamp(0), silent);
        Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
        
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        String message = adaptMessage.adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_RESET_RESULT), PlayerType.PLAYER.getText());
        sender.sendMessage(adaptMessage.adaptMessage(message));
    }

    private void changeWantedOffline(CommandSender sender, PlayerDataModel model, String[] args, String[] arguments) {
        model.setWantedTimeStamp(new Timestamp(0));
        StorageManager.getManager().asyncUploadPlayerModel(model);

        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        String message = adaptMessage.adaptMessageToModel(model, LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_WANTED_RESET_RESULT), PlayerType.PLAYER.getText());
        sender.sendMessage(adaptMessage.adaptMessage(message));
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return null;
    }
}
