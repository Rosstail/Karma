package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

/**
 * Gonna be used to optimize the research of values
 */
public class GetSet {
    private Karma karma = Karma.get();

    private File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
    private YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);

    /**
     * Returns the amount of Karma of the player
     * @param player
     * @return
     */
    public int getPlayerKarma(Player player) {
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {
                Statement statement = karma.connection.createStatement();
                String UUID = String.valueOf(player.getUniqueId());
                ResultSet result = statement.executeQuery("SELECT Karma FROM Karma WHERE UUID = '" + UUID + "';");
                int karma = 0;
                while (result.next()) {
                    karma = result.getInt("Karma");
                }
                statement.close();
                return karma;
            }
            else {
                File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                return playerConfig.getInt("karma");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the Tier identifier of the player
     * @param player
     * @return
     */
    public String getPlayerTier(Player player) {
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {
                Statement statement = karma.connection.createStatement();
                String UUID = String.valueOf(player.getUniqueId());
                ResultSet result = statement.executeQuery("SELECT Tier FROM Karma WHERE UUID = '" + UUID + "';");
                String tier = null;
                while (result.next()) {
                    tier = result.getString("Tier");
                }
                statement.close();
                return tier;
            } else {
                String UUID = String.valueOf(player.getUniqueId());
                File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + UUID + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                return playerConfig.getString("tier");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * return the displaying name of the tier.
     * @param player
     * @return
     */
    public String getPlayerDisplayTier(Player player) {
        return karma.getConfig().getString("tiers." + getPlayerTier(player) + ".tier-display-name");
    }

    /**
     * Get the karma limits of karma for specified tier in Config.yml
     * @param tier
     * @return
     */
    public int[] getTierLimits(String tier) {
        int tierMinimumKarma = karma.getConfig().getInt("tiers." + tier + ".tier-minimum-karma");
        int tierMaximumKarma = karma.getConfig().getInt("tiers." + tier + ".tier-maximum-karma");
        return new int[]{tierMinimumKarma, tierMaximumKarma};
    }



    /**
     * Initialize the file / Line of the player with UUID, name, karma and tier
     * @param player -> The player for the props are gonna be made
     */
    public void initPlayerData(Player player) {
        int value = karma.getConfig().getInt("karma.default-karma");
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {
                PreparedStatement preparedStatement = karma.connection.prepareStatement("INSERT INTO Karma (UUID, NickName, Karma, Tier)\n" +
                        "VALUES (?, ?, ?, ?);");

                preparedStatement.setString(1, String.valueOf(player.getUniqueId()));
                preparedStatement.setString(2, player.getName());
                preparedStatement.setInt(3, value);
                preparedStatement.setString(4, null);

                preparedStatement.execute();
                preparedStatement.close();

                setKarmaToLimit(player);
                setTierToPlayer(player);

            } else {
                File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                playerConfig.set("karma", value);
                try {
                    playerConfig.save(playerFile);
                    setTierToPlayer(player);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * /**
     * Update the new karma of the player if change is needed.
     * Uses local files or Database if connection is active
     * @param player -> the player
     * @param value -> The new karma amoutn of the player
     */
    public void setKarmaToPlayer(Player player, int value) {
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {

                String query = "UPDATE Karma SET Karma = ? WHERE UUID = ?;";
                PreparedStatement preparedStatement = karma.connection.prepareStatement(query);

                preparedStatement.setInt(1, value);
                preparedStatement.setString(2, player.getUniqueId().toString());

                preparedStatement.executeUpdate();
                preparedStatement.close();

            } else {
                File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                playerConfig.set("karma", value);
                try {
                    playerConfig.save(playerFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        setKarmaToLimit(player);
        setTierToPlayer(player);
    }

    /**
     * Set the new tier of the player if change is needed.
     * Uses local files or Database if connection is active
     * @param player
     */
    public void setTierToPlayer(Player player) {
        Set<String> path = karma.getConfig().getConfigurationSection("tiers").getKeys(false);
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {
                int[] tierLimits;
                String tier = getPlayerTier(player);
                for (String tierList : path) {
                    tierLimits = getTierLimits(tierList);
                    if (getPlayerKarma(player) >=  tierLimits[0] && getPlayerKarma(player) <= tierLimits[1] && !tierList.equals(tier)) {

                        String query = "UPDATE Karma SET Tier = ? WHERE UUID = ?;";
                        PreparedStatement preparedStatement = karma.connection.prepareStatement(query);

                        preparedStatement.setString(1, tierList);
                        preparedStatement.setString(2, player.getUniqueId().toString());

                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        changePlayerTierMessage(player);
                        break;
                    }
                }

            } else {
                File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

                int[] tierLimits;
                String tier = getPlayerTier(player);
                for (String tierList : path) {
                    tierLimits = getTierLimits(tierList);
                    if (getPlayerKarma(player) >=  tierLimits[0] && getPlayerKarma(player) <= tierLimits[1] && !tierList.equals(tier)) {
                        playerConfig.set("tier", tierList);

                        try {
                            playerConfig.save(playerFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        changePlayerTierMessage(player);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * SET the karma of the player to the maximum or the minimum
     * depending of if AND what limit has been reached
     * @param player The player
     */
    public void setKarmaToLimit(Player player) {
        int playerKarma = getPlayerKarma(player);
        int min = this.karma.getConfig().getInt("karma.minimum-karma");
        int max = this.karma.getConfig().getInt("karma.maximum-karma");

        if (playerKarma < min) {
            setKarmaToPlayer(player, min);
        }
        else if (playerKarma > max) {
            setKarmaToPlayer(player, max);
        }
    }

    /**
     * MUST BE MOVED ON ANOTHER CLASS
     * SEND A MESSAGE WITH THE SPECIFIED TIER TO THE PLAYER
     * @param player the player who gonna receive the message
     */
    public void changePlayerTierMessage(Player player) {
        String message = configurationLang.getString("tier-change");
        if (message != null) {
            message = message.replaceAll("<tier>", getPlayerDisplayTier(player));
            message = ChatColor.translateAlternateColorCodes('&', message);
            player.sendMessage(message);
        }
    }
}
