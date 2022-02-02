package com.rosstail.karma.datas;

import com.rosstail.karma.Karma;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.customevents.PlayerWantedPeriodRefreshEvent;
import com.rosstail.karma.events.Fights;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class DBInteractions {

    private static DBInteractions dbInteractions;
    private final Karma plugin;

    public Connection connection;
    public String host, database, username, password;
    public int port;
    public Timer connChecker = new Timer();


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

    public void prepareConnection() {
        YamlConfiguration config = plugin.getCustomConfig();
        host = config.getString("mysql.host");
        database = config.getString("mysql.database");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");
        port = config.getInt("mysql.port");
        try {
            openConnection();
            setTableToDataBase();
            connChecker.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        ping();
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }, 3600000, 3600000);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database,
                    this.username, this.password);
        }
    }

    private void ping() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            AdaptMessage.print("[Karma] Hourly ping to maintain database connexion.", AdaptMessage.prints.OUT);
            PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM " + plugin.getName() + ";");
            statement.executeQuery();
        }
    }

    public boolean getPlayerData(Player player) {
        boolean reached = false;
        String UUID = player.getUniqueId().toString();
        PlayerData playerData = PlayerData.gets(player);
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + plugin.getName() + " WHERE UUID = '" + UUID + "';");
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                playerData.setKarma(result.getDouble("Karma"));
                playerData.setPreviousKarma(result.getDouble("Previous_Karma"));
                playerData.setTier(TierManager.getTierManager().getTiers().get(result.getString("Tier")));
                playerData.setPreviousTier(TierManager.getTierManager().getTiers().get(result.getString("Previous_Tier")));
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
        }
        if (playerData.isWanted()) {
            PlayerWantedPeriodRefreshEvent playerWantedPeriodRefreshEvent = new PlayerWantedPeriodRefreshEvent(player, "Player connect", false);
            Bukkit.getPluginManager().callEvent(playerWantedPeriodRefreshEvent);
        }
        return reached;
    }


    private void setTableToDataBase() {
        String sql = "CREATE TABLE IF NOT EXISTS " + plugin.getName() + " ( UUID varchar(40) PRIMARY KEY UNIQUE NOT NULL,\n" +
                " Karma double,\n" +
                " Previous_Karma double,\n" +
                " Tier varchar(50),\n" +
                " Previous_Tier varchar(50),\n" +
                " Wanted_Time bigint UNSIGNED DEFAULT 0,\n" +
                " Last_Update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        try {
            if (connection != null && !connection.isClosed()) {
                Statement statement = connection.createStatement();
                statement.execute(sql);
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initPlayerDB(Player player) {
        String UUID = String.valueOf(player.getUniqueId());
        double value = ConfigData.getConfigData().defaultKarma;

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, value, true, Cause.OTHER);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        PlayerData playerData = PlayerData.gets(player);
        playerData.setPreviousKarma(playerData.getKarma());
        playerData.checkTier();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updatePlayersDB(reasons reason, Map<Player, PlayerData> players) {
        if (!reason.equals(reasons.SERVER_CLOSE)) {
            players.forEach((player, playerData) -> Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    updateData(player, PlayerData.getPlayerList().get(player), reason);
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }));
        } else {
            players.forEach((player, playerData) -> {
                try {
                    updateData(player, PlayerData.getPlayerList().get(player), reason);
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    public void updateData(Player player, PlayerData playerData, reasons reason) throws SQLException {
        String UUID = player.getUniqueId().toString();
        String query = "UPDATE " + plugin.getName() + " SET Karma = ?, Previous_Karma = ?, Tier = ?, Previous_Tier = ?," +
                " Wanted_Time = ?, Last_Update = ? WHERE UUID = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setDouble(1, playerData.getKarma());
        preparedStatement.setDouble(2, playerData.getPreviousKarma());
        preparedStatement.setString(3, playerData.getTier().getName());
        preparedStatement.setString(4, playerData.getPreviousTier().getName());
        preparedStatement.setLong(5, playerData.getWantedTime());
        preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
        preparedStatement.setString(7, UUID);
        preparedStatement.executeUpdate();
        preparedStatement.close();

        if (reason.equals(reasons.DISCONNECT)) {
            PlayerData.getPlayerList().remove(player);
        }
    }

    public void closeConnexion() {
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

    public void cancelTimer() {
        connChecker.cancel();
    }
}
