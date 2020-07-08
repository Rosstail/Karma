package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Gonna be used to optimize the research of values
 */
public class DataHandler {
    private final Karma plugin;
    private final File langFile;
    private final YamlConfiguration configLang;
    private final int nbDec;
    private PAPI papi = new PAPI();

    private static Map<Player, DataHandler> getSets = new HashMap<Player, DataHandler>();
    private Player player;
    private double playerKarma;
    private String playerTier;
    private String playerDisplayTier;
    private long playerLastAttack;

    private DataHandler(Karma plugin, Player player) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder(),
                "lang/" + plugin.getConfig().getString("general.lang") + ".yml");
        this.configLang = YamlConfiguration.loadConfiguration(langFile);
        this.nbDec = plugin.getConfig().getInt("general.decimal-number-to-show");
        this.player = player;
        this.playerKarma = loadPlayerKarma();
        this.playerTier = loadPlayerTier();
        this.playerDisplayTier = loadPlayerDisplayTier();
        this.playerLastAttack = loadPlayerLastAttack();
    }

    public static DataHandler gets(Player player, Karma plugin) {
        if(!getSets.containsKey(player)){ // If player doesn't have instance
            getSets.put(player, new DataHandler(plugin, player));
        }
        return getSets.get(player);
    }

    public double getPlayerKarma() {
        return playerKarma;
    }

    public String getPlayerTier() {
        return playerTier;
    }

    public String getPlayerDisplayTier() {
        return playerDisplayTier;
    }

    public double getPlayerLastAttack() {
        return playerLastAttack;
    }

    public boolean ifPlayerExistsInDTB() {
        String UUID = String.valueOf(player.getUniqueId());
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                PreparedStatement statement = plugin.connection
                    .prepareStatement("SELECT * FROM Karma WHERE UUID = '" + UUID + "';");
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    playerKarma = result.getDouble("Karma");
                    playerTier = result.getString("Tier");
                    playerLastAttack = result.getLong("Last_Attack");
                    return true;
                }
                statement.close();
                playerDisplayTier = getPlayerDisplayTier();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Request the player karma inside file or database
     *
     * @return
     */
    public double loadPlayerKarma() {
        String UUID = String.valueOf(player.getUniqueId());
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                PreparedStatement statement = plugin.connection
                    .prepareStatement("SELECT Karma FROM Karma WHERE UUID = '" + UUID + "';");
                ResultSet result = statement.executeQuery();
                double karma = plugin.getConfig().getDouble("karma.default-karma");
                while (result.next()) {
                    karma = result.getDouble("Karma");
                }
                statement.close();
                return karma;
            } else {
                File playerFile =
                    new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                return playerConfig.getDouble("karma");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Request the player Tier inside file or database
     *
     * @return
     */
    public String loadPlayerTier() {
        String tier = null;
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                String UUID = String.valueOf(player.getUniqueId());
                PreparedStatement statement = plugin.connection.prepareStatement("SELECT Tier FROM Karma WHERE UUID = '" + UUID + "';");
                ResultSet result =
                    statement.executeQuery();
                while (result.next()) {
                    tier = result.getString("Tier");
                }
                statement.close();
            } else {
                String UUID = String.valueOf(player.getUniqueId());
                File playerFile = new File(plugin.getDataFolder(), "playerdata/" + UUID + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                tier = playerConfig.getString("tier");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (tier == null) {
            tier = getPlayerTier();
        }
        return tier;
    }

    /**
     * Returns the player last attack long
     *
     * @return
     */
    public long loadPlayerLastAttack() {
        if (!player.hasMetadata("NPC")) {
            return 0L;
        }
        try {
            String UUID = String.valueOf(player.getUniqueId());
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                PreparedStatement statement = plugin.connection
                    .prepareStatement("SELECT Last_Attack FROM Karma WHERE UUID = '" + UUID + "';");
                ResultSet result = statement.executeQuery();
                long dateTime = 0L;
                while (result.next()) {
                    dateTime = result.getLong("Last_Attack");
                }
                statement.close();
                return dateTime;
            } else {
                File playerFile = new File(plugin.getDataFolder(), "playerdata/" + UUID + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                return playerConfig.getLong("last-attack");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * return the displaying name of the tier.
     *
     * @return
     */
    public String loadPlayerDisplayTier() {
        playerDisplayTier = plugin.getConfig()
                .getString("tiers." + playerTier + ".tier-display-name");
        return playerDisplayTier;
    }

    /**
     * Get the karma limits of karma for specified tier in Config.yml
     *
     * @return
     */
    public double[] getTierLimits(String tier) {
        double tierMinimumKarma =
            plugin.getConfig().getDouble("tiers." + tier + ".tier-minimum-karma");
        double tierMaximumKarma =
            plugin.getConfig().getDouble("tiers." + tier + ".tier-maximum-karma");
        return new double[] {tierMinimumKarma, tierMaximumKarma};
    }

    public String[] getSystemTimeLimits(String time) {
        String minimumHourMin =
            plugin.getConfig().getString("times.system-times." + time + ".starting-time");
        String maximumHourMin =
            plugin.getConfig().getString("times.system-times." + time + ".ending-time");
        return new String[] {minimumHourMin, maximumHourMin};
    }

    public long[] getWorldTimeLimits(String time) {
        String minimumHourMin =
            plugin.getConfig().getString("times.worlds-times." + time + ".starting-time");
        String maximumHourMin =
            plugin.getConfig().getString("times.worlds-times." + time + ".ending-time");

        assert minimumHourMin != null;
        assert maximumHourMin != null;
        String[] convMinHourMin = minimumHourMin.split(":", 2);
        String[] convMaxHourMin = maximumHourMin.split(":", 2);
        long minHour = (long) (1000 * Integer.parseInt(convMinHourMin[0]) + 16.66 * Integer
            .parseInt(convMinHourMin[1])) + 18000L;
        long maxHour = (long) (1000 * Integer.parseInt(convMaxHourMin[0]) + 16.66 * Integer
            .parseInt(convMinHourMin[1])) + 18000L;
        if (minHour > 24000) {
            minHour -= 24000;
        }
        if (maxHour > 24000) {
            maxHour -= 24000;
        }
        return new long[] {minHour, maxHour};
    }

    public boolean getTime() {
        String type = plugin.getConfig().getString("times.use-both-system-and-worlds-time");
        if (type != null && !type.equalsIgnoreCase("NONE")) {
            if (type.equals("BOTH")) {
                return getSystemTime() && getWorldTime();
            } else if (type.equalsIgnoreCase("SYSTEM")) {
                return getSystemTime();
            } else if (type.equalsIgnoreCase("WORLDS")) {
                return getWorldTime();
            }
        }
        return true;
    }

    public boolean getSystemTime() {
        Set<String> path =
            plugin.getConfig().getConfigurationSection("times.system-times").getKeys(false);
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm");

        String[] timeLimits;

        for (String timeList : path) {
            timeLimits = getSystemTimeLimits(timeList);
            if (timeLimits[1].compareTo(timeLimits[0]) >= 0) {
                if (timeLimits[0].compareTo(hhmmFormat.format(now)) <= 0
                    && timeLimits[1].compareTo(hhmmFormat.format(now)) >= 0) {
                    if (ThreadLocalRandom.current().nextInt(0, 100) <= plugin.getConfig()
                        .getInt("times.system-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            } else {
                if (timeLimits[0].compareTo(hhmmFormat.format(now)) <= 0
                    || timeLimits[1].compareTo(hhmmFormat.format(now)) >= 0) {
                    if (ThreadLocalRandom.current().nextInt(0, 100) <= plugin.getConfig()
                        .getInt("times.system-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean getWorldTime() {
        Set<String> path =
            plugin.getConfig().getConfigurationSection("times.worlds-times").getKeys(false);
        World world = player.getWorld();
        long worldTime = world.getTime();
        long[] timeLimits;

        for (String timeList : path) {
            timeLimits = getWorldTimeLimits(timeList);
            if (timeLimits[0] <= timeLimits[1]) {
                if (timeLimits[0] <= worldTime && timeLimits[1] >= worldTime) {
                    if (ThreadLocalRandom.current().nextInt(0, 100) <= plugin.getConfig()
                        .getInt("times.worlds-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            } else {
                if (timeLimits[0] <= worldTime || timeLimits[1] >= worldTime) {
                    if (ThreadLocalRandom.current().nextInt(0, 100) <= plugin.getConfig()
                        .getInt("times.worlds-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Initialize the file / Line of the player with UUID, name, karma and tier
     *
     */
    public void initPlayerData() {
        File playerFile =
                new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");

        if (ifPlayerExistsInDTB()) {
            updatePlayerNameDTB();
        } else {
            try {
                if (plugin.connection != null && !plugin.connection.isClosed()) {
                    initPlayerDataDTB();
                } else if (!playerFile.exists()){
                    initPlayerDataLocale();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void initPlayerDataDTB() {
        String UUID = String.valueOf(player.getUniqueId());
        String name = player.getName();
        double value = plugin.getConfig().getDouble("karma.default-karma");
        double min = plugin.getConfig().getDouble("karma.minimum-karma");
        double max = plugin.getConfig().getDouble("karma.maximum-karma");

        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        double finalValue = value;

        this.playerKarma = value;
        setTierToPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = plugin.connection.prepareStatement(
                            "INSERT INTO Karma (UUID, NickName, Karma, Tier, Last_Attack)\n"
                                    + "VALUES (?, ?, ?, ?, ?);");

                    preparedStatement.setString(1, UUID);
                    preparedStatement.setString(2, name);
                    preparedStatement.setDouble(3, finalValue);
                    preparedStatement.setString(4, null);
                    preparedStatement.setString(5, null);

                    preparedStatement.execute();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initPlayerDataLocale() {
        String UUID = String.valueOf(player.getUniqueId());
        double value = plugin.getConfig().getDouble("karma.default-karma");
        double min = plugin.getConfig().getDouble("karma.minimum-karma");
        double max = plugin.getConfig().getDouble("karma.maximum-karma");

        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        File playerFile =
                new File(plugin.getDataFolder(), "playerdata/" + UUID + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        double finalValue = value;
        playerKarma = finalValue;
        setTierToPlayer();
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

    private void updatePlayerNameDTB() {
        String nickName = player.getName();
        String UUID = player.getUniqueId().toString();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = plugin.connection.prepareStatement(
                            "UPDATE Karma SET NickName = ? WHERE UUID = ?;\n"
                                    + "VALUES (?, ?);");
                    preparedStatement.setString(1, nickName);
                    preparedStatement.setString(2, UUID);
                } catch (SQLException e) {
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
    public void setKarmaToPlayer(double value) {
        double min = plugin.getConfig().getDouble("karma.minimum-karma");
        double max = plugin.getConfig().getDouble("karma.maximum-karma");
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        this.playerKarma = value;

        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                setPlayerKarmaDTB(value);
            } else {
                setPlayerKarmaLocale(value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setPlayerKarmaDTB(double value) {
        String UUID = player.getUniqueId().toString();

        double finalValue = value;
        setTierToPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    String query = "UPDATE Karma SET Karma = ? WHERE UUID = ?;";
                    PreparedStatement preparedStatement = plugin.connection.prepareStatement(query);

                    preparedStatement.setDouble(1, finalValue);
                    preparedStatement.setString(2, UUID);

                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setPlayerKarmaLocale(double value) {
        double finalValue = value;
        setTierToPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                File playerFile = new File(plugin.getDataFolder(),
                        "playerdata/" + player.getUniqueId().toString() + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
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
     * Set the new tier of the player if change is needed.
     * Uses local files or Database if connection is active
     *
     */
    public void setTierToPlayer() {
        String UUID = player.getUniqueId().toString();
        String startTier = getPlayerTier();

        Set<String> path = plugin.getConfig().getConfigurationSection("tiers").getKeys(false);
        ArrayList<String> array = new ArrayList<>();
        double[] tierLimits;

        for (String tierList : path) {
            tierLimits = getTierLimits(tierList);
            if (getPlayerKarma() >= tierLimits[0]
                    && getPlayerKarma() <= tierLimits[1] && !tierList.equals(getPlayerTier())) {
                this.playerTier = tierList;
                this.playerDisplayTier = plugin.getConfig()
                        .getString("tiers." + getPlayerTier() + ".tier-display-name");

                changePlayerTierMessage();
                tierCommandsLauncher();
                if (startTier != null) {
                    if (array.contains(startTier)) {
                        tierCommandsLauncherOnUp();
                    } else {
                        tierCommandsLauncherOnDown();
                    }
                }
                break;
            }

            array.add(tierList);
        }

        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                setPlayerTierDTB(UUID);
            } else {
                setPlayerTierLocale(UUID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setPlayerTierDTB(String UUID) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    String query = "UPDATE Karma SET Tier = ? WHERE UUID = ?;";
                    PreparedStatement preparedStatement =
                            plugin.connection.prepareStatement(query);

                    preparedStatement.setString(1, getPlayerTier());
                    preparedStatement.setString(2, UUID);

                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setPlayerTierLocale(String UUID) {
        String tier = playerTier;

        File playerFile = new File(plugin.getDataFolder(),
                "playerdata/" + UUID + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                playerConfig.set("tier", tier);

                try {
                    playerConfig.save(playerFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Set the timestamp of the player's attack moment if needed
     *
     */
    public void setLastAttackToPlayer() {
        if (player.hasMetadata("NPC")) {
            return;
        }
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                setPlayerLastAttackDTB();
            } else {
                setPlayerLastAttackLocale();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setPlayerLastAttackDTB() {
        String UUID = player.getUniqueId().toString();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    String query = "UPDATE Karma SET Last_Attack = ? WHERE UUID = ?;";
                    PreparedStatement preparedStatement = plugin.connection.prepareStatement(query);

                    preparedStatement.setString(1, "NOW()");
                    preparedStatement.setString(2, UUID);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setPlayerLastAttackLocale() {
        String UUID = player.getUniqueId().toString();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                File playerFile = new File(plugin.getDataFolder(),
                        "playerdata/" + UUID + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                playerConfig.set("last-attack", System.currentTimeMillis());
                try {
                    playerConfig.save(playerFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changePlayerTierMessage() {
        String message = configLang.getString("tier-change");
        if (message != null) {
            AdaptMessage adaptMessage = new AdaptMessage(plugin);
            adaptMessage.message(player, player, 0, message);
        }
    }

    private void tierCommandsLauncher() {
        String tier = getPlayerTier();
        List<String> list = plugin.getConfig().getStringList("tiers." + tier + ".commands");
        for (String line : list) {
            if (line != null) {
                placeCommands(player, line);
            }
        }
    }

    private void tierCommandsLauncherOnUp() {
        String tier = getPlayerTier();
        List<String> list = plugin.getConfig().getStringList("tiers." + tier + ".commands-on-up");
        for (String line : list) {
            if (line != null) {
                placeCommands(player, line);
            }
        }
    }

    private void tierCommandsLauncherOnDown() {
        String tier = getPlayerTier();
        List<String> list = plugin.getConfig().getStringList("tiers." + tier + ".commands-on-down");
        for (String line : list) {
            if (line != null) {
                placeCommands(player, line);
            }
        }
    }

    private void placeCommands(Player player, String command) {
        command = command.replaceAll("<PLAYER>", player.getName());
        command = command
            .replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));
        command = command.replaceAll("<TIER>", playerDisplayTier);
        command = ChatColor.translateAlternateColorCodes('&', command);
        command = papi.setPlaceholdersOnMessage(command, player);

        if (command.startsWith("<MESSAGE>")) {
            command = command.replaceAll("<MESSAGE>", "").trim();
            AdaptMessage adaptMessage = new AdaptMessage(plugin);
            adaptMessage.message(player, player, 0, command);
        } else if (command.startsWith("<@>")) {
            command = command.replaceAll("<@>", "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Bukkit.dispatchCommand(player, command);
        }
    }

}
