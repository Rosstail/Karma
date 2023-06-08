package com.rosstail.karma.datas;

import com.rosstail.karma.Karma;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.customevents.PlayerWantedPeriodRefreshEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

import static java.sql.DriverManager.getConnection;

public class DBInteractions {

    private static DBInteractions dbInteractions;
    private final Karma plugin;

    public String host, database, username, password;
    public int port;

    private DBInteractions(Karma plugin) {
        this.plugin = plugin;
    }

    public enum reasons {
        CONNECT,
        TIMED,
        DISCONNECT,
        COMMAND,
        SERVER_CLOSE
    }

    public void prepareTable() {
        YamlConfiguration config = plugin.getCustomConfig();
        host = config.getString("mysql.host");
        database = config.getString("mysql.database");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");
        port = config.getInt("mysql.port");

        try {
            Connection connection = openConnection();
            setTableToDataBase(connection);
            closeConnexion(connection);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection openConnection() throws SQLException, ClassNotFoundException {
        Connection conn;
        synchronized (this) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            return conn;
        }
    }

    public boolean getPlayerData(Player player) {
        boolean reached = false;
        String UUID = player.getUniqueId().toString();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
        Connection connection = null;
        try {
            connection = openConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + plugin.getName() + " WHERE UUID = '" + UUID + "';");
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                playerData.setKarma(result.getDouble("Karma"));
                playerData.setPreviousKarma(result.getDouble("Previous_Karma"));
                playerData.setTier(TierManager.getTierManager().getTiers().get(result.getString("Tier")));
                playerData.setPreviousTier(TierManager.getTierManager().getTiers().get(result.getString("Previous_Tier")));
                playerData.setLastUpdate(result.getTimestamp("Last_Update").getTime());
                long wantedTime = result.getLong("Wanted_Time");
                playerData.setWantedTimeStamp(new Timestamp(
                        (ConfigData.getConfigData().wantedCountdownApplyOnDisconnect
                                ? result.getTimestamp("Last_Update").getTime()
                                : System.currentTimeMillis())
                                + wantedTime)
                );
                reached = true;
            }
            result.close();
            statement.close();
        } catch (Exception e) {
            AdaptMessage.print(e.toString(), AdaptMessage.prints.ERROR);
        } finally {
            closeConnexion(connection);
        }
        if (ConfigData.getConfigData().wantedEnable && playerData.isWanted()) {
            PlayerWantedPeriodRefreshEvent playerWantedPeriodRefreshEvent = new PlayerWantedPeriodRefreshEvent(player, "Player connect", false);
            Bukkit.getPluginManager().callEvent(playerWantedPeriodRefreshEvent);
        }
        return reached;
    }


    private void setTableToDataBase(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + plugin.getName() + " ( UUID varchar(40) PRIMARY KEY UNIQUE NOT NULL,\n" +
                " Karma double,\n" +
                " Previous_Karma double,\n" +
                " Tier varchar(50),\n" +
                " Previous_Tier varchar(50),\n" +
                " Wanted_Time bigint UNSIGNED DEFAULT 0,\n" +
                " Last_Update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        Statement statement = connection.createStatement();
        statement.execute(sql);
        statement.close();
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
            Connection connection = null;
            try {
                connection = openConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO " + plugin.getName() + " (UUID, Karma, Previous_Karma, Tier, Previous_Tier, Last_Update)\n"
                                + "VALUES (?, ?, ?, ?, ?, ?);");

                preparedStatement.setString(1, UUID);
                preparedStatement.setDouble(2, playerData.getKarma());
                preparedStatement.setDouble(3, playerData.getPreviousKarma());
                preparedStatement.setString(4, playerData.getTier().getName());
                preparedStatement.setString(5, playerData.getPreviousTier().getName());
                preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

                preparedStatement.execute();
                preparedStatement.close();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                closeConnexion(connection);
            }
        });
    }

    public void updatePlayersDB(reasons reason, Map<Player, PlayerData> players) throws SQLException, ClassNotFoundException {
        Connection connection = openConnection();
        if (reason.equals(reasons.SERVER_CLOSE)) {
            players.forEach((player, playerData) -> {
                updateData(player, PlayerDataManager.getPlayerDataMap().get(player), connection, reason);
            });
            closeConnexion(connection);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                players.forEach((player, playerData) -> {
                    updateData(player, PlayerDataManager.getPlayerDataMap().get(player), connection, reason);
                });
                closeConnexion(connection);
            });
        }
    }

    public void updateData(Player player, PlayerData playerData, Connection connection, reasons reason) {
        String UUID = player.getUniqueId().toString();
        String query = "UPDATE " + plugin.getName() + " SET Karma = ?, Previous_Karma = ?, Tier = ?, Previous_Tier = ?," +
                " Wanted_Time = ?, Last_Update = ? WHERE UUID = ?;";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDouble(1, playerData.getKarma());
            preparedStatement.setDouble(2, playerData.getPreviousKarma());
            preparedStatement.setString(3, playerData.getTier().getName());
            preparedStatement.setString(4, playerData.getPreviousTier().getName());
            preparedStatement.setLong(5, playerData.getWantedTimeLeft());
            preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setString(7, UUID);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (reason.equals(reasons.DISCONNECT)) {
            PlayerDataManager.getPlayerDataMap().remove(player);
        }
    }

    public void closeConnexion(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void initDBInteractions(Karma plugin) {
        dbInteractions = new DBInteractions(plugin);
    }

    public static DBInteractions getInstance() {
        return dbInteractions;
    }
}
