package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

/**
 * Changes the attacker karma when killing living entities
 */
public class KillEvents extends GetSet implements Listener {
    private Karma karma = Karma.get();
    AdaptMessage adaptMessage = new AdaptMessage();

    String message = null;

    /**
     * Check and apply karma when a non-pLayer livingEntity is killed by a Player
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = null;
        double killerKarma = 0F;
        double reward = 0F;
        LivingEntity livingEntity;
        String livingEntityName;

        event.getEntity();
        if (event.getEntity().getKiller() != null)
        {
            livingEntity = (LivingEntity) event.getEntity();
            killer = livingEntity.getKiller();
            if (killer != null && getTime(killer))
                livingEntityName = livingEntity.toString().replaceAll("Craft", "");
            else
                return;
        }
        else
            return;

        if (killer.hasMetadata("NPC")) {
            return;
        }

        reward = karma.getConfig().getInt("entities." + livingEntityName + ".kill-karma-reward");

        if (reward != 0) {
            killerKarma = getPlayerKarma(killer);

            if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")
                    && karma.getConfig().getBoolean("general.use-worldguard")) {

                WGPreps wgPreps = new WGPreps();
                double mult = wgPreps.chekMulKarmFlag(killer);
                reward = reward * mult;
            }

            setKarmaToPlayer(killer,killerKarma + reward);
            setTierToPlayer(killer);
        }

        message = karma.getConfig().getString("entities." + livingEntityName + ".kill-message");
        adaptMessage.getEntityKillMessage(message, killer, reward);
    }

    /**
     * Apply a new karma to the Player KILLER when he kills another player
     * @param event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || !getTime(killer))
            return;

        double killerInitialKarma = getPlayerKarma(killer);
        double victimKarma = getPlayerKarma(victim);

        if (killer.hasMetadata("NPC")) {
            return;
        }

        if (!victim.getName().equals(killer.getName())) {

            double arg1 = karma.getConfig().getInt("pvp.kill-reward-variables.1");
            String arg2Str = karma.getConfig().getString("pvp.kill-reward-variables.2");
            double arg2 = 0;
            double arg3 = karma.getConfig().getInt("pvp.kill-reward-variables.3");
            double arg4 = karma.getConfig().getInt("pvp.kill-reward-variables.4");

            if (arg2Str != null) {
                if (arg2Str.equals("<victimKarma>")) {
                    if (!victim.hasMetadata("NPC")) {
                        arg2 = victimKarma;
                    } else {
                        return;
                    }
                } else
                    arg2 = Double.parseDouble(arg2Str);
            }

            double reward = arg1 * (arg2 + arg3) / arg4;

            if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")
                    && karma.getConfig().getBoolean("general.use-worldguard")) {

                WGPreps wgPreps = new WGPreps();
                double mult = wgPreps.chekMulKarmFlag(killer);
                reward = reward * mult;
            }

            double killerNewKarma = killerInitialKarma + reward;

            setKarmaToPlayer(killer,killerNewKarma);
            setTierToPlayer(killer);

            message = null;
            if (killerNewKarma > killerInitialKarma) {
                message = karma.getConfig().getString("pvp.kill-message-on-karma-increase");
            }
            else if (killerNewKarma < killerInitialKarma) {
                message = karma.getConfig().getString("pvp.kill-message-on-karma-decrease");
            }
            if (message != null) {
                adaptMessage.getPlayerKillMessage(message, killer, killerInitialKarma);
            }
        }

    }
}