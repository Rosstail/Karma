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

    private DBInteractions(Karma plugin) {
        this.plugin = plugin;
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
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database,
                    this.username, this.password);
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
                playerData.setWantedTimeStamp(result.getTimestamp("Wanted_Time"));
                reached = true;
            }
            result.close();
            statement.close();
        } catch (Exception e) {
            AdaptMessage.print(e.toString(), AdaptMessage.prints.ERROR);
        }
        playerData.setWanted(Fights.isPlayerWanted(playerData.getWantedTimeStamp().getTime()));
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
                " Wanted_Time DATETIME NOT NULL DEFAULT '1970-01-01 01:00:00');";
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
                        "INSERT INTO " + plugin.getName() + " (UUID, Karma, Previous_Karma, Tier, Previous_Tier, Wanted_Time)\n"
                                + "VALUES (?, ?, ?, ?, ?, ?);");

                preparedStatement.setString(1, UUID);
                preparedStatement.setDouble(2, playerData.getKarma());
                preparedStatement.setDouble(3, playerData.getPreviousKarma());
                preparedStatement.setString(4, playerData.getTier().getName());
                preparedStatement.setString(5, playerData.getPreviousTier().getName());
                preparedStatement.setTimestamp(6, playerData.getWantedTimeStamp());

                preparedStatement.execute();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updatePlayersDB(boolean isSync, Map<Player, PlayerData> map) {
        if (!isSync) {
            for (Player player : map.keySet()) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        updateData(player, PlayerData.getPlayerList().get(player));
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });
            }
        } else {
            System.out.println("sync");
            for (Player player : map.keySet()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        updateData(player, PlayerData.getPlayerList().get(player));
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });
            }
        }
    }

    public void updateData(Player player, PlayerData playerData) throws SQLException {
        String UUID = player.getUniqueId().toString();
        String query = "UPDATE " + plugin.getName() + " SET Karma = ?, Previous_Karma = ?, Tier = ?, Previous_Tier = ?, Wanted_Time = ? WHERE UUID = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setDouble(1, playerData.getKarma());
        preparedStatement.setDouble(2, playerData.getPreviousKarma());
        preparedStatement.setString(3, playerData.getTier().getName());
        preparedStatement.setString(4, playerData.getPreviousTier().getName());
        preparedStatement.setTimestamp(5, playerData.getWantedTimeStamp());
        preparedStatement.setString(6, UUID);

        preparedStatement.executeUpdate();
        preparedStatement.close();
        System.out.println("done " + player);
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
}
