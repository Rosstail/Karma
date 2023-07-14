package com.rosstail.karma.overtime;

import com.rosstail.karma.lang.AdaptMessage;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class OvertimeLoop {
    public final String name;
    public final String displayName;
    public final long firstTimer;
    public final long nextTimer;
    public boolean online;
    public boolean offline;

    public boolean hasMinKarma;
    public final float minKarma;
    public boolean hasMaxKarma;
    public final float maxKarma;

    public final float amount;
    public List<String> commands;

    public OvertimeLoop(ConfigurationSection section) {
        name = section.getName();
        displayName = AdaptMessage.getAdaptMessage().adaptMessage(name);
        firstTimer = Math.max(1L, section.getLong("timers.first")) * 1000L;
        nextTimer = section.getString("timers.next") != null ? section.getLong("timers.next") * 1000L : firstTimer;

        offline = section.getBoolean("requirements.offline", false);
        online = section.getBoolean("requirements.online", true);

        hasMinKarma = section.getString("requirements.karma.minimum") != null;
        minKarma = (float) section.getDouble("requirements.karma.minimum");
        hasMaxKarma = section.getString("requirements.karma.maximum") != null;
        maxKarma = (float) section.getDouble("requirements.karma.maximum");

        amount = (float) section.getDouble("amount");
        commands = section.getStringList("commands");
    }
}
