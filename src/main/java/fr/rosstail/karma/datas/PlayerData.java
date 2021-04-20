package fr.rosstail.karma.datas;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import fr.rosstail.karma.tiers.Tier;
import fr.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Gonna be used to optimize the research of values
 */
public class PlayerData {
    private final Karma plugin;
    private static final ConfigData configData = ConfigData.getConfigData();
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

    private static final Map<Player, PlayerData> playerList = new HashMap<Player, PlayerData>();
    private final File playerFile;
    private final Player player;
    private double karma;
    private double previousKarma;
    private Tier tier;
    private Tier previousTier;
    private long lastAttack;
    private Timer updateDataTimer;
    private int overTimerScheduler;

    private PlayerData(Karma plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        playerFile = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        loadPlayerData();
        previousKarma = karma;
        previousTier = tier;
    }

    public static PlayerData gets(Player player, Karma plugin) {
        if(!playerList.containsKey(player)){ // If player doesn't have instance
            playerList.put(player, new PlayerData(plugin, player));
        }
        return playerList.get(player);
    }

    public Timer getUpdateDataTimer() {
        return updateDataTimer;
    }

    public double getKarma() {
        return karma;
    }

    public Tier getTier() {
        return tier;
    }

    public long getLastAttack() {
        return lastAttack; //milliseconds are important only for calculations
    }

    public double getPreviousKarma() {
        return previousKarma;
    }

    public Tier getPreviousTier() {
        return previousTier;
    }

    public int getOverTimerChange() {
        return overTimerScheduler;
    }

