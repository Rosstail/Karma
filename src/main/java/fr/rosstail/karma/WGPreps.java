package fr.rosstail.karma;

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
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class WGPreps {

    public static DoubleFlag KARMA_MULTIPLICATOR;
    public static IntegerFlag KARMA_CHANGE_CHANCE;

    public void worldGuardHook() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            DoubleFlag karmaMultFlag = new DoubleFlag("karma-multiplicator");
            registry.register(karmaMultFlag);
            KARMA_MULTIPLICATOR = karmaMultFlag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("karma-multiplicator");
            if (existing instanceof DoubleFlag) {
                KARMA_MULTIPLICATOR = (DoubleFlag) existing;
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
    }

    public double checkMultipleKarmaFlags(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location location = localPlayer.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (ThreadLocalRandom.current().nextInt(0, 100) >= checkChangeKarmaFlag(player)) {
            return 0;
        }
        Double value = query.queryValue(location, localPlayer, KARMA_MULTIPLICATOR);
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
