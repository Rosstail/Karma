package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;
import java.io.IOException;

/**
 * Changes the attacker karma when killing living entities
 */
public class KillEvents implements Listener {
    private Karma karma = Karma.getInstance();
    VerifyKarmaLimits verifyKarmaLimits = new VerifyKarmaLimits();
    SetTier setTier = new SetTier();
    AdaptMessage adaptMessage = new AdaptMessage();
    String message = null;

    /**
     * Check and apply karma when a non-pLayer livingEntity is killed by a Player
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = null;
        int killerKarma = 0;
        int reward = 0;
        LivingEntity livingEntity;
        String livingEntityName;
        SetTier setTier = new SetTier();

        event.getEntity();
        if (event.getEntity().getKiller() != null)
        {
            livingEntity = (LivingEntity) event.getEntity();
            killer = livingEntity.getKiller();
            if (killer != null)
                livingEntityName = livingEntity.toString().replaceAll("Craft", "");
            else
                return;
        }
        else
            return;

        reward = karma.getConfig().getInt("entities." + livingEntityName + ".kill-karma-reward");

        if (reward != 0) {
            File killerFile = new File(this.karma.getDataFolder(), "playerdata/" + killer.getUniqueId() + ".yml");
            YamlConfiguration killerConfig = YamlConfiguration.loadConfiguration(killerFile);
            killerKarma = killerConfig.getInt("karma");

            killerConfig.set("karma", killerKarma + reward);
            try {
                killerConfig.save(killerFile);
                verifyKarmaLimits.checkKarmaLimit(killer);
                setTier.checkTier(killer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        message = karma.getConfig().getString("entities." + livingEntityName + ".kill-message");
        adaptMessage.getEntityKillMessage(message, killer, killerKarma, reward);
    }

    /**
     * Apply a new karma to the Player KILLER when he kills another player
     * @param event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null)
            return;

        File killerFile = new File(this.karma.getDataFolder(), "playerdata/" + killer.getUniqueId() + ".yml");
        YamlConfiguration killerConfig = YamlConfiguration.loadConfiguration(killerFile);
        int killerInitialKarma = killerConfig.getInt("karma");
        File victimFile = new File(this.karma.getDataFolder(), "playerdata/" + victim.getUniqueId() + ".yml");
        YamlConfiguration victimConfig = YamlConfiguration.loadConfiguration(victimFile);
        int victimKarma = victimConfig.getInt("karma");

        if (!victim.getName().equals(killer.getName())) {

            int arg1 = karma.getConfig().getInt("pvp.kill-reward-variables.1");
            String arg2Str = karma.getConfig().getString("pvp.kill-reward-variables.2");
            int arg2 = 0;
            int arg3 = karma.getConfig().getInt("pvp.kill-reward-variables.3");
            int arg4 = karma.getConfig().getInt("pvp.kill-reward-variables.4");

            if (arg2Str != null) {
                if (arg2Str.equals("<victimKarma>")) {
                    arg2 = victimKarma;
                } else
                    arg2 = Integer.parseInt(arg2Str);
            }

            int killerNewKarma = killerInitialKarma + arg1 * (arg2 + arg3) / arg4;

            killerConfig.set("karma", killerNewKarma);
            try {
                killerConfig.save(killerFile);
                verifyKarmaLimits.checkKarmaLimit(killer);
                setTier.checkTier(killer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            message = null;
            if (killerNewKarma > killerInitialKarma) {
                message = karma.getConfig().getString("pvp.kill-message-on-karma-increase");
            }
            else if (killerNewKarma < killerInitialKarma) {
                message = karma.getConfig().getString("pvp.kill-message-on-karma-decrease");
            }
            if (message != null) {
                adaptMessage.getPlayerKillMessage(message, killer, killerInitialKarma, killerNewKarma);
            }
        }

    }
}