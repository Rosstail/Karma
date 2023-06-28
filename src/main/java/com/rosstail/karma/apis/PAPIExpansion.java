package com.rosstail.karma.apis;

import com.rosstail.karma.Karma;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.TopFlopScoreManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.List;

public class PAPIExpansion extends PlaceholderExpansion {
    // We get an instance of the plugin later.
    private final Karma plugin;

    /**
     * Since this expansion requires api access to the plugin "SomePlugin"
     * we must check if said plugin is on the server or not.
     *
     * @return true or false depending on if the required plugin is installed.
     */
    @Override
    public boolean canRegister(){
        return Bukkit.getPluginManager().getPlugin("Karma") != null;
    }

    /**
     * We can optionally override this method if we need to initialize variables
     * within this class if we need to or even if we have to do other checks to
     * ensure the hook is properly set up.
     *
     * @return true or false depending on if it can register.
     */

    public PAPIExpansion(Karma plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean register(){

        // Make sure "SomePlugin" is on the server
        if(!canRegister()){
            return false;
        }
        /*
         * "SomePlugin" does not have static methods to access its api so we must
         * create a variable to obtain access to it.
         */

        // if for some reason we can not get our variable, we should return false.
        if (plugin == null){
            return false;
        }

        /*
         * Since we override the register method, we need to call the super method to actually
         * register this hook
         */
        return super.register();
    }

    /**
     * The name of the person who created this expansion should go here.
     *
     * @return The name of the author as a String.
     */
    @Override
    public @NotNull String getAuthor(){
        return "Rosstail";
    }
    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public @NotNull String getIdentifier(){
        return plugin.getName().toLowerCase();
    }

    /**
     * if the expansion requires another plugin as a dependency, the
     * proper name of the dependency should go here.
     * <br>Set this to {@code null} if your placeholders do not require
     * another plugin to be installed on the server for them to work.
     * <br>
     * <br>This is extremely important to set your plugin here, since if
     * you don't do it, your expansion will throw errors.
     *
     * @return The name of our dependency.
     */
    @Override
    public String getRequiredPlugin(){
        return plugin.getName();
    }

    /**
     * This is the version of this expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * @return The version as a String.
     */
    @Override
    public @NotNull String getVersion(){
        return plugin.getDescription().getVersion();
    }



    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link Player Player}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        if (player != null) {
            PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
            // %karma_value% here
            if(identifier.startsWith("player_karma")){
                float karma = model.getKarma();
                if (identifier.contains("_abs")) {
                    karma = Math.abs(karma);
                }
                if (identifier.contains("_int")) {
                    return String.valueOf(((int) karma));
                }
                return AdaptMessage.getAdaptMessage().decimalFormat(karma, '.');
            }
            if(identifier.startsWith("player_previous_karma")){
                float karma = model.getPreviousKarma();
                if (identifier.contains("_abs")) {
                    karma = Math.abs(karma);
                }
                if (identifier.contains("_int")) {
                    return String.valueOf(((int) karma));
                }
                return AdaptMessage.getAdaptMessage().decimalFormat(karma, '.');
            }
            if(identifier.startsWith("player_diff")) {
                float karma = model.getKarma() - model.getPreviousKarma();
                if (identifier.contains("_abs")) {
                    karma = Math.abs(karma);
                }
                if (identifier.contains("_int")) {
                    return String.valueOf(((int) karma));
                }
                return AdaptMessage.getAdaptMessage().decimalFormat(karma, '.');
            }

            if(identifier.startsWith("player_tier")) {
                Tier tier = TierManager.getTierManager().getTierByName(model.getTierName());
                if (identifier.equals("player_tier")) {
                    return tier.getName();
                }
                if (identifier.equals("player_tier_display")) {
                    return tier.getDisplay();
                }
                if (identifier.equals("player_tier_short_display")) {
                    return tier.getShortDisplay();
                }
                if (identifier.equals("player_tier_minimum")) {
                    return AdaptMessage.getAdaptMessage().decimalFormat(tier.getMinKarma(), '.');
                }
                if (identifier.equals("player_tier_maximum")) {
                    return AdaptMessage.getAdaptMessage().decimalFormat(tier.getMaxKarma(), '.');
                }
            }

            if(identifier.startsWith("player_previous_tier")) {
                Tier tier = TierManager.getTierManager().getTierByName(model.getPreviousTierName());
                if (identifier.equals("player_previous_tier")) {
                    return tier.getName();
                }
                if (identifier.equals("player_previous_tier_display")) {
                    return tier.getDisplay();
                }
                if (identifier.equals("player_previous_tier_short_display")) {
                    return tier.getShortDisplay();
                }
                if (identifier.equals("player_previous_tier_minimum")) {
                    return AdaptMessage.getAdaptMessage().decimalFormat(tier.getMinKarma(), '.');
                }
                if (identifier.equals("player_previous_tier_maximum")) {
                    return AdaptMessage.getAdaptMessage().decimalFormat(tier.getMaxKarma(), '.');
                }
            }

            if (identifier.equals("player_wanted_time")) {
                return AdaptMessage.getAdaptMessage().decimalFormat(model.getWantedTimeStamp().getTime(), '.');
            }
            if (identifier.equals("player_wanted_time_display")) {
                long time = model.getWantedTimeStamp().getTime();
                if (time > 0f) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigData.getConfigData().getDateTimeFormat());
                    return simpleDateFormat.format(time);
                }
                return "N/A";
            }

            if (identifier.equals("player_wanted_time_delay")) {
                return AdaptMessage.getAdaptMessage().decimalFormat(model.getWantedTimeStamp().getTime() - System.currentTimeMillis(), '.');
            }
            if (identifier.equals("player_wanted_time_delay_display")) {
                long time = PlayerDataManager.getWantedTimeLeft(model);
                if (time > 0f) {
                    return AdaptMessage.getAdaptMessage().countDownFormat(time);
                }
                return "-";
            }
            if (identifier.equals("player_wanted_status")) {
                if (model.isWanted()) {
                    return AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.STATUS_WANTED), null);
                }
                return AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.STATUS_INNOCENT), null);
            }
            if (identifier.equals("player_wanted_status_short")) {
                if (model.isWanted()) {
                    return AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.STATUS_WANTED_SHORT), null);
                }
                return AdaptMessage.getAdaptMessage().adapt(null, LangManager.getMessage(LangMessage.STATUS_INNOCENT_SHORT), null);
            }
        }

        //%karma_scoreboard_top_name_X% / %karma_scoreboard_bottom_karma_X%
        if (identifier.startsWith("scoreboard_")) {
            TopFlopScoreManager topFlopScoreManager = TopFlopScoreManager.getTopFlopScoreManager();
            List<PlayerModel> topFlopList;

            if (identifier.contains("_top_") || identifier.contains("_bottom_")) {
                if (identifier.contains("_top_")) {
                    topFlopList = topFlopScoreManager.getPlayerTopScoreList();
                } else {
                    topFlopList = topFlopScoreManager.getPlayerFlopScoreList();
                }
                String indexStr = identifier.replaceAll("[^0-9]", "");
                int index = Math.max(1, Integer.parseInt(indexStr));
                PlayerModel model = topFlopList.get(index);
                try {
                    if (identifier.contains("_karma_")) {
                        if (model == null) {
                            return "-";
                        }
                        if (identifier.contains("_int_")) {
                            int intValue = (int) model.getKarma();
                            return String.valueOf(intValue);
                        }
                        float value = model.getKarma();
                        return AdaptMessage.getAdaptMessage().decimalFormat(value, '.');
                    } else if (identifier.contains("_name_")) {
                        if (model == null) {
                            return "Unknown";
                        }
                        return model.getUsername();
                    }
                } catch (IndexOutOfBoundsException e) {
                    if (identifier.contains("_karma_")) {
                        return "-";
                    }
                    if (identifier.contains("_name_")) {
                        return "Unknown";
                    }
                }
            }
        }

        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }
}