package com.rosstail.karma.timeperiod.times;

import org.bukkit.configuration.ConfigurationSection;

public class SystemTimes {

    private final String name;
    private final String startTime;
    private final String endTime;
    private final byte rate;

    public SystemTimes(ConfigurationSection section, String name) {
        this.name = name;

        this.startTime = section.getString("start-time");
        this.endTime = section.getString("end-time");
        this.rate = (byte) section.getInt("rate");
    }

    public String getName() {
        return name;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public byte getRate() {
        return rate;
    }

    public boolean roll() {
        return Math.random() * 100 <= rate;
    }
}
