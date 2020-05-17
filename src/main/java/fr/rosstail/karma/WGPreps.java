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
                System.out.println("[WARNING] CONFLICT BETWEEN KARMA karma-multiplier FLAG AND ANOTHER PLUGIN, PLEASE CONTACT ROSSTAIL ON SPIGOT OR DISCORD");
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
                System.out.println("[WARNING] CONFLICT BETWEEN KARMA karma-change-chance FLAG AND ANOTHER PLUGIN, PLEASE CONTACT ROSSTAIL ON SPIGOT OR DISCORD");
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
    }

    public double chekMulKarmFlag(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location location = localPlayer.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (!((int) (Math.random() * 100) <= chekChangKarmFlag(player))) {
            return 0;
        }
        if (query.queryValue(location, localPlayer, KARMA_MULTIPLICATOR) != null) {
            return query.queryValue(location, localPlayer, KARMA_MULTIPLICATOR);
        }
        return 1;
    }

    public int chekChangKarmFlag(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location location = localPlayer.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (query.queryValue(location, localPlayer, KARMA_CHANGE_CHANCE) != null) {
            return query.queryValue(location, localPlayer, KARMA_CHANGE_CHANCE);
        }
        return 100;
    }
}