    private boolean ifPlayerExistsInDTB() {
        String UUID = String.valueOf(player.getUniqueId());
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                PreparedStatement statement = plugin.connection
                    .prepareStatement("SELECT * FROM " + plugin.getName() + " WHERE UUID = '" + UUID + "';");
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    karma = result.getDouble("Karma");
                    previousKarma = result.getDouble("Previous_Karma");

                    String tierLabel = result.getString("Tier");
                    String previousTierLabel = result.getString("Previous_Tier");
                    if (TierManager.getTierManager().getTiers().containsKey(tierLabel)) {
                        tier = TierManager.getTierManager().getTiers().get(tierLabel);
                    }
                    if (TierManager.getTierManager().getTiers().containsKey(previousTierLabel)) {
                        previousTier = TierManager.getTierManager().getTiers().get(previousTierLabel);
                    }
                    lastAttack = result.getLong("Last_Attack");
                    return true;
                }
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Request the player Tier inside file or database
     *
     */
    public void loadPlayerData() {
        String UUID = String.valueOf(player.getUniqueId());
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                PreparedStatement statement = plugin.connection.prepareStatement("SELECT * FROM " + plugin.getName() + " WHERE UUID = '" + UUID + "';");
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    karma = result.getDouble("Karma");
                    previousKarma = result.getDouble("Previous_Karma");
                    tier = TierManager.getTierManager().getTiers().get(result.getString("Tier"));
                    previousTier = TierManager.getTierManager().getTiers().get(result.getString("Previous_Tier"));
                    lastAttack = result.getLong("Last_Attack");
                }
                statement.close();
            } else {
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                karma = playerConfig.getDouble("karma");
                previousKarma = playerConfig.getDouble("previous-karma");
                tier = TierManager.getTierManager().getTiers().get(playerConfig.getString("tier"));
                previousTier = TierManager.getTierManager().getTiers().get(playerConfig.getString("previous-tier"));
                lastAttack = playerConfig.getLong("last-attack");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the file / Line of the player with UUID, name, karma and tier
     *
     */
    public void initPlayerData() {
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                if (!ifPlayerExistsInDTB()) {
                    initPlayerDataDTB();
                }
            } else if (!playerFile.exists()){
                initPlayerDataLocale();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void initPlayerDataDTB() {
        String UUID = String.valueOf(player.getUniqueId());
        double value = configData.getDefaultKarma();
        double min = configData.getMinKarma();
        double max = configData.getMaxKarma();

        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        double finalValue = value;

        this.karma = value;
        this.previousKarma = value;
        setTier();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = plugin.connection.prepareStatement(
                            "INSERT INTO " + plugin.getName() + " (UUID, Karma, Previous_Karma, Tier, Previous_Tier)\n"
                                    + "VALUES (?, ?, ?, ?, ?);");

                    preparedStatement.setString(1, UUID);
                    preparedStatement.setDouble(2, finalValue);
                    preparedStatement.setDouble(3, finalValue);
                    if (tier != null) {
                        preparedStatement.setString(4, tier.getName());
                    } else {
                        preparedStatement.setString(4, null);
                    }
                    if (previousTier != null) {
                        preparedStatement.setString(5, previousTier.getName());
                    } else {
                        preparedStatement.setString(5, null);
                    }

                    preparedStatement.execute();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initPlayerDataLocale() {
        double value = configData.getDefaultKarma();
        double min = configData.getMinKarma();
        double max = configData.getMaxKarma();

        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        double finalValue = value;
        karma = finalValue;
        setTier();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                playerConfig.set("karma", finalValue);
                try {
                    playerConfig.save(playerFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * /**
     * Update the new karma of the player if change is needed.
     * Uses local files or Database if connection is active
     *
     * @param value  -> The new karma amount of the player
     */
    public void setKarma(double value) {
        double min = plugin.getConfig().getDouble("karma.minimum");
        double max = plugin.getConfig().getDouble("karma.maximum");
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        setPreviousKarma(this.karma);
        this.karma = value;
        setTier();
    }

    public void setPreviousKarma(double previousKarma) {
        this.previousKarma = previousKarma;
    }

    public void updateData() {
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                updateDB();
            } else {
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

                playerConfig.set("karma", karma);
                playerConfig.set("previous-karma", previousKarma);
                if (tier != null) {
                    playerConfig.set("tier", tier.getName());
                } else {
                    playerConfig.set("tier", null);
                }
                if (previousTier != null) {
                    playerConfig.set("previous-tier", previousTier.getName());
                } else {
                    playerConfig.set("previous-tier", null);
                }
                playerConfig.set("last-attack", lastAttack);

                playerConfig.save(playerFile);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDB() {
        String UUID = player.getUniqueId().toString();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    String query = "UPDATE " + plugin.getName() + " SET Karma = ?, Previous_Karma = ?, Tier = ?, Previous_Tier = ?, Last_Attack = ? WHERE UUID = ?;";
                    PreparedStatement preparedStatement = plugin.connection.prepareStatement(query);

                    preparedStatement.setDouble(1, karma);
                    preparedStatement.setDouble(2, previousKarma);
                    if (tier != null) {
                        preparedStatement.setString(3, tier.getName());
                    } else {
                        preparedStatement.setString(3, null);
                    }

                    if (previousTier != null) {
                        preparedStatement.setString(4, previousTier.getName());
                    } else {
                        preparedStatement.setString(4, null);
                    }
                    preparedStatement.setLong(5, lastAttack);
                    preparedStatement.setString(6, UUID);

                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Set the new tier of the player if change is needed.
     * Uses local files or Database if connection is active
     *
     */
    public void setTier() {
        for (Tier tier : TierManager.getTierManager().getTiers().values()) {
            if (karma >= tier.getMinKarma() && karma <= tier.getMaxKarma() && !tier.equals(getTier())) {
                previousTier = this.tier;
                this.tier = tier;

                changePlayerTierMessage();
                tierCommandsLauncher(tier.getJoinCommands());
                if (previousTier != null) {
                    if (karma > previousKarma) {
                        tierCommandsLauncher(tier.getJoinOnUpCommands());
                    } else {
                        tierCommandsLauncher(tier.getJoinOnDownCommands()); //maybe reverse ?
                    }
                }
                break;
            }
        }
    }

    public void setUpdateDataTimer(int delay) {
        this.updateDataTimer = new Timer();
        updateDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateData();
            }
        }, delay, delay);;
    }

    public void setOverTimerChange() {
        stopOverTimer();
        if (!ConfigData.getConfigData().isOvertimeActive()) {
            return;
        }
        this.overTimerScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                double playerKarma = getKarma();
                double karma = getKarma();
                double decreaseValue = ConfigData.getConfigData().getOvertimeDecreaseValue();
                double increaseValue = ConfigData.getConfigData().getOvertimeIncreaseValue();
                if (decreaseValue > 0) {
                    double decreaseLimit = ConfigData.getConfigData().getOvertimeDecreaseLimit();
                    if (playerKarma > decreaseLimit) {
                        karma = getKarma() - decreaseValue;
                        if (karma < decreaseLimit) {
                            karma = decreaseLimit;
                        }
                    }
                }
                if (increaseValue > 0) {
                    double increaseLimit = ConfigData.getConfigData().getOvertimeIncreaseLimit();
                    if (playerKarma < increaseLimit) {
                        karma = getKarma() + increaseValue;
                        if (karma > increaseLimit) {
                            karma = increaseLimit;
                        }
                    }
                }

                if (karma != getKarma()) {
                    setKarma(karma);
                }
            }
        }, ConfigData.getConfigData().getOvertimeFirstDelay(), ConfigData.getConfigData().getOvertimeNextDelay());
    }

    public void stopOverTimer() {
        Bukkit.getScheduler().cancelTask(overTimerScheduler);
    }

    /**
     * Set the timestamp of the player's attack moment if needed
     *
     */
    public void setLastAttack() {
        if (player.hasMetadata("NPC")) {
            return;
        }
        lastAttack = System.currentTimeMillis();
    }

    private void changePlayerTierMessage() {
        String message = LangManager.getMessage(LangMessage.TIER_CHANGE);
        if (message != null) {
            player.sendMessage(adaptMessage.message(player, 0, message));
        }
    }

    public void tierCommandsLauncher(List<String> commands) {
        commands.forEach(s -> {
            placeCommands(player, s);
        });
    }

    private void placeCommands(Player player, String command) {
        command = command.replaceAll("<PLAYER>", player.getName());
        command = command.replaceAll("<KARMA>", String.format("%." + configData.getDecNumber() + "f", karma));
        command = command.replaceAll("<TIER>", tier.getDisplay());
        command = ChatColor.translateAlternateColorCodes('&', command);

        if (command.startsWith("<MESSAGE>")) {
            command = command.replaceAll("<MESSAGE>", "").trim();
            player.sendMessage(adaptMessage.message(player, 0, command));
        } else if (command.startsWith("<@>")) {
            command = command.replaceAll("<@>", "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Bukkit.dispatchCommand(player, command);
        }
    }

    public static Map<Player, PlayerData> getPlayerList() {
        return playerList;
    }
}
