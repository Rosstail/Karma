package com.rosstail.karma.timeperiod.times;

import org.bukkit.configuration.ConfigurationSection;

public class WorldsTimes {
    private final String name;
    private final long startTime;
    private final long endTime;
    private final byte rate;

    public WorldsTimes(ConfigurationSection section, String name) {
        this.name = name;

        this.startTime = formatToLong(section.getString("start", "00:00"));
        this.endTime = formatToLong(section.getString("end", "00:00"));
        this.rate = (byte) section.getInt("rate");
    }

    private long formatToLong(String formattedTime) {
        String[] convMinHourMin = formattedTime.split(":", 2);
        long hour = (long) (1000L * Integer.parseInt(convMinHourMin[0]) + 16.66F * Integer
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
        float diceRoll = (float) Math.random() * 100;
        return diceRoll <= rate;
    }
}
