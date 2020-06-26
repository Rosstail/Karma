package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
public class GetSet {
    private final Karma plugin;
    private final File langFile;
    private final YamlConfiguration configLang;
    private final int nbDec;
    private PAPI papi = new PAPI();

    private static Map<Player, GetSet> getSets = new HashMap<Player, GetSet>();
    public Player player;
    public double playerKarma;
    public String playerTier;
    public String playerDisplayTier;
    public long playerLastAttack;

    private GetSet(Karma plugin, Player player) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder(),
                "lang/" + plugin.getConfig().getString("general.lang") + ".yml");
        this.configLang = YamlConfiguration.loadConfiguration(langFile);
        this.nbDec = plugin.getConfig().getInt("general.decimal-number-to-show");
        this.player = player;
        this.playerKarma = getPlayerKarma();
        this.playerTier = getPlayerTier();
        this.playerDisplayTier = getPlayerDisplayTier();
        this.playerLastAttack = getPlayerLastAttack();
    }

    public static GetSet gets(Player player, Karma plugin) {
        if(!getSets.containsKey(player)){ // If player doesn't have instance
            getSets.put(player, new GetSet(plugin, player));
        }
        return getSets.get(player);
    }

    public double getVarPlayerKarma() {
        System.out.println("getVarPlayerKarma = " + playerKarma);
        return playerKarma;
    }

    public String getVarPlayerTier() {
        return playerTier;
    }

    public String getVarPlayerDisplayTier() {
        return playerDisplayTier;
    }

    public double getVarPlayerLastAttack() {
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
     * Returns the amount of Karma of the player
     *
     * @return
     */
    public double getPlayerKarma() {
        String UUID = String.valueOf(player.getUniqueId());
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                PreparedStatement statement = plugin.connection
                    .prepareStatement("SELECT Karma FROM Karma WHERE UUID = '" + UUID + "';");
                ResultSet result = statement.executeQuery();
                double karma = 0;
                while (result.next()) {
                    karma = result.getDouble("Karma");
                }
                statement.close();
                this.playerKarma = karma;
                return karma;
            } else {
                File playerFile =
                    new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                this.playerKarma = playerConfig.getDouble("karma");
                return playerConfig.getDouble("karma");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the Tier identifier of the player
     *
     * @return
     */
    public String getPlayerTier() {
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                String UUID = String.valueOf(player.getUniqueId());
                PreparedStatement statement = plugin.connection.prepareStatement("SELECT Tier FROM Karma WHERE UUID = '" + UUID + "';");
                ResultSet result =
                    statement.executeQuery();
                String tier = null;
                while (result.next()) {
                    tier = result.getString("Tier");
                }
                statement.close();
                this.playerTier = tier;
                return tier;
            } else {
                String UUID = String.valueOf(player.getUniqueId());
                File playerFile = new File(plugin.getDataFolder(), "playerdata/" + UUID + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                this.playerTier = playerConfig.getString("tier");
                return playerConfig.getString("tier");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the Tier identifier of the player
     *
     * @return
     */
    public long getPlayerLastAttack() {
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
                this.playerLastAttack = dateTime;
                return dateTime;
            } else {
                File playerFile = new File(plugin.getDataFolder(), "playerdata/" + UUID + ".yml");
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                this.playerLastAttack = playerConfig.getLong("last-attack");
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
    public String getPlayerDisplayTier() {
        playerDisplayTier = plugin.getConfig()
                .getString("tiers." + playerTier + ".tier-display-name");
        return playerDisplayTier;
    }

    /**
     * Get the karma limits of karma for specified tier in Config.yml
     *
     * @return
     */
    public double[] getTierLimits() {
        double tierMinimumKarma =
            plugin.getConfig().getDouble("tiers." + playerTier + ".tier-minimum-karma");
        double tierMaximumKarma =
            plugin.getConfig().getDouble("tiers." + playerTier + ".tier-maximum-karma");
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
        if (ifPlayerExistsInDTB()) {
            updatePlayerNameDTB();
            return;
        }
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                initPlayerDataDTB();
            } else {
                initPlayerDataLocale();
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                setTierToPlayer();
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
        double min = plugin.getConfig().getDouble("karma.minimum-karma");
        double max = plugin.getConfig().getDouble("karma.maximum-karma");

        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        double finalValue = value;
        playerKarma = value;
        System.out.println("Nouvelle valeur de playerKarma dans GetSet : " + playerKarma);
        System.out.println("Valeur du getter dans GetSet" + getVarPlayerKarma());
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
        double min = plugin.getConfig().getDouble("karma.minimum-karma");
        double max = plugin.getConfig().getDouble("karma.maximum-karma");

        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        double finalValue = value;
        playerKarma = value;
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
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                setPlayerTierDTB();
            } else {
                setPlayerTierLocale();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setPlayerTierDTB() {
        String UUID = player.getUniqueId().toString();
        String tier = playerTier;

        Set<String> path = plugin.getConfig().getConfigurationSection("tiers").getKeys(false);
        ArrayList<String> array = new ArrayList<>();
        double[] tierLimits;

        for (String tierList : path) {
            tierLimits = getTierLimits();

            if (getPlayerKarma() >= tierLimits[0]
                    && getPlayerKarma() <= tierLimits[1] && !tierList.equals(tier)) {
                playerTier = tierList;
                playerDisplayTier = plugin.getConfig()
                        .getString("tiers." + tierList + ".tier-display-name");

                changePlayerTierMessage();
                tierCommandsLauncher();
                if (array.contains(tier)) {
                    tierCommandsLauncherOnUp();
                } else {
                    tierCommandsLauncherOnDown();
                }

                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String query = "UPDATE Karma SET Tier = ? WHERE UUID = ?;";
                            PreparedStatement preparedStatement =
                                    plugin.connection.prepareStatement(query);

                            preparedStatement.setString(1, tierList);
                            preparedStatement.setString(2, UUID);

                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            }

            array.add(tierList);
        }
    }

    private void setPlayerTierLocale() {
        String UUID = player.getUniqueId().toString();
        Set<String> path = plugin.getConfig().getConfigurationSection("tiers").getKeys(false);
        String tier = playerTier;
        ArrayList<String> array = new ArrayList<>();

        File playerFile = new File(plugin.getDataFolder(),
                "playerdata/" + UUID + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

                double[] tierLimits;

                for (String tierList : path) {
                    tierLimits = getTierLimits();

                    if (getPlayerKarma() >= tierLimits[0]
                            && getPlayerKarma() <= tierLimits[1] && !tierList.equals(tier)) {
                        playerTier = tierList;
                        playerDisplayTier = plugin.getConfig()
                                .getString("tiers." + tierList + ".tier-display-name");

                        changePlayerTierMessage();
                        if (tier != null) {
                            if (array.contains(tier)) {
                                tierCommandsLauncherOnUp();
                            } else {
                                tierCommandsLauncherOnDown();
                            }
                        }
                        tierCommandsLauncher();

                        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                            @Override
                            public void run() {
                                playerConfig.set("tier", tierList);

                                try {
                                    playerConfig.save(playerFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    }

                    array.add(tierList);
                }
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

    public void createPlayerLocaleFile(Player player, File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("name", player.getName());
        configuration.set("karma", plugin.getConfig().getDouble("karma.default-karma"));
        this.playerKarma = plugin.getConfig().getDouble("karma.default-karma");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    configuration.save(file);
                    setTierToPlayer();
                } catch (IOException var4) {
                    var4.printStackTrace();
                }
            }
        });
    }

    /**
     * MUST BE MOVED ON ANOTHER CLASS
     * SEND A MESSAGE WITH THE SPECIFIED TIER TO THE PLAYER
     *
     */
    private void changePlayerTierMessage() {
        String message = configLang.getString("tier-change");
        if (message != null) {
            message = message.replaceAll("<TIER>", playerDisplayTier);
            message = ChatColor.translateAlternateColorCodes('&', message);
            message = papi.setPlaceholdersOnMessage(message, player);
            player.sendMessage(message);
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

        if (command.startsWith("<@>")) {
            command = command.replaceAll("<@>", "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Bukkit.dispatchCommand(player, command);
        }
    }

}
