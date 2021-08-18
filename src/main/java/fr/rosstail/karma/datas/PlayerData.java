package fr.rosstail.karma.datas;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import fr.rosstail.karma.lang.PlayerType;
import fr.rosstail.karma.tiers.Tier;
import fr.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
//import org.graalvm.compiler.debug.TimeSource;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
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
    private Timestamp lastAttack = new Timestamp(0L);
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
        if (tier != null) {
            return tier;
        }
        return TierManager.getNoTier();
    }

    public Timestamp getLastAttack() {
        return lastAttack; //milliseconds are important only for calculations
    }

    public double getPreviousKarma() {
        return previousKarma;
    }

    public Tier getPreviousTier() {
        if (previousTier != null) {
            return previousTier;
        }
        return TierManager.getNoTier();
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
                    lastAttack = result.getTimestamp("Last_Attack");
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
                    lastAttack = result.getTimestamp("Last_Attack");
                }
                statement.close();
            } else {
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                karma = playerConfig.getDouble("karma");
                previousKarma = playerConfig.getDouble("previous-karma");
                tier = TierManager.getTierManager().getTiers().get(playerConfig.getString("tier"));
                previousTier = TierManager.getTierManager().getTiers().get(playerConfig.getString("previous-tier"));
                lastAttack = new Timestamp(playerConfig.getLong("last-attack"));
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
        } catch (SQLException throwable) {
            throwable.printStackTrace();
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
                            "INSERT INTO " + plugin.getName() + " (UUID, Karma, Previous_Karma, Tier, Previous_Tier, Last_Attack)\n"
                                    + "VALUES (?, ?, ?, ?, ?, ?);");

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
                    preparedStatement.setTimestamp(6, lastAttack);

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
        double min = ConfigData.getConfigData().getMinKarma();
        double max = ConfigData.getConfigData().getMaxKarma();
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
                playerConfig.set("last-attack", lastAttack.getTime());

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

                    preparedStatement.setTimestamp(5, lastAttack);
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
                setPreviousTier(this.tier);
                this.tier = tier;

                changePlayerTierMessage();
                tierCommandsLauncher(player, tier.getJoinCommands());
                if (previousTier != null) {
                    if (karma > previousKarma) {
                        tierCommandsLauncher(player, tier.getJoinOnUpCommands());
                    } else {
                        tierCommandsLauncher(player, tier.getJoinOnDownCommands());
                    }
                }
                break;
            }
        }
    }

    public void setPreviousTier(Tier previousTier) {
        this.previousTier = previousTier;
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
        lastAttack = new Timestamp(System.currentTimeMillis());
    }

    private void changePlayerTierMessage() {
        String message = LangManager.getMessage(LangMessage.TIER_CHANGE);
        if (message != null) {
            player.sendMessage(adaptMessage.message(player, message, PlayerType.player.getId()));
        }
    }

    public static void tierCommandsLauncher(Player player, List<String> commands) {
        if (commands != null) {
            commands.forEach(s -> {
                placeCommands(player, s);
            });
        }
    }

    public static void tierCommandsLauncher(Player attacker, Player victim, List<String> commands) {
        if (commands != null) {
            commands.forEach(s -> {
                placeCommands(attacker, victim, s);
            });
        }
    }

    private static void placeCommands(Player player, String command) {
        command = adaptMessage.message(player, command, PlayerType.player.getId());
        command = ChatColor.translateAlternateColorCodes('&', command);

        CommandSender senderOrTarget = Bukkit.getConsoleSender();

        String regex = PlayerType.player.getId();
        if (command.startsWith(regex)) {
            command = command.replaceFirst(regex, "").trim();
            senderOrTarget = player;
        }
        if (command.startsWith("message")) {
            command = command.replaceFirst("message", "").trim();
            senderOrTarget.sendMessage(command);
        } else {
            Bukkit.dispatchCommand(senderOrTarget, command);
        }
    }

    private static void placeCommands(Player attacker, Player victim, String command) {
        command = adaptMessage.message(attacker, command, PlayerType.attacker.getId());
        command = adaptMessage.message(victim, command, PlayerType.victim.getId());
        command = ChatColor.translateAlternateColorCodes('&', command);

        CommandSender senderOrTarget = Bukkit.getConsoleSender();
        if (command.startsWith(PlayerType.victim.getId())) {
            command = command.replaceFirst(PlayerType.victim.getId(), "").trim();
            senderOrTarget = victim;
        } else if (command.startsWith(PlayerType.attacker.getId())) {
            command = command.replaceFirst(PlayerType.attacker.getId(), "").trim();
            senderOrTarget = attacker;
        }

        if (command.startsWith("message")) {
            command = command.replaceFirst("message", "").trim();
            senderOrTarget.sendMessage(command);
        } else {
            Bukkit.dispatchCommand(senderOrTarget, command);
        }
    }

    public static Map<Player, PlayerData> getPlayerList() {
        return playerList;
    }
}
