package com.rosstail.karma.timeManagement.times;

import org.bukkit.configuration.ConfigurationSection;

public class WorldsTimes {

    private final String name;
    private final long startTime;
    private final long endTime;
    private final byte rate;

    public WorldsTimes(ConfigurationSection section, String name) {
        this.name = name;

        this.startTime = formatToLong(section.getString("start-time"));
        this.endTime = formatToLong(section.getString("end-time"));
        this.rate = (byte) section.getInt("rate");
    }

    private long formatToLong(String formattedTime) {
        String[] convMinHourMin = formattedTime.split(":", 2);
        long hour = (long) (1000L * Integer.parseInt(convMinHourMin[0]) + 16.66 * Integer
                .parseInt(convMinHourMin[1])) + 18000L;
        if (hour > 24000L) {
            hour -= 24000L;
        }
        return hour;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public byte getRate() {
        return rate;
    }

    public boolean roll() {
        double diceRoll = Math.random() * 100;
        boolean bool = diceRoll <= rate;
        return bool;
    }
}
