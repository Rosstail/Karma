package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Mob;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;
import java.io.IOException;

/**
 * Changes the attacker karma when attacking / killing entities
 */
public class KillEvents implements Listener {
    private Karma karma = Karma.getInstance();
    VerifyKarmaLimits verifyKarmaLimits = new VerifyKarmaLimits();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = null;
        int reward = 0;
        String message;
        Mob mob;
        String mobName;
        SetTier setTier = new SetTier();
        int killerKarma = 0;
        int killerModifiedKarma = 0;

        if (event.getEntity() instanceof Mob && event.getEntity().getKiller() != null)
        {
            mob = (Mob) event.getEntity();
            killer = mob.getKiller();
            if (killer != null)
                mobName = mob.toString().replaceAll("Craft", "");
            else
                return;
        }
        else
            return;

        reward = karma.getConfig().getInt("entities." + mobName + ".kill-karma-reward");

        if (reward != 0) {
            File killerFile = new File(this.karma.getDataFolder(), "playerdata/" + killer.getUniqueId() + ".yml");
            YamlConfiguration killerConfig = YamlConfiguration.loadConfiguration(killerFile);
            killerKarma = killerConfig.getInt("karma");
            killerModifiedKarma = killerKarma + reward;

            killerConfig.set("karma", killerModifiedKarma);
            try {
                killerConfig.save(killerFile);
                verifyKarmaLimits.checkKarmaLimit(killer);
                setTier.checkTier(killer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        message = karma.getConfig().getString("entities." + mobName + ".kill-message");

        if (message != null) {
            message = message.replaceAll("<attacker>", killer.getName());
            message = message.replaceAll("<reward>", Integer.toString(reward));
            message = message.replaceAll("<previousKarma>", Integer.toString(killerKarma));
            message = message.replaceAll("<karma>", Integer.toString(killerModifiedKarma));
            killer.sendMessage(message);
        }

    }

    /**
     * Launch When a player is killed.
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
        int killerKarma = killerConfig.getInt("karma");
        File victimFile = new File(this.karma.getDataFolder(), "playerdata/" + victim.getUniqueId() + ".yml");
        YamlConfiguration victimConfig = YamlConfiguration.loadConfiguration(victimFile);
        int victimKarma = victimConfig.getInt("karma");

        int killerModifiedKarma = killerKarma + (killerKarma - victimKarma) / 10;
        killer.sendMessage("Initial Karma from killer " + killer.getName() + "goes from " + killerKarma + " to " + killerModifiedKarma + ".");
        killer.sendMessage("Difference " + (killerModifiedKarma - killerKarma));
        killerConfig.set("karma", killerModifiedKarma);
        try {
            killerConfig.save(killerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
