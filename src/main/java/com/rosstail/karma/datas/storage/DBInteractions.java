package com.rosstail.karma.datas.storage;

import com.rosstail.karma.Karma;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.customevents.PlayerWantedPeriodRefreshEvent;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

import static java.sql.DriverManager.getConnection;

public class DBInteractions {
    private final Karma plugin;
    private static DBInteractions dbInteractions;
    private Connection connection;

    public String host, database, username, password;
    public short port;

    private final String playerDataString;
    private final String updatePlayerDataString;
    private final String initPlayerDBString;

    public DBInteractions(Karma plugin) {
        this.plugin = plugin;

        host = ConfigData.getConfigData().storageHost;
        database = ConfigData.getConfigData().storageHost;
        username = ConfigData.getConfigData().storageHost;
        password = ConfigData.getConfigData().storageHost;
        port = ConfigData.getConfigData().storagePort;

        String pluginName = plugin.getName().toLowerCase();
        this.playerDataString = "SELECT * FROM " + pluginName + " WHERE uuid = ?";
        this.updatePlayerDataString = "UPDATE " + pluginName + " SET karma = ?, previous_karma = ?, tier = ?, previous_tier = ?," +
                " wanted_time = ?, last_update = ? WHERE uuid = ?;";
        this.initPlayerDBString = "INSERT INTO " + pluginName + " (uuid, karma, previous_karma, tier, previous_tier, last_update)"
                + "VALUES (?, ?, ?, ?, ?, ?);";
    }

    public enum reasons {
        CONNECT,
        DISCONNECT,
        COMMAND,
        SERVER_CLOSE
    }

    public boolean getPlayerData(Player player) {
        boolean reached = false;
        String UUID = player.getUniqueId().toString();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
        try {
            PreparedStatement statement = connection.prepareStatement(playerDataString);
            statement.setString(1, UUID);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                playerData.setKarmaBetweenLimits(result.getDouble("karma"));
                playerData.setPreviousKarma(result.getDouble("previous_karma"));
                playerData.setTier(TierManager.getTierManager().getTiers().get(result.getString("tier")));
                playerData.setPreviousTier(TierManager.getTierManager().getTiers().get(result.getString("previous_tier")));
                playerData.setLastUpdate(result.getTimestamp("last_update").getTime());
                long wantedTime = result.getLong("wanted_time");
                playerData.setWantedTimeStamp(new Timestamp(
                        (ConfigData.getConfigData().wantedCountdownApplyOnDisconnect
                                ? result.getTimestamp("last_update").getTime()
                                : System.currentTimeMillis())
                                + wantedTime));
                reached = true;
            }
            result.close();
            statement.close();
        } catch (Exception e) {
            AdaptMessage.print(e.toString(), AdaptMessage.prints.ERROR);
        }

        if (ConfigData.getConfigData().wantedEnable && playerData.isWanted()) {
            PlayerWantedPeriodRefreshEvent playerWantedPeriodRefreshEvent = new PlayerWantedPeriodRefreshEvent(player, reasons.CONNECT, false);
            Bukkit.getPluginManager().callEvent(playerWantedPeriodRefreshEvent);
        }
        return reached;
    }

    public void initPlayerDB(Player player) {
        String UUID = String.valueOf(player.getUniqueId());
        double value = ConfigData.getConfigData().defaultKarma;

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, value, true, Cause.OTHER);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
        playerData.setPreviousKarma(playerData.getKarma());
        playerData.checkTier();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(initPlayerDBString);

                preparedStatement.setString(1, UUID);
                preparedStatement.setDouble(2, playerData.getKarma());
                preparedStatement.setDouble(3, playerData.getPreviousKarma());
                preparedStatement.setString(4, playerData.getTier().getName());
                preparedStatement.setString(5, playerData.getPreviousTier().getName());
                preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

                preparedStatement.execute();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updatePlayersDB(reasons reason, Map<Player, PlayerData> players) throws SQLException, ClassNotFoundException {
        if (reason.equals(reasons.SERVER_CLOSE)) {
            serverCloseUpdatePlayersDB(players, reason);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                players.forEach((player, playerData) -> {
                    updateOnlinePlayer(player, PlayerDataManager.getPlayerDataMap().get(player), reason);
                });
            });
        }
    }

    public void serverCloseUpdatePlayersDB(Map<Player, PlayerData> playerDataMap, reasons reason) throws SQLException, ClassNotFoundException{
        playerDataMap.forEach((player, playerData) -> {
            updateOnlinePlayer(player, PlayerDataManager.getPlayerDataMap().get(player), reason);
        });
    }

    public void updateOnlinePlayer(Player player, PlayerData playerData, reasons reason) {
        String UUID = player.getUniqueId().toString();
        try {
            PreparedStatement statement = connection.prepareStatement(updatePlayerDataString);
            statement.setDouble(1, playerData.getKarma());
            statement.setDouble(2, playerData.getPreviousKarma());
            statement.setString(3, playerData.getTier().getName());
            statement.setString(4, playerData.getPreviousTier().getName());
            statement.setLong(5, playerData.getWantedTimeLeft());
            statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            statement.setString(7, UUID);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (reason.equals(reasons.DISCONNECT)) {
            PlayerDataManager.getPlayerDataMap().remove(player);
        }
    }

    public static DBInteractions getInstance() {
        return dbInteractions;
    }
}
