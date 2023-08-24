package com.rosstail.karma.apis;

import com.rosstail.karma.Karma;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.players.PlayerModel;
import com.rosstail.karma.players.TopFlopScoreManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin("Karma") != null;
    }

    /**
     * We can optionally override this method if we need to initialize variables
     * within this class if we need to or even if we have to do other checks to
     * ensure the hook is properly set up.
     *
     * @return true or false depending on if it can register.
     */

    public PAPIExpansion(Karma plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean register() {

        // Make sure "SomePlugin" is on the server
        if (!canRegister()) {
            return false;
        }
        /*
         * "SomePlugin" does not have static methods to access its api so we must
         * create a variable to obtain access to it.
         */

        // if for some reason we can not get our variable, we should return false.
        if (plugin == null) {
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
    public @NotNull String getAuthor() {
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
    public @NotNull String getIdentifier() {
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
    public String getRequiredPlugin() {
        return plugin.getName();
    }

    /**
     * This is the version of this expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * @return The version as a String.
     */
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param player     A {@link Player Player}.
     * @param identifier A String containing the identifier/value.
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        if (identifier.startsWith("scoreboard_")) {

            TopFlopScoreManager topFlopScoreManager = TopFlopScoreManager.getTopFlopScoreManager();
            List<PlayerModel> topFlopList;

            if (identifier.contains("_top_") || identifier.contains("_bottom_")) {
                if (identifier.contains("_top_")) {
                    topFlopList = topFlopScoreManager.getPlayerTopScoreList();
                } else {
                    topFlopList = topFlopScoreManager.getPlayerFlopScoreList();
                }
                String indexStr = identifier.replaceAll("[^0-9]*", "");
                int index = Math.max(1, Integer.parseInt(indexStr));
                PlayerModel model = topFlopList.get(index - 1);

                if (model == null) {
                    return "-";
                }

                if (identifier.contains("_status")) {
                    boolean isWanted;
                    if (ConfigData.getConfigData().wanted.wantedCountdownApplyOnDisconnect) {
                        isWanted = (model.getWantedTimeStamp().getTime() - System.currentTimeMillis()) > 0L;
                    } else {
                        isWanted = model.getWantedTimeStamp().getTime() > 0L;
                    }

                    if (identifier.contains("_status_display")) {
                        if (identifier.contains("_status_display_short")) {
                            return LangManager.getMessage(isWanted ? LangMessage.WANTED_STATUS_WANTED_SHORT : LangMessage.WANTED_STATUS_INNOCENT_SHORT);
                        }
                        return LangManager.getMessage(isWanted ? LangMessage.WANTED_STATUS_WANTED : LangMessage.WANTED_STATUS_INNOCENT);
                    }
                }
                if (identifier.contains("_karma_")) {
                    if (identifier.contains("_tier")) {
                        Tier tier = TierManager.getTierManager().getTierByKarmaAmount(model.getKarma());
                        if (identifier.contains("_tier_display")) {
                            if (identifier.contains("_tier_display_short")) {
                                return tier != null ? tier.getShortDisplay() : TierManager.getNoTier().getShortDisplay();
                            }
                            return tier != null ? tier.getDisplay() : TierManager.getNoTier().getDisplay();
                        }
                        return tier != null ? tier.getName() : TierManager.getNoTier().getName();
                    }
                    if (identifier.contains("_int_")) {
                        int intValue = (int) model.getKarma();
                        return String.valueOf(intValue);
                    }
                    float value = model.getKarma();
                    return adaptMessage.decimalFormat(value, '.');
                }
                if (identifier.contains("_tier_")) {
                    Tier tier = TierManager.getTierManager().getTierByName(model.getTierName());
                    if (identifier.contains("_tier_display")) {
                        if (identifier.contains("_tier_display_short")) {
                            return tier != null ? tier.getShortDisplay() : TierManager.getNoTier().getShortDisplay();
                        }
                        return tier != null ? tier.getDisplay() : TierManager.getNoTier().getDisplay();
                    }
                    return tier != null ? tier.getName() : TierManager.getNoTier().getName();
                } else if (identifier.contains("_name_")) {
                    return model.getUsername();
                }
            }
        }

        if (player != null) {
            return adaptMessage.adaptMessage(adaptMessage.adaptPlayerMessage(player, "[" + identifier + "]", PlayerType.PLAYER.getText()));
        }

        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return adaptMessage.adaptMessage("[" + identifier + "]");
    }
}