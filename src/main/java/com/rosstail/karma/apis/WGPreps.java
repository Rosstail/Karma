package com.rosstail.karma.apis;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.datas.PlayerData;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.rosstail.karma.Karma;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class WGPreps {

    public static Karma plugin = Karma.getInstance();
    public static WGPreps wgPreps;
    public static DoubleFlag KARMA_MULTIPLICATION;
    public static IntegerFlag KARMA_CHANGE_CHANCE;
    public static DoubleFlag KARMA_MINIMUM;
    public static DoubleFlag KARMA_MAXIMUM;

    public static void initWGPreps() {
        wgPreps = new WGPreps();
    }

    public static WGPreps getWgPreps() {
        return wgPreps;
    }

    public void worldGuardHook() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            DoubleFlag karmaMultiFlag = new DoubleFlag("karma-multiplicator");
            registry.register(karmaMultiFlag);
            KARMA_MULTIPLICATION = karmaMultiFlag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("karma-multiplicator");
            if (existing instanceof DoubleFlag) {
                KARMA_MULTIPLICATION = (DoubleFlag) existing;
            } else {
                Karma.getPlugin(Karma.class).getLogger().log(Level.WARNING,
                    "[WARNING] CONFLICT BETWEEN KARMA karma-multiplier FLAG AND ANOTHER PLUGIN, PLEASE CONTACT ROSSTAIL ON SPIGOT OR DISCORD");
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            IntegerFlag karmaChangFlag = new IntegerFlag("karma-change-chance");
            registry.register(karmaChangFlag);
            KARMA_CHANGE_CHANCE = karmaChangFlag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("karma-change-chance");
            if (existing instanceof IntegerFlag) {
                KARMA_CHANGE_CHANCE = (IntegerFlag) existing;
            } else {
                Karma.getPlugin(Karma.class).getLogger().log(Level.WARNING,
                    "[WARNING] CONFLICT BETWEEN KARMA karma-change-chance FLAG AND ANOTHER PLUGIN, PLEASE CONTACT ROSSTAIL ON SPIGOT OR DISCORD");
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            DoubleFlag minKarmaEntryFlag = new DoubleFlag("entry-min-karma");
            registry.register(minKarmaEntryFlag);
            KARMA_MINIMUM = minKarmaEntryFlag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("entry-min-karma");
            if (existing instanceof DoubleFlag) {
                KARMA_MINIMUM = (DoubleFlag) existing;
            } else {
                Karma.getPlugin(Karma.class).getLogger().log(Level.WARNING,
                    "[WARNING] CONFLICT BETWEEN KARMA entry-min-karma FLAG AND ANOTHER PLUGIN, PLEASE CONTACT ROSSTAIL ON SPIGOT OR DISCORD");
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            DoubleFlag maxKarmaEntryFlag = new DoubleFlag("entry-max-karma");
            registry.register(maxKarmaEntryFlag);
            KARMA_MAXIMUM = maxKarmaEntryFlag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("entry-max-karma");
            if (existing instanceof DoubleFlag) {
                KARMA_MAXIMUM = (DoubleFlag) existing;
            } else {
                Karma.getPlugin(Karma.class).getLogger().log(Level.WARNING,
                    "[WARNING] CONFLICT BETWEEN KARMA entry-max-karma FLAG AND ANOTHER PLUGIN, PLEASE CONTACT ROSSTAIL ON SPIGOT OR DISCORD");
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
    }

    public boolean checkRequiredKarmaFlags(Player player) {
        double karma = PlayerData.gets(player).getKarma();
        boolean value = true;
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location location = localPlayer.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        boolean hasMinKarma = hasReqFlag(location, localPlayer, query, KARMA_MINIMUM);
        boolean hasMaxKarma = hasReqFlag(location, localPlayer, query, KARMA_MAXIMUM);

        if (hasMinKarma) {
            if (karma < query.queryValue(location, localPlayer, KARMA_MINIMUM)) {
                value = false;
            }
        }
        if (hasMaxKarma) {
            if (karma > query.queryValue(location, localPlayer, KARMA_MAXIMUM)) {
                value = false;
            }
        }
        return value;
    }

    private boolean hasReqFlag(com.sk89q.worldedit.util.Location location, LocalPlayer localPlayer, RegionQuery query, DoubleFlag flag) {
        return query.queryValue(location, localPlayer, flag) != null;
    }

    public double checkMultipleKarmaFlags(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location location = localPlayer.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (ThreadLocalRandom.current().nextInt(0, 100) >= checkChangeKarmaFlag(player)) {
            return 0;
        }
        Double value = query.queryValue(location, localPlayer, KARMA_MULTIPLICATION);
        return value == null ? 1 : value;
    }

    public int checkChangeKarmaFlag(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location location = localPlayer.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        Integer value = query.queryValue(location, localPlayer, KARMA_CHANGE_CHANCE);
        return value == null ? 100 : value;
    }
}
