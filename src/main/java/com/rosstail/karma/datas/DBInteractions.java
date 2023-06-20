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
    private Connection connection;

    public String host, database, username, password;
    public int port;

    private final String playerDataString;
    private final String scoreboardString;
    private final String updatePlayerDataString;
    private final String createTableString;
    private final String initPlayerDBString;

    private DBInteractions(Karma plugin) {
        this.plugin = plugin;
        String pluginName = plugin.getName();
        this.playerDataString = "SELECT * FROM " + pluginName + " WHERE UUID = ?";
        this.scoreboardString =  "SELECT * FROM " + pluginName + " ORDER BY " + pluginName +  ".Karma ? LIMIT ?";
        this.updatePlayerDataString = "UPDATE " + pluginName + " SET Karma = ?, Previous_Karma = ?, Tier = ?, Previous_Tier = ?," +
                " Wanted_Time = ?, Last_Update = ? WHERE UUID = ?;";
        this.createTableString = "CREATE TABLE IF NOT EXISTS " + pluginName + " ( UUID varchar(40) PRIMARY KEY UNIQUE NOT NULL," +
                " Karma double," +
                " Previous_Karma double," +
                " Tier varchar(50)," +
                " Previous_Tier varchar(50)," +
                " Wanted_Time bigint UNSIGNED DEFAULT 0," +
                " Last_Update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        this.initPlayerDBString = "INSERT INTO " + pluginName + " (UUID, Karma, Previous_Karma, Tier, Previous_Tier, Last_Update)"
                + "VALUES (?, ?, ?, ?, ?, ?);";
        try {
            this.openConnection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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
            if (checkConnection(connection) || openConnection()) {
                setTableToDataBase(connection);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean checkConnection(Connection connection) {
        try {
            return (connection.isValid(5));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean openConnection() throws SQLException, ClassNotFoundException {
        synchronized (this) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection;
            for (int index = 0; index < 3; index++) {
                connection = getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
                if (checkConnection(connection)) {
                    this.connection = connection;
                    return true;
                }
            }

        }
        System.out.println("Could not open connection to database after 3 attempted tries.");
        return false;
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


    private void setTableToDataBase(Connection connection) throws SQLException {
        String sql = createTableString;
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
            players.forEach((player, playerData) -> {
                updateData(player, PlayerDataManager.getPlayerDataMap().get(player), reason);
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                players.forEach((player, playerData) -> {
                    updateData(player, PlayerDataManager.getPlayerDataMap().get(player), reason);
                });
            });
        }
    }

    public void updateData(Player player, PlayerData playerData, reasons reason) {
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

    public List<AbstractMap.SimpleEntry<String, Double>> getPlayersKarmaTop(String order, Integer limit) {
        List<AbstractMap.SimpleEntry<String, Double>> playersTopList = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(scoreboardString);
            statement.setString(1, order);
            statement.setInt(2, limit);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String uuid = result.getString("UUID");
                Double karma = result.getDouble("Karma");
                playersTopList.add(new AbstractMap.SimpleEntry<>(uuid, karma));
            }

            result.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playersTopList;
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
