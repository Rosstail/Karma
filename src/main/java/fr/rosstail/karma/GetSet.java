package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Gonna be used to optimize the research of values
 */
public class GetSet {
    private Karma karma = Karma.get();

    private File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
    private YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);
    private int nbDec = karma.getConfig().getInt("general.decimal-number-to-show");

    public boolean ifPlayerExistsInDTB(Player player) {
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {
                Statement statement = karma.connection.createStatement();
                String UUID = String.valueOf(player.getUniqueId());
                ResultSet result = statement.executeQuery("SELECT UUID FROM Karma WHERE UUID = '" + UUID + "';");
                if (result.next()) {
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
     * Returns the amount of Karma of the player
     * @param player
     * @return
     */
    public double getPlayerKarma(Player player) {
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {
                Statement statement = karma.connection.createStatement();
                String UUID = String.valueOf(player.getUniqueId());
                ResultSet result = statement.executeQuery("SELECT Karma FROM Karma WHERE UUID = '" + UUID + "';");
                double karma = 0;
                while (result.next()) {
                    karma = result.getDouble("Karma");
                }
                statement.close();
                return karma;
            }
            else {
                File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                return playerConfig.getDouble("karma");
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
     * Returns the Tier identifier of the player
     * @param player
     * @return
     */
    public Long getPlayerLastAttack(Player player) {
        try {
            if (!player.hasMetadata("NPC")) {
                if (karma.connection != null && !karma.connection.isClosed()) {
                    Statement statement = karma.connection.createStatement();
                    String UUID = String.valueOf(player.getUniqueId());
                    ResultSet result = statement.executeQuery("SELECT Last_Attack FROM Karma WHERE UUID = '" + UUID + "';");
                    Long dateTime = 0L;
                    while (result.next()) {
                        dateTime = result.getLong("Last_Attack");
                    }
                    statement.close();
                    return dateTime;
                } else {
                    String UUID = String.valueOf(player.getUniqueId());
                    File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + UUID + ".yml");
                    YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                    return playerConfig.getLong("last-attack");
                }
            } else {
                return 0L;
                //return player.getMetadata("Last_Attack").get(0).asLong();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
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
    public double[] getTierLimits(String tier) {
        double tierMinimumKarma = karma.getConfig().getInt("tiers." + tier + ".tier-minimum-karma");
        double tierMaximumKarma = karma.getConfig().getInt("tiers." + tier + ".tier-maximum-karma");
        return new double[]{tierMinimumKarma, tierMaximumKarma};
    }

    public String[] getSystemTimeLimits(String time) {
        String minimumHourMin = karma.getConfig().getString("times.system-times." + time + ".starting-time");
        String maximumHourMin = karma.getConfig().getString("times.system-times." + time + ".ending-time");
        return new String[]{minimumHourMin, maximumHourMin};
    }

    public Long[] getWorldTimeLimits(String time) {
        String minimumHourMin = karma.getConfig().getString("times.worlds-times." + time + ".starting-time");
        String maximumHourMin = karma.getConfig().getString("times.worlds-times." + time + ".ending-time");

        assert minimumHourMin != null;
        assert maximumHourMin != null;
        String[] convMinHourMin = minimumHourMin.split(":", 2);
        String[] convMaxHourMin = maximumHourMin.split(":", 2);
        long minHour = (long)( 1000 * Integer.parseInt(convMinHourMin[0]) + 16.66 * Integer.parseInt(convMinHourMin[1])) + 18000L;
        long maxHour = (long)( 1000 * Integer.parseInt(convMaxHourMin[0]) + 16.66 * Integer.parseInt(convMinHourMin[1])) + 18000L;
        if (minHour > 24000) {
            minHour -= 24000;
        }
        if (maxHour > 24000) {
            maxHour -= 24000;
        }
        return new Long[]{minHour, maxHour};
    }

    public boolean getTime(Player player) {
        String type = karma.getConfig().getString("times.use-both-system-and-worlds-time");
        if (type != null && !type.equalsIgnoreCase("NONE")) {
            if (type.equals("BOTH")) {
                return getSystemTime() && getWorldTime(player);
            } else if (type.equalsIgnoreCase("SYSTEM")) {
                return getSystemTime();
            } else if (type.equalsIgnoreCase("WORLDS")) {
                return getWorldTime(player);
            }
        }
        return true;
    }

    public boolean getSystemTime() {
        Set<String> path = karma.getConfig().getConfigurationSection("times.system-times").getKeys(false);
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm");

        String[] timeLimits;

        for ( String timeList : path ) {
            timeLimits = getSystemTimeLimits(timeList);
            if (timeLimits[1].compareTo(timeLimits[0]) >= 0) {
                if (timeLimits[0].compareTo(hhmmFormat.format(now)) <= 0 && timeLimits[1].compareTo(hhmmFormat.format(now)) >= 0) {
                    if ((int) (Math.random() * 100) <= karma.getConfig().getInt("times.system-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            } else {
                if (timeLimits[0].compareTo(hhmmFormat.format(now)) <= 0 || timeLimits[1].compareTo(hhmmFormat.format(now)) >= 0) {
                    if ((int) (Math.random() * 100) <= karma.getConfig().getInt("times.system-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean getWorldTime(Player player) {
        Set<String> path = karma.getConfig().getConfigurationSection("times.worlds-times").getKeys(false);
        World world = player.getWorld();
        Long worldTime = world.getTime();
        Long[] timeLimits;

        for ( String timeList : path ) {
            timeLimits = getWorldTimeLimits(timeList);
            if (timeLimits[0] <= timeLimits[1]) {
                if (timeLimits[0] <= worldTime && timeLimits[1] >= worldTime) {
                    if ((int) (Math.random() * 100) <= karma.getConfig().getInt("times.worlds-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            } else {
                if (timeLimits[0] <= worldTime || timeLimits[1] >= worldTime) {
                    if ((int) (Math.random() * 100) <= karma.getConfig().getInt("times.worlds-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    /**
     * Initialize the file / Line of the player with UUID, name, karma and tier
     * @param player -> The player for the props are gonna be made
     */
    public void initPlayerData(Player player) {
        if (!ifPlayerExistsInDTB(player)) {
            double value = karma.getConfig().getInt("karma.default-karma");
            try {
                if (karma.connection != null && !karma.connection.isClosed()) {
                    PreparedStatement preparedStatement = karma.connection.prepareStatement("INSERT INTO Karma (UUID, NickName, Karma, Tier, Last_Attack)\n" +
                            "VALUES (?, ?, ?, ?, ?);");

                    preparedStatement.setString(1, String.valueOf(player.getUniqueId()));
                    preparedStatement.setString(2, player.getName());
                    preparedStatement.setDouble(3, value);
                    preparedStatement.setString(4, null);
                    preparedStatement.setString(5, null);

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
    }

    /**
     * /**
     * Update the new karma of the player if change is needed.
     * Uses local files or Database if connection is active
     * @param player -> the player
     * @param value -> The new karma amount of the player
     */
    public void setKarmaToPlayer(Player player, double value) {
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {

                String query = "UPDATE Karma SET Karma = ? WHERE UUID = ?;";
                PreparedStatement preparedStatement = karma.connection.prepareStatement(query);

                preparedStatement.setDouble(1, value);
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
        ArrayList<String> array = new ArrayList<String>();
        try {
            if (karma.connection != null && !karma.connection.isClosed()) {
                double[] tierLimits;
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
                        tierCommandsLauncher(player);
                        if (array.contains(tier)) {
                            tierCommandsLauncherOnUp(player);
                        } else {
                            tierCommandsLauncherOnDown(player);
                        }
                        break;
                    }

                    array.add(tierList);
                }

            } else {
                File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

                double[] tierLimits;
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
                        if (tier != null) {
                            if (array.contains(tier)) {
                                tierCommandsLauncherOnUp(player);
                            } else {
                                tierCommandsLauncherOnDown(player);
                            }
                        }
                        tierCommandsLauncher(player);
                        break;
                    }

                    array.add(tierList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void setLastAttackToPlayer(Player player) {
        try {
            if (!player.hasMetadata("NPC")) {
                if (karma.connection != null && !karma.connection.isClosed()) {
                    String query = "UPDATE Karma SET Last_Attack = ? WHERE UUID = ?;";
                    PreparedStatement preparedStatement = karma.connection.prepareStatement(query);

                    preparedStatement.setString(1, "NOW()");
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } else {
                    File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                    YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                    playerConfig.set("last-attack", System.currentTimeMillis());
                    try {
                        playerConfig.save(playerFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                return;
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
        double playerKarma = getPlayerKarma(player);
        double min = this.karma.getConfig().getInt("karma.minimum-karma");
        double max = this.karma.getConfig().getInt("karma.maximum-karma");

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
    private void changePlayerTierMessage(Player player) {
        String message = configurationLang.getString("tier-change");
        if (message != null) {
            message = message.replaceAll("<TIER>", getPlayerDisplayTier(player));
            message = ChatColor.translateAlternateColorCodes('&', message);
            player.sendMessage(message);
        }
    }

    private void tierCommandsLauncher(Player player) {
        String tier = getPlayerTier(player);
        List<String> list = (List<String>) karma.getConfig().getList("tiers." + tier + ".commands");
        if (list != null) {
            for (String line : list) {
                if (line != null) {
                    placeCommands(player, line);
                }
            }
        }
    }

    private void tierCommandsLauncherOnUp(Player player) {
        String tier = getPlayerTier(player);
        List<String> list = (List<String>) karma.getConfig().getList("tiers." + tier + ".commands-on-up");
        if (list != null) {
            for (String line : list) {
                if (line != null) {
                    placeCommands(player, line);
                }
            }
        }
    }

    private void tierCommandsLauncherOnDown(Player player) {
        String tier = getPlayerTier(player);
        List<String> list = (List<String>) karma.getConfig().getList("tiers." + tier + ".commands-on-down");
        if (list != null) {
            for (String line : list) {
                if (line != null) {
                    placeCommands(player, line);
                }
            }
        }
    }

    private void placeCommands(Player player, String command) {
            command = command.replaceAll("<PLAYER>", player.getName());
            command = command.replaceAll("<KARMA>", String.format("%." + nbDec + "f", getPlayerKarma(player)));
            command = command.replaceAll("<TIER>", getPlayerDisplayTier(player));
        command = ChatColor.translateAlternateColorCodes('&', command);

        if (command.startsWith("<@>")) {
            command = command.replaceAll("<@>", "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Bukkit.dispatchCommand(player, command);
        }
    }

}
